package org.veupathdb.lib.gradle.container.tasks.jaxrs

import org.veupathdb.lib.gradle.container.tasks.base.JaxRSSourceAction

import java.io.*
import java.util.Arrays

open class JaxRSPatchEnumValue : JaxRSSourceAction() {

  companion object {
    const val TaskName = "jaxrs-patch-enums"

    private const val OldNameField = "  private String name;"
    private const val NewNameField = "  public final String value;"

    private const val OldNameSetter = "    this.name = name;"
    private const val NewNameSetter = "    this.value = name;"
  }

  override fun execute() {
    log.open()

    var counter = 0

    log.debug("Patching enum files")

    getGeneratedModelDirectories()
      .map(File::listFiles)
      .flatMap(Arrays::stream)
      .filter(this::enumFilter)
      .peek { counter++ }
      .forEach(this::patch)

    log.info("Patched {} enum files", counter)

    log.close()
  }

  override val pluginDescription: String
    get() = "Adds a getter for the internal value for Raml for Jax RS generated enum types."

  private fun enumFilter(file: File): Boolean {
    log.open(file)

    return BufferedReader(FileReader(file)).use { reader ->
      log.close(reader.lines().anyMatch { it.startsWith("public enum") })
    }
  }

  private fun patch(file: File) {
    val tmpFile = makeTmpFile(file)
    patchContents(file, tmpFile)
    file.delete()
    tmpFile.renameTo(file)
  }

  private fun patchContents(from: File, to: File) {
    BufferedReader(FileReader(from)).use { reader ->
      BufferedWriter(FileWriter(to)).use { writer ->
        var i = 0
        while (true) {
          val line = reader.readLine() ?: break
          i++

          when {
            line.startsWith(OldNameField)  -> writer.write(NewNameField)

            line.startsWith(OldNameSetter) -> writer.write(NewNameSetter)

            line == "}"                    -> {
              writer.write(generateValueGetter())
              writer.write(line)
            }

            else                           -> writer.write(line)
          }

          writer.newLine()
        }

        writer.flush()
      }
    }
  }

  private fun makeTmpFile(mirror: File) =
    File(mirror.parentFile, "_" + mirror.name).also { it.createNewFile() }

  ////////////////////////////////////////////////////////////////////////////////////////////////
  //                                                                                            //
  //   Getter Assembly                                                                          //
  //                                                                                            //
  ////////////////////////////////////////////////////////////////////////////////////////////////

  private fun generateValueGetter() =
    "  public String getValue() {\n" +
    "    return this.value;\n" +
    "  }\n"

}
