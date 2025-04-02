package org.veupathdb.lib.gradle.container.tasks.jaxrs

import org.veupathdb.lib.gradle.container.tasks.base.JaxRSSourceAction
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File

open class JaxRSPatchResponseTypes: JaxRSSourceAction() {
  companion object {
    const val TaskName = "jaxrs-patch-response-types"

    private const val PrivatePrefix = "private"
    private const val ProtectedPrefix = "protected"
    private const val PublicPrefix = "public"

    private val ResponseClassPattern = Regex("^ +class (\\w+) extends ResponseDelegate \\{$")
  }

  override val pluginDescription: String
    get() = "Patches the generated response type classes to make constructors" +
    " public."

  override fun execute() {
    log.open()

    getGeneratedResourceDirectories()
      .map(File::listFiles)
      .filterNotNull()
      .flatMap { it.asSequence() }
      .filter { it.name.endsWith(".java") }
      .forEach { processFile(it, ::processClass) }

    log.close()
  }

  private fun processClass(input: BufferedReader, output: BufferedWriter) {
    val lines = input.lineSequence().iterator()

    // Process relevant class contents
    while (lines.hasNext()) {
      val className = skipUntilResponseClass(lines, output) ?: continue
      var firstMethodLine: String? = null

      // Patch constructor visibilities
      for (line in lines) {
        val i = line.indexOf(PrivatePrefix)
        if (i > -1) {
          line.patchToPublic(i, PrivatePrefix, output)

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

        // Stop once we hit the first method (which will be public)
        if (line.contains(PublicPrefix)) {
          firstMethodLine = line
          break
        }
      }

      // Inject a new constructor
      output.writeLine(createCopyConstructor(className))
      output.newLine()

      output.writeLine(firstMethodLine!!)
    }
  }

  private fun skipUntilResponseClass(lines: Iterator<String>, output: BufferedWriter): String? {
    for (line in lines) {
      output.writeLine(line)
      if (line.contains("class")) {
        val match = ResponseClassPattern.matchEntire(line) ?: continue
        return match.groups[1]!!.value
      }
    }

    return null
  }

  private fun String.patchToPublic(hit: Int, prefix: String, output: BufferedWriter) {
    output.write(substring(0, hit))
    output.write(PublicPrefix)
    output.write(substring(hit + prefix.length))
    output.newLine()
  }

  private fun createCopyConstructor(className: String) = """
      |    public $className(ResponseDelegate response) {
      |      super(response.delegate, response.entity);
      |    }
    """.trimMargin()

}
