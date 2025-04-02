package org.veupathdb.lib.gradle.container.tasks.jaxrs

import org.veupathdb.lib.gradle.container.tasks.base.JaxRSSourceAction
import java.io.File
import java.util.Arrays
import java.util.stream.Stream

open class JaxRSPatchDates : JaxRSSourceAction() {

  companion object {
    const val TaskName = "jaxrs-patch-dates"

    private val replacements = listOf(
      "(Date " to "(OffsetDateTime ",
      " Date " to " OffsetDateTime ",
      "import java.util.Date;" to "import java.time.OffsetDateTime;",
      "  @JsonDeserialize(\n      using = TimestampDeserializer.class\n  )\n" to "",
      "  @JsonFormat(\n      shape = JsonFormat.Shape.STRING,\n      pattern = \"yyyy-MM-dd'T'HH:mm:ss.SSSXXX\"\n  )\n" to "",
      "  @JsonFormat(\n      shape = JsonFormat.Shape.STRING,\n      pattern = \"yyyy-MM-dd'T'HH:mm:ss\"\n  )" to "",
    )
  }

  override val pluginDescription: String
    get() = "Replaces usage of the deprecated Java Date API with OffsetDateTime."

  override fun execute() {
    (getGeneratedModelDirectories() + getGeneratedResourceDirectories())
      .map(File::listFiles)
      .filterNotNull()
      .flatMap { it.asSequence() }
      .filter { it.name.endsWith(".java") }
      .filter { it.name != "TimestampDeserializer.java" }
      .forEach { processFile(it) }
  }

  private fun processFile(file: File) {
    var contents = file.readText()

    for (replacement in replacements)
      contents = contents.replace(replacement.first, replacement.second)

    val tmpFile = File("${file.path}.tmp")
    tmpFile.createNewFile()
    tmpFile.writeText(contents)
    tmpFile.copyTo(file, true)
    tmpFile.delete()
  }
}
