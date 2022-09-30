package org.veupathdb.lib.gradle.container.tasks.jaxrs

import org.veupathdb.lib.gradle.container.tasks.base.JaxRSSourceAction

import java.io.*
import java.util.Arrays
import java.util.Locale

open class JaxRSDiscriminatorPatch : JaxRSSourceAction() {

  companion object {
    private const val LogModelDirCheck = "Testing generated model dir {}"
    private const val LogModelDirFound = "Found generated model dir {}"
    private const val LogJavaFileCheck = "Testing file {}"

    private const val DiscTypeNameLine = "  String _DISCRIMINATOR_TYPE_NAME = "

    private const val DiscriminatorTypeName = "_DISCRIMINATOR_TYPE_NAME"

    private const val InterfaceOldLinePrefix = "  String $DiscriminatorTypeName = \""

    private const val InterfaceOldLinePrefixLength = InterfaceOldLinePrefix.length

    private const val ModelDir = "model"

    const val TaskName = "patch-discriminators"

    private val ValueCorrection = Regex("\\W+")

    fun assembleEnum(type: String, value: String): String {
      return "$type." + ValueCorrection.replace(value, "")
        .toUpperCase(Locale.ROOT)
    }
  }

  override fun execute() {
    log.open()

    // Get a list of all the generated "my.package.path.generated.model" dirs.
    var stream = getGeneratedSourceDirectories()
      .map { File(it, ModelDir) }

    // Filter the list of directories down to only those that actually exist.
    stream = if (log.isDebug) {
      stream.peek(newFileLogger(LogModelDirCheck))
        .filter(File::exists)
        .peek(newFileLogger(LogModelDirFound))
    } else {
      stream.filter(File::exists)
    }

    // Expand the stream to all the files in each directory, then filter the
    // expanded file stream down to only files that appear to be interfaces.
    stream = stream.map(File::listFiles)
      .flatMap(Arrays::stream)
      // Filter out implementation files
      .filter(this::nonImplPredicate)
      // Filter ot non-java files
      .filter(this::javaFilePredicate)

    if (log.isDebug) {
      stream = stream.peek(newFileLogger(LogJavaFileCheck))
    }

    // Sift through each file looking for
    val iterator = stream.map(this::testFile)
      .filter(SourceSearchResult::found)
      .iterator()

    while (iterator.hasNext()) {
      val result = iterator.next()

      log.debug("Processing file {}", result.file)

      val type = getDiscriminatorType(result.file)

      if (!isCustomType(type)) {
        log.debug("Skipping due to non-custom discriminator type.")
        continue
      }

      log.debug("Discriminator Type: {}", type)

      patchResult(result, type, interfaceName(result.file))
    }

    log.close()
  }

  override val pluginDescription
    get() = "Patches the generated JaxRS classes by correcting invalid discriminator types."

  private fun patchResult(
    result: SourceSearchResult,
    type: String,
    iName: String
  ) {
    val tmp = File(result.file.parentFile, "_" + result.file.name)

    log.debug("Patching file {}", tmp)

    try {
      tmp.createNewFile()
    } catch (e: IOException) {
      log.error("Failed to create temp file {}", tmp)
      throw RuntimeException("Failed to create temp file $tmp", e)
    }

    BufferedReader(FileReader(result.file)).use { reader ->
      BufferedWriter(FileWriter(tmp)).use { writer ->
        var lineText = reader.readLine()
        var lineNum = 0

        while (lineText != null) {
          lineNum++

          if (lineNum == result.line) {
            writer.write("  ")
            writer.write(type)
            writer.write(" ")
            writer.write(DiscriminatorTypeName)
            writer.write(" = ")

            // Get the string value currently assigned to the
            // _DISCRIMINATOR_TYPE_NAME property.
            val value = parseDiscriminatorTypeStringValue(lineText)
              // Trim out any underscores (the raml to jaxrs generator does this
              // when converting RAML types to java classes).
              .replace("_", "")

            // If the value is just the interface name, it's the root type and
            // should not have a value.  Set it to null.
            if (value == iName) {
              writer.write("null")
            } else {
              writer.write(assembleEnum(type, value))
            }

            writer.write(";")
          } else {
            writer.write(lineText)
          }

          writer.newLine()

          if (lineNum % 10 == 0) {
            writer.flush()
          }

          lineText = reader.readLine()
        }

        writer.flush()
      }
    }

    if (!result.file.delete()) {
      log.error("Failed to delete file {}", result.file)
      throw RuntimeException("Failed to delete file " + result.file)
    }

    if (!tmp.renameTo(result.file)) {
      log.error("Failed to move file {} to {}", tmp, result.file)
      throw RuntimeException("Failed to move file " + tmp + " to " + result.file)
    }
  }

