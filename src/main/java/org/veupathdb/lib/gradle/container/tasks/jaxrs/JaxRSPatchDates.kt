package org.veupathdb.lib.gradle.container.tasks.jaxrs

import org.veupathdb.lib.gradle.container.tasks.base.JaxRSSourceAction
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.util.Arrays
import java.util.stream.Stream

open class JaxRSPatchDates : JaxRSSourceAction() {

  companion object {
    const val TaskName = "jaxrs-patch-dates"

    private const val OldImportLine = "import java.util.Date;"
    private const val NewImportLine = "import java.time.OffsetDateTime;"

    private val DatePattern = Regex("([( .])Date([ ;])")
  }

  override val pluginDescription: String
    get() = "Replaces usage of the deprecated Java Date API with OffsetDateTime."

  override fun execute() {
    Stream.concat(getGeneratedModelDirectories(), getGeneratedResourceDirectories())
      .map { it.listFiles() }
      .filter { it != null }
      .flatMap { Arrays.stream(it) }
      .filter { it.name.endsWith(".java") }
      .forEach { processFile(it) }
  }

  private fun processFile(file: File) {
    val tmpFile = File("${file.path}.tmp")
    tmpFile.createNewFile()

    tmpFile.bufferedWriter().use { output ->
      file.bufferedReader().use { input -> processContents(output, input) }
      output.flush()
    }

    tmpFile.copyTo(file, true)
    tmpFile.delete()
  }

  private fun processContents(output: BufferedWriter, input: BufferedReader) {
    var line = input.readLine()

    while (line != null) {
      output.write(processLine(line))
      output.newLine()

      line = input.readLine()
    }
  }

  private val sb = StringBuilder(1024)

  private fun processLine(line: String): String? {
    if (line == OldImportLine)
      return NewImportLine

    val match = DatePattern.find(line) ?: return line
    val start = match.groups[1]!!
    val end = match.groups[2]!!

    sb.clear()
    sb.append(line, 0, start.range.last+1)
    sb.append("OffsetDateTime")
    sb.append(line, end.range.first, line.length)

    return sb.toString()
  }
}
