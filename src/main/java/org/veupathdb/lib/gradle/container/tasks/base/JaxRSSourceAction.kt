package org.veupathdb.lib.gradle.container.tasks.base

import org.gradle.api.tasks.Internal
import org.veupathdb.lib.gradle.container.tasks.jaxrs.GeneratedModelDirectory
import org.veupathdb.lib.gradle.container.tasks.jaxrs.GeneratedResourceDirectory
import org.veupathdb.lib.gradle.container.tasks.jaxrs.GeneratedSupportDirectory
import java.io.BufferedReader
import java.io.BufferedWriter

import java.io.File

abstract class JaxRSSourceAction : SourceAction() {

  companion object {
    private const val GeneratedDir = "generated"
    private const val ModelDir     = "$GeneratedDir/$GeneratedModelDirectory"
    private const val ResourceDir  = "$GeneratedDir/$GeneratedResourceDirectory"
    private const val SupportDir   = "$GeneratedDir/$GeneratedSupportDirectory"
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
}