  /**
   * Parses the string value out of a raw `_DISCRIMINATOR_TYPE_NAME` line.
   *
   * Example Input:
   * ```
   *   String _DISCRIMINATOR_TYPE_NAME = "API_Variable";
   * ```
   *
   * Example Output:
   * ```
   * API_Variable
   * ```
   *
   * @param line Line of java source code containing the
   * `_DISCRIMINATOR_TYPE_NAME` definition.
   *
   * @return The string value assigned to the original
   * `_DISCRIMINATOR_TYPE_NAME` property.
   */
  private fun parseDiscriminatorTypeStringValue(line: String) =
    line.substring(InterfaceOldLinePrefixLength, line.lastIndexOf('"'))

  /**
   * Test the file to see if it contains the expected discriminator type definition.
   *
   * @param file File to test.
   *
   * @return A result wrapping details about the match.
   */
  private fun testFile(file: File): SourceSearchResult {
    log.open(file)

    BufferedReader(FileReader(file)).use { reader ->
      var lineNumber = skipUntilPrefix(reader, "public interface")

      if (lineNumber == -1) {
        return log.close(EmptySearchResult.Instance)
      }

      while (true) {
        val currentLine = reader.readLine() ?: break

        lineNumber++

        // Skip empty lines
        if (currentLine.isEmpty())
          continue

        if (currentLine.startsWith(DiscTypeNameLine)) {
          return log.close(FullSearchResult(file, lineNumber))
        }
      }

      return log.close(EmptySearchResult.Instance)
    }
  }

  private fun getDiscriminatorType(file: File): String {
    log.open(file)

    val impl = toImpl(file)

    BufferedReader(FileReader(impl)).use { reader ->
      if (skipUntilPrefix(reader, "public class") == -1) {
        throw RuntimeException("Invalid implementation file $impl")
      }

      while (true) {
        val line = reader.readLine() ?: break

        // Target line will start with "  private final "
        if (!line.startsWith("  private final "))
          continue

        // Target line will end with "_DISCRIMINATOR_TYPE_NAME;"
        if (!line.endsWith("_DISCRIMINATOR_TYPE_NAME;"))
          continue

        return log.close(line.substring(16, line.indexOf(' ', 16)))
      }

      throw RuntimeException("Invalid implementation file $impl")
    }
  }

  private fun isCustomType(type: String): Boolean {
    log.open(type)

    // Longest builtin type names == 7 characters
    if (type.length > 7)
      return log.close(true)

    // TODO: is this backwards?  Aren't these the built-in types?
    return log.close(when (type) {
      "String", "Byte", "Short", "Integer", "Long", "Float", "Double" -> false
      "byte", "short", "int", "long", "float", "double"               -> false
      else                                                            -> true
    })
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
  private fun nonImplPredicate(file: File): Boolean {
    return log.map(file, !file.name.endsWith("Impl.java"))
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
  private fun javaFilePredicate(file: File): Boolean {
    return log.map(file, file.name.endsWith(".java"))
  }

  private fun toImpl(file: File): File {
    val path = file.path
    return log.map(file, File(path.substring(0, path.length - 5) + "Impl.java"))
  }

  private fun skipUntilPrefix(
    reader: BufferedReader,
    untilPrefix: String
  ): Int {
    log.open(reader, untilPrefix)

    var lc = 0

    while (true) {
      val line = reader.readLine() ?: break

      lc++

      // Skip empty lines
      if (line.isBlank())
        continue

      // Skip junk lines
      when (line[0]) {
        'i' -> continue
        ' ' -> continue
        '@' -> continue
      }

      if (line.startsWith(untilPrefix))
        return log.close(lc)
    }

    return log.close(-1)
  }

  private fun interfaceName(file: File): String {
    val fName = file.name
    return log.map(file, fName.substring(0, fName.length - 5))
  }
}
