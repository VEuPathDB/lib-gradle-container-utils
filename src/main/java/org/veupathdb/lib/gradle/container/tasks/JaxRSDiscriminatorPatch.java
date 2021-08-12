package org.veupathdb.lib.gradle.container.tasks;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.tasks.base.JaxRSSourceAction;

import java.io.*;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

public class JaxRSDiscriminatorPatch extends JaxRSSourceAction {
  private static final String LogModelDirCheck = "Testing generated model dir {}";
  private static final String LogModelDirFound = "Found generated model dir {}";
  private static final String LogJavaFileCheck = "Testing file {}";

  private static final String  DiscTypeNameLine = "  String _DISCRIMINATOR_TYPE_NAME = ";

  private static final String ModelDir = "model";

  public static final String TaskName = "patch-discriminators";

  @Override
  public void execute() {
    log().open();

    var stream = getGeneratedSourceDirectories()
      .map(f -> new File(f, ModelDir));

    if (log().debugEnabled()) {
      stream = stream.peek(newFileLogger(LogModelDirCheck))
        .filter(File::exists)
        .peek(newFileLogger(LogModelDirFound));
    } else {
      stream = stream.filter(File::exists);
    }

    stream = stream.map(File::listFiles)
      .flatMap(Arrays::stream)
      // Filter out implementation files
      .filter(this::nonImplPredicate)
      // Filter ot non-java files
      .filter(this::javaFilePredicate);

    if (log().debugEnabled()) {
      stream = stream.peek(newFileLogger(LogJavaFileCheck));
    }

    var iterator = stream.map(this::testFile)
      .filter(SourceSearchResult::found)
      .iterator();

    while (iterator.hasNext()) {
      final var result = iterator.next();

      log().debug("Processing file {}", result.file());

      final var type = getDiscriminatorType(result.file());

      if (!isCustomType(type)) {
        log().debug("Skipping due to non-custom discriminator type.");
        continue;
      }

      log().debug("Discriminator Type: {}", type);

      patchResult(result, type, interfaceName(result.file()));
    }

    log().close();
  }

  private static final String DiscriminatorTypeName        = "_DISCRIMINATOR_TYPE_NAME";
  private static final String InterfaceOldLinePrefix       = "  String " + DiscriminatorTypeName + " = \"";
  private static final int    InterfaceOldLinePrefixLength = InterfaceOldLinePrefix.length();

  @Override
  @NotNull
  public String pluginDescription() {
    return "Patches the generated JaxRS classes by correcting invalid discriminator types.";
  }

  private void patchResult(
    @NotNull final SourceSearchResult result,
    @NotNull final String type,
    @NotNull final String iName
  ) {
    final var tmp = new File(result.file().getParentFile(), "_" + result.file().getName());

    log().debug("Patching file {}", tmp);

    try {
      tmp.createNewFile();
    } catch (IOException e) {
      log().error("Failed to create temp file {}", tmp);
      throw new RuntimeException("Failed to create temp file " + tmp, e);
    }

    try (
      final var reader = new BufferedReader(new FileReader(result.file()));
      final var writer = new BufferedWriter(new FileWriter(tmp))
    ) {
      String lineText;
      int    lineNum = 0;

      while ((lineText = reader.readLine()) != null) {
        lineNum++;

        if (lineNum == result.line()) {
          writer.write("  ");
          writer.write(type);
          writer.write(' ');
          writer.write(DiscriminatorTypeName);
          writer.write(" = ");

          final var value = parseValue(lineText);

          // If the value is just the interface name, it's the root type and
          // should not have a value.  Set it to null.
          if (value.equals(iName)) {
            writer.write("null");
          } else {
            writer.write(assembleEnum(type, value));
          }

          writer.write(';');
        } else {
          writer.write(lineText);
        }

        writer.newLine();

        if (lineNum % 10 == 0) {
          writer.flush();
        }
      }

      writer.flush();
    } catch (IOException e) {
      log().error("Failed to write out patched interface to {}", tmp);
      throw new RuntimeException("Failed to write out patched interface to " + tmp, e);
    }

    if (!result.file().delete()) {
      log().error("Failed to delete file {}", result.file());
      throw new RuntimeException("Failed to delete file " + result.file());
    }

    if (!tmp.renameTo(result.file())) {
      log().error("Failed to move file {} to {}", tmp, result.file());
      throw new RuntimeException("Failed to move file " + tmp + " to " + result.file());
    }
  }

  private @NotNull String parseValue(@NotNull final String line) {
    return line.substring(InterfaceOldLinePrefixLength, line.lastIndexOf('"'));
  }

