package org.veupathdb.lib.gradle.container.tasks.jaxrs

import org.veupathdb.lib.gradle.container.tasks.base.JaxRSSourceAction
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File

open class JaxRSPatchResponseDelegate: JaxRSSourceAction() {
  companion object {
    const val TaskName = "jaxrs-patch-response-delegate"

    private const val PrivatePrefix = "private"
    private const val ProtectedPrefix = "protected"
    private const val PublicPrefix = "public"
  }

  override val pluginDescription: String
    get() = "Patches the generated ResponseDelegate class to enable direct" +
    " usage of properties and constructors."

  override fun execute() {
    log.open()

    getGeneratedSupportDirectories()
      .map(File::listFiles)
      .filterNotNull()
      .flatMap { it.asSequence() }
      .filter { it.name == "ResponseDelegate.java" }
      .forEach { processFile(it, ::processClass) }

    log.close()
  }

  private fun processClass(input: BufferedReader, output: BufferedWriter) {
    val lines = input.lineSequence().iterator()

    // Skip until the class starts
    for (line in lines) {
      output.writeLine(line)
      if (line.startsWith("public class"))
        break
    }

    // Process relevant class contents
    for (line in lines) {

      // Change visibility of private properties.
      var i = line.indexOf(PrivatePrefix)
      if (i > -1) {
        line.patchToPublic(i, PrivatePrefix, output)
        output.newLine()
        continue
      }

      // Change visibility of constructors
      i = line.indexOf(ProtectedPrefix)
      if (i > -1) {
        line.patchToPublic(i, ProtectedPrefix, output)

        // Write out the rest of the constructor body
        for (l in lines) {
          output.writeLine(l)
          if (l.endsWith('}')) {
            output.newLine()
            break
          }
        }

        continue
      }

      // Stop processing at first method
      if (line.contains("@Override")) {
        output.writeLine(line)
        break
      }
    }

    // Copy out the rest unchanged
    for (line in lines) {
      output.writeLine(line)
    }
  }

  private fun String.patchToPublic(hit: Int, prefix: String, output: BufferedWriter) {
    output.write(substring(0, hit))
    output.write(PublicPrefix)
    output.write(substring(hit + prefix.length))
    output.newLine()
  }
}
