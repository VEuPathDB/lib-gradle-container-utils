package org.veupathdb.lib.gradle.container.tasks.base

import org.gradle.api.tasks.Internal
import org.veupathdb.lib.gradle.container.tasks.jaxrs.GeneratedModelDirectory
import org.veupathdb.lib.gradle.container.tasks.jaxrs.GeneratedResourceDirectory
import org.veupathdb.lib.gradle.container.tasks.jaxrs.GeneratedSupportDirectory
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream

import java.io.File

abstract class JaxRSSourceAction : SourceAction() {

  companion object {
    private const val GeneratedDir = "generated"
    private const val ModelDir     = "$GeneratedDir/$GeneratedModelDirectory"
    private const val ResourceDir  = "$GeneratedDir/$GeneratedResourceDirectory"
    private const val SupportDir   = "$GeneratedDir/$GeneratedSupportDirectory"

    private val AsciiDigitRange = 48..57
    private val AsciiLowerRange = 97..122
    private val AsciiUpperRange = 65..90
    private const val AsciiUnderscore = 95
    private const val CaseModifier = 32
  }

  @Internal
  protected fun getGeneratedSourceDirectories(): Sequence<File> {
    log.open()
    return log.close(getProjectSourceDirectories()
      .map { File(it, GeneratedDir) }
      .filter(File::exists))
  }

  @Internal
  protected fun getGeneratedModelDirectories(): Sequence<File> {
    log.open()
    return log.close(getProjectSourceDirectories()
      .map { File(it, ModelDir) }
      .filter(File::exists))
  }

  @Internal
  protected fun getGeneratedSupportDirectories(): Sequence<File> {
    log.open()
    return log.close(getProjectSourceDirectories()
      .map { File(it, SupportDir) }
      .filter(File::exists))
  }

  @Internal
  protected fun getGeneratedResourceDirectories(): Sequence<File> {
    log.open()
    return log.close(getProjectSourceDirectories()
      .map { File(it, ResourceDir) }
      .filter(File::exists))
  }

  protected inline fun processFile(file: File, fn: (BufferedReader, BufferedWriter) -> Unit) {
    val tmpFile = File("${file.path}.tmp")
    tmpFile.createNewFile()

    tmpFile.bufferedWriter().use { output ->
      file.bufferedReader().use { input -> fn(input, output) }
      output.flush()
    }

    tmpFile.copyTo(file, true)
    tmpFile.delete()
  }

  @Suppress("NOTHING_TO_INLINE")
  protected inline fun BufferedWriter.writeLine(line: String) {
    write(line)
    newLine()
  }

  protected fun computeConstName(name: String): String {
    val out = ByteArrayOutputStream(name.length + 8)
    val raw = name.byteInputStream()

    // upper = 1
    // lower = 2
    // digit = 3
    // under = 4
    var lastType = 0
    var written = 0
    while (true) {
      when (val b = raw.read()) {
        -1 -> break

        in AsciiUpperRange -> {
          if (lastType == 2 || lastType == 3)
            out.write(AsciiUnderscore)
          out.write(b)
          lastType = 1
        }

        in AsciiLowerRange -> {
          if (lastType == 3)
            out.write(AsciiUnderscore)
          out.write(b - CaseModifier)
          lastType = 2
        }

        in AsciiDigitRange -> {
          if (written == 0 || lastType != 3)
            out.write(AsciiUnderscore)
          out.write(b)
          lastType = 3
        }

        else -> {
          if (written == 0 || lastType == 4)
            continue

          out.write(AsciiUnderscore)
          lastType = 4
        }
      }

      written++
    }

    // If the whole property name is something that can't be converted to a java
    // variable name, return an empty string.  We will skip empty constant names
    // when generating the constants.
    if (written == 0)
      return ""

    // If the last character was an underscore, trim it off.
    if (lastType == 4)
      return out.toString().let { it.substring(0, it.length-1) }

    return out.toString()
  }
}
