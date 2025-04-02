package org.veupathdb.lib.gradle.container.tasks.jaxrs

import org.veupathdb.lib.gradle.container.tasks.base.JaxRSSourceAction
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File

open class JaxRSPatchJakartaImports : JaxRSSourceAction() {

  companion object {
    private const val ReplaceOld = "javax.ws"
    private const val ReplaceNew = "jakarta.ws"

    const val TaskName = "jaxrs-patch-jakarta-imports"
  }

  override val pluginDescription: String
    get() = "Patches generated jax-rs classes to import jakarta.ws rather than javax.ws"

  override fun execute() {
    log.open()

    // Get all the generated source root directories
    getGeneratedSourceDirectories()
      // Expand the stream to the 2 relevant subdirectories
      .flatMap { sequence {
        yield(File(it, GeneratedResourceDirectory))
        yield(File(it, GeneratedSupportDirectory))
      } }
      .filter { it.exists() }               // Filter out any erroneous paths
      .map { it.listFiles() }               // Map to lists of files in each directory
      .filterNotNull()                      // Filter out any erroneous entries (just to be safe)
      .flatMap { it.asSequence() }          // Expand the stream to a stream of files
      .filter { it.name.endsWith(".java") } // Filter the stream down to just java files
      .forEach(::processFile)

    log.close()
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

  private fun processLine(line: String) =
    if (line.startsWith("import")) {
      line.replace(ReplaceOld, ReplaceNew)
    } else {
      line
    }
}
