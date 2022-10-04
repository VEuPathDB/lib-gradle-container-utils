package org.veupathdb.lib.gradle.container.tasks.raml

import org.veupathdb.lib.gradle.container.tasks.base.Action

open class ExecMergeRaml : Action() {

  companion object {
    const val TaskName = "merge-raml"
  }

  private val binDirectory by lazy { RootDir.resolve(options.binBuilds.binDirectory) }
  private val binFile by lazy { binDirectory.resolve("merge-raml") }
  private val outputFile by lazy { ProjectDir.resolve("schema/library.raml") }
  private val inputPath by lazy { ProjectDir.resolve("schema") }

  override val pluginDescription: String
    get() = "Merges the project's RAML files into a single library.raml file."

  override fun execute() {
    log.open()

    outputFile.delete()
    outputFile.createNewFile()

    with(ProcessBuilder(binFile.path, inputPath.path).start()) {
      outputFile.outputStream().use { inputStream.transferTo(it) }
      errorStream.transferTo(System.err)
      require(waitFor() == 0)
    }

    log.close()
  }


}