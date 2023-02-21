package org.veupathdb.lib.gradle.container.tasks.jaxrs

import org.veupathdb.lib.gradle.container.tasks.base.JaxRSSourceAction
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.util.Arrays
import java.util.stream.Stream

open class JaxRSPatchBoxedTypes : JaxRSSourceAction() {

  companion object {
    const val TaskName = "jaxrs-patch-boxed-types"
  }

  private val replacements = arrayOf(
    Replacement(Regex("\\bboolean\\b"), "Boolean"),
    Replacement(Regex("\\bbyte\\b"),    "Byte"),
    Replacement(Regex("\\bchar\\b"),    "Character"),
    Replacement(Regex("\\bdouble\\b"),  "Double"),
    Replacement(Regex("\\bfloat\\b"),   "Float"),
    Replacement(Regex("\\bint\\b"),     "Integer"),
    Replacement(Regex("\\blong\\b"),    "Long"),
    Replacement(Regex("\\bshort\\b"),   "Short"),
  )

  override val pluginDescription: String
    get() = "Replaces java primitive usages with their boxed forms."

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

  private fun processLine(line: String): String {
    var patched = line

    for (repl in replacements)
      patched = repl.pattern.replace(patched, repl.replacement)

    return patched
  }

  private data class Replacement(val pattern: Regex, val replacement: String)
}