  private static final Pattern ValueCorrection = Pattern.compile("\\W+");
  static @NotNull String assembleEnum(@NotNull final String type, @NotNull final String value) {
    return type + "." + ValueCorrection.matcher(value)
      .replaceAll("")
      .toUpperCase(Locale.ROOT);
  }

  /**
   * Test the file to see if it contains the expected discriminator type definition.
   *
   * @param file File to test.
   *
   * @return A result wrapping details about the match.
   */
  private @NotNull SourceSearchResult testFile(@NotNull final File file) {
    log().open(file);

    String currentLine;
    int    lineNumber;

    try (final var reader = new BufferedReader(new FileReader(file))) {
      // Skip over the leading junk, start processing at the interface def line.
      //
      // If no interface def line was found, return an empty result.
      if ((lineNumber = skipUntilPrefix(reader, "public interface")) == -1) {
        return log().close(EmptySearchResult.Instance);
      }

      while ((currentLine = reader.readLine()) != null) {
        lineNumber++;

        // Skip empty lines
        if (currentLine.isEmpty())
          continue;

        if (currentLine.startsWith(DiscTypeNameLine)) {
          return log().close(new FullSearchResult(file, lineNumber));
        }
      }

      return log().close(EmptySearchResult.Instance);
    } catch (IOException e) {
      log().error("Failed to read file {}", file);
      throw new RuntimeException("Failed to read file " + file, e);
    }
  }

  private @NotNull String getDiscriminatorType(@NotNull final File file) {
    log().open(file);

    final var impl = toImpl(file);

    String line;

    try (final var reader = new BufferedReader(new FileReader(impl))) {
      if (skipUntilPrefix(reader, "public class") == -1) {
        throw new RuntimeException("Invalid implementation file " + impl);
      }

      while ((line = reader.readLine()) != null) {
        // Target line will start with "  private final "
        if (!line.startsWith("  private final "))
          continue;

        // Target line will end with "_DISCRIMINATOR_TYPE_NAME;"
        if (!line.endsWith("_DISCRIMINATOR_TYPE_NAME;"))
          continue;

        return log().close(line.substring(16, line.indexOf(' ', 16)));
      }

      throw new RuntimeException("Invalid implementation file " + impl);
    } catch (IOException e) {
      log().error("Failed to read file {}", impl);
      throw new RuntimeException("Failed to read file " + impl, e);
    }
  }

  private boolean isCustomType(@NotNull final String type) {
    log().open(type);

    // Longest builtin type names == 7 characters
    if (type.length() > 7)
      return log().close(true);

    return log().close(switch (type) {
      case "String", "Byte", "Short", "Integer", "Long", "Float", "Double" -> true;
      case "byte", "short", "int", "long", "float", "double" -> true;
      default -> false;
    });
  }

  /**
   * Inverse predicate testing whether a given file appears to be an
   * implementation class.
   *
   * @param file File to test.
   *
   * @return {@code true} if the file does <i>not</i> appear to be an
   * implementation class, otherwise {@code false}.
   */
  private boolean nonImplPredicate(@NotNull final File file) {
    return log().map(file, !file.getName().endsWith("Impl.java"));
  }

  /**
   * Predicate testing whether a given file's name indicates that it's a Java
   * source file.
   *
   * @param file File to test.
   *
   * @return {@code true} if the file appears to be a Java file, otherwise
   * {@code false}.
   */
  private boolean javaFilePredicate(@NotNull final File file) {
    return log().map(file, file.getName().endsWith(".java"));
  }

  private @NotNull File toImpl(@NotNull final File file) {
    final var path = file.getPath();
    return log().map(file, new File(path.substring(0, path.length() - 5) + "Impl.java"));
  }

  private int skipUntilPrefix(
    @NotNull final BufferedReader reader,
    @NotNull final String untilPrefix
  ) throws IOException {
    log().open(reader, untilPrefix);

    String line;
    int    lc = 0;

    while ((line = reader.readLine()) != null) {
      lc++;

      // Skip empty lines
      if (line.isBlank())
        continue;

      // Skip junk lines
      switch (line.charAt(0)) {
        case 'i': continue;
        case ' ': continue;
        case '@': continue;
      }

      if (line.startsWith(untilPrefix))
        return log().close(lc);
    }

    return log().close(-1);
  }

  private String interfaceName(@NotNull final File file) {
    final var fName = file.getName();
    return log().map(file, fName.substring(0, fName.length() - 5));
  }
}
