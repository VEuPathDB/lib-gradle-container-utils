package org.veupathdb.lib.gradle.container.tasks.jaxrs;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.tasks.base.JaxRSSourceAction;
import org.veupathdb.lib.gradle.container.util.Counter;

import java.io.*;
import java.util.Arrays;
import java.util.Locale;

public class JaxRSEnumValuePatch extends JaxRSSourceAction {
  public static final String TaskName = "patch-enums";

  private static final String OldLeader = "  private ";
  private static final String NewLeader = "  public final ";

  @Override
  public void execute() {
    log().open();

    final var counter = new Counter();

    log().debug("Patching enum files");
    getGeneratedModelDirectories()
      .map(File::listFiles)
      .flatMap(Arrays::stream)
      .filter(this::enumFilter)
      .peek(counter::increment)
      .forEach(this::patch);
    log().info("Patched {} enum files", counter.get());

    log().close();
  }

  @Override
  public @NotNull String pluginDescription() {
    return "Adds a getter for the internal value for Raml for Jax RS generated enum types.";
  }

  private boolean enumFilter(@NotNull final File file) {
    log().open(file);

    try (final var reader = new BufferedReader(new FileReader(file))) {
      return log().close(reader.lines().anyMatch(line -> line.startsWith("public enum")));
    } catch (final IOException e) {
      return log().fatal(e);
    }
  }

  private void patch(@NotNull final File file) {
    final var tmpFile = makeTmpFile(file);
    patchContents(file, tmpFile);
    file.delete();
    tmpFile.renameTo(file);
  }

  private void patchContents(@NotNull final File from, @NotNull final File to) {
    String getter = null;

    try (
      final var reader = new BufferedReader(new FileReader(from));
      final var writer = new BufferedWriter(new FileWriter(to))
    ) {
      String line;
      var i = 0;

      while ((line = reader.readLine()) != null) {
        i++;

        if (line.startsWith(OldLeader)) {
          writer.write(NewLeader);
          var end = line.substring(OldLeader.length());
          getter = assembleGetter(end);
          writer.write(end);
        } else if (getter != null && "}".equals(line)) {
          writer.write(getter);
          writer.write(line);
        } else {
          writer.write(line);
        }
        writer.newLine();
      }

      writer.flush();

    } catch (IOException e) {
      log().fatal(e);
    }
  }

  private @NotNull File makeTmpFile(@NotNull final File mirror) {
    final var newFile = new File(mirror.getParentFile(), "_" + mirror.getName());

    try {
      newFile.createNewFile();
    } catch (IOException e) {
      return log().fatal(e);
    }

    return newFile;
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////
  //                                                                                            //
  //   Getter Assembly                                                                          //
  //                                                                                            //
  ////////////////////////////////////////////////////////////////////////////////////////////////

  private static final String Getter1 = "\n\n  public ";
  private static final String Getter2 = " get";
  private static final String Getter3 = "() {\n" +
    "    return this.";
  private static final String Getter4 = ";\n" +
    "  }\n";
  private static final int GetterLength = Getter1.length()
    + Getter2.length()
    + Getter3.length()
    + Getter4.length();

  private @NotNull String assembleGetter(@NotNull String line) {
    var x = line.indexOf(' ');
    var y = line.indexOf(';', x);

    return assembleGetter(line.substring(0, x), line.substring(x+1, y));
  }

  private @NotNull String assembleGetter(@NotNull String type, @NotNull String name) {
    return Getter1 +
      type +
      Getter2 +
      name.substring(0, 1).toUpperCase(Locale.ROOT) +
      name.substring(1) +
      Getter3 +
      name +
      Getter4;
  }
}
