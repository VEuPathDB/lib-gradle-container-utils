package org.veupathdb.lib.gradle.container.tasks.jaxrs

import org.veupathdb.lib.gradle.container.tasks.base.JaxRSSourceAction

import java.io.*
import java.util.Arrays
import java.util.Locale

open class JaxRSPatchEnumValue : JaxRSSourceAction() {

  companion object {
    const val TaskName = "jaxrs-patch-enums"

    private const val OldLeader = "  private "
    private const val NewLeader = "  public final "

    private const val Getter1 = "\n\n  public "
    private const val Getter2 = " get"
    private const val Getter3 = "() {\n" +
    "    return this."
    private const val Getter4 = ";\n" +
    "  }\n"
    private const val GetterLength = Getter1.length +
      Getter2.length +
      Getter3.length +
      Getter4.length
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
    var getter: String? = null

    BufferedReader(FileReader(from)).use { reader ->
      BufferedWriter(FileWriter(to)).use { writer ->
        var i = 0
        while (true) {
          val line = reader.readLine() ?: break
          i++

          if (line.startsWith(OldLeader)) {
            writer.write(NewLeader)
            val end = line.substring(OldLeader.length)
            getter = assembleGetter(end)
            writer.write(end)
          } else if (getter != null && "}" == line) {
            writer.write(getter!!)
            writer.write(line)
          } else {
            writer.write(line)
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


  private fun assembleGetter(line: String): String {
    val x = line.indexOf(' ')
    val y = line.indexOf(';', x)

    return assembleGetter(line.substring(0, x), line.substring(x+1, y))
  }

  private fun assembleGetter(type: String, name: String): String {
    return Getter1 +
      type +
      Getter2 +
      name.substring(0, 1).toUpperCase(Locale.ROOT) +
      name.substring(1) +
      Getter3 +
      name +
      Getter4
  }
}
