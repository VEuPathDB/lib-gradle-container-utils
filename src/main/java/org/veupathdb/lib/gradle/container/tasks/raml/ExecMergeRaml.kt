package org.veupathdb.lib.gradle.container.tasks.raml

import java.io.File
import org.veupathdb.lib.gradle.container.tasks.base.Action

open class ExecMergeRaml : Action() {

  companion object {
    const val TaskName = "merge-raml"
  }

  // WARNING!!!
  //
  // These values are lazily loaded because they are not yet available when the
  // class is created.
  //
  // They will be available by the time execute is called.
  private val binDirectory by lazy { RootDir.resolve(options.binBuilds.binDirectory) }
  private val binFile by lazy { binDirectory.resolve("merge-raml") }
  private val outputFile by lazy { options.generateRamlDocs.outputFilePath }
  private val inputPath by lazy { ProjectDir.resolve(options.generateRamlDocs.schemaRootDir) }
  private val exclusions by lazy { options.generateRamlDocs.excludedFiles }

  override val pluginDescription: String
    get() = "Merges the project's RAML files into a single library.raml file."

  override fun execute() {
    log.open()

    val command = ArrayList<String>(2 + 2 * exclusions.size)
    command.add(binFile.path)
    exclusions.asSequence()
      .map { File(it).name }
      .forEach {
        command.add("-x")
        command.add(it)
      }
    command.add(inputPath.path)

    outputFile.delete()
    outputFile.createNewFile()

    with(ProcessBuilder(command).start()) {
      outputFile.outputStream().use { inputStream.transferTo(it) }
      errorStream.transferTo(System.err)
      require(waitFor() == 0)
    }

    log.close()
  }
}
