package org.veupathdb.lib.gradle.container.tasks.jaxrs

import org.veupathdb.lib.gradle.container.tasks.base.JaxRSSourceAction

import java.io.*
import java.util.Arrays

open class JaxRSPatchEnumValue : JaxRSSourceAction() {

  companion object {
    const val TaskName = "jaxrs-patch-enums"

    private val EnumLeaderPat = Regex("^ *(?:public)? +enum ")

    private val OldNamePattern = Regex("^ *(?:private|protected final) (\\w+) name;")

    private fun newNameField(type: String) =
      "public final $type value;"

    private const val OldNameSetter = "this.name = name;"
    private const val NewNameSetter = "this.value = name;"
  }

  override fun execute() {
    log.open()

    var counter = 0

    log.debug("Patching enum files")

    getGeneratedModelDirectories()
      .map(File::listFiles)
      .filterNotNull()
      .flatMap { it.asSequence() }
      .filter(this::enumFilter)
      .onEach { counter++ }
      .forEach(this::patch)

    log.info("Patched {} enum files", counter)

    log.close()
  }

  override val pluginDescription: String
    get() = "Adds a getter for the internal value for Raml for Jax RS generated enum types."

  private fun enumFilter(file: File): Boolean {
    log.open(file)

    return BufferedReader(FileReader(file)).use { reader ->
      log.close(reader.lines().anyMatch { EnumLeaderPat.containsMatchIn(it) })
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
        var inEnum = false
        var i = 0

        while (true) {
          val line = reader.readLine() ?: break
          i++

          if (inEnum) {
            if (line.matches(OldNamePattern)) {
              val indent = copyIndent(line)
              val match = OldNamePattern.find(line)!!.groupValues[1]
              writer.write(indent)
              writer.write(newNameField(match))
              writer.newLine()
              writer.newLine()
              writer.write(generateValueGetter(indent, match))
            }

            else if (line.contains(OldNameSetter)) {
              writer.write(copyIndent(line))
              writer.write(NewNameSetter)
            }

            else {
              writer.write(line)
            }

          } else {
            if (EnumLeaderPat.containsMatchIn(line))
              inEnum = true
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

  private fun copyIndent(line: String): String {
    val sb = StringBuilder(8)
    var i = 0
    while (line[i++] == ' ')
      sb.append(' ')
    return sb.toString()
  }

  private fun generateValueGetter(indent: String, type: String) =
    "${indent}public $type getValue() {\n" +
    "$indent  return this.value;\n" +
    "${indent}}"

}
