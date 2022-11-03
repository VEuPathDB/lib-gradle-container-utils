package org.veupathdb.lib.gradle.container.tasks.jaxrs

import org.veupathdb.lib.gradle.container.tasks.base.JaxRSSourceAction
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.util.*

private typealias Replacer = (MatchResult) -> String

open class JaxRSPatchFileResponses : JaxRSSourceAction() {
  companion object {

    private const val OldImportLine = "import java.io.File;"

    private const val NewImportLine = "import jakarta.ws.rs.core.StreamingOutput;"

    private val MethodReplaces = arrayOf<Pair<Regex, Replacer>>(
      Regex("^ +public static (\\w+) respond(\\d+)With(\\w+)\\(File entity,\$")
        to { "    public static ${it.groupValues[1]} respond${it.groupValues[2]}With${it.groupValues[3]}(StreamingOutput entity," },
      Regex("^ +public static (\\w+) respond(\\d+)With(\\w+)\\(File entity\\) \\{\$")
        to { "    public static ${it.groupValues[1]} respond${it.groupValues[2]}With${it.groupValues[3]}(StreamingOutput entity) {" },
      Regex("^ +public static (\\w+) respond(\\d+)With\\(File entity,\$")
        to { "    public static ${it.groupValues[1]} respond${it.groupValues[2]}With(StreamingOutput entity," },
      Regex("^ +public static (\\w+) respond(\\d+)With\\(File entity\\) \\{\$")
        to { "    public static ${it.groupValues[1]} respond${it.groupValues[2]}With(StreamingOutput entity) {" },
    )

    const val TaskName = "jaxrs-patch-file-responses"
  }

  override val pluginDescription: String
    get() = "Patches generated resource type methods that return files to instead return input streams."

  override fun execute() {
    // Get a stream over the resource directories
    getGeneratedResourceDirectories()
      // Map to a stream of arrays of files contained in those directories
      .map { it.listFiles() }
      // It won't here, but technically listFiles can return null
      .filter { it != null }
      // Flat map our stream of arrays to a stream of files contained in the
      // resource directories.
      .flatMap { Arrays.stream(it) }
      // Filter our stream down to only java files
      .filter { it.name.endsWith(".java") }
      // Filter our stream down to only those files that contain a File import.
      .filter { testFile(it) }
      // Log what we're doing
      .peek { log.info("Processing file {}", it) }
      // Process the files
      .forEach { processFile(it) }
  }

  private fun testFile(file: File): Boolean {
    val read = file.bufferedReader()
    var line: String

    while (read.readLine().also { line = it } != null) {
      // If we see "public" then we have hit the class definition and did not
      // see a file import.  We can go ahead and return false now, as if this
      // file doesn't import `java.io.File` then it can't have a file entity
      // response method.
      if (line.startsWith("public "))
        return false

      if (line == OldImportLine)
        return true
    }

    // Shouldn't ever get here, but if we do then we obviously didn't find our
    // import line.
    return false
  }

  private fun processFile(original: File) {
    val temp = original.resolveSibling("${original.name}.fileresponse.tmp")

    temp.createNewFile()
    temp.bufferedWriter().use { w ->
      original.bufferedReader().use { r -> processContents(r, w) }
      w.flush()
    }

    temp.copyTo(original, true)
    temp.delete()
  }

  private fun processContents(reader: BufferedReader, writer: BufferedWriter) {
    var line = reader.readLine()

    while (line != null) {
      writer.write(
        when {
          line.startsWith(' ')  -> processLine(line)
          line == OldImportLine -> NewImportLine
          else                  -> line
        }
      )
      writer.newLine()


      line = reader.readLine()
    }
  }

  private fun processLine(line: String): String {
    for ((pattern, replacer) in MethodReplaces)
      return replacer(pattern.matchEntire(line) ?: continue)

    return line
  }
}