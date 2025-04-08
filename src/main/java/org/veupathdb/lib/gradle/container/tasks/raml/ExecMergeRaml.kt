package org.veupathdb.lib.gradle.container.tasks.raml

import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.veupathdb.lib.gradle.container.tasks.base.Action
import org.veupathdb.lib.gradle.container.util.Logger
import java.io.File

open class ExecMergeRaml : Action() {

  companion object {
    const val TaskName = "merge-raml"

    const val DefaultOutputLibraryName = "library.raml"
  }

  // WARNING!!!
  //
  // These values are lazily loaded because they are not yet available when the
  // class is created.
  //
  // They will be available by the time execute is called.
  private val binDirectory by lazy { RootDir.resolve(options.utils.binDirectory) }
  private val binFile by lazy { binDirectory.resolve("merge-raml") }
  private val exclusions by lazy { options.raml.mergeExcludedFiles }

  @get:InputDirectory
  val inputPath
    get() = options.raml.schemaRootDir

  @get:OutputFile
  val outputFile
    get() = options.raml.mergedOutputFile

  override val pluginDescription: String
    get() = "Merges the project's RAML files into a single library file."

  override fun register() {
    super.register()

    dependsOn(InstallMergeRaml.TaskName)
  }

  private fun logConfig(command: List<String>) {
    log.open()

    log.debug("binDirectory = {}", binDirectory)
    log.debug("binFile      = {}", binFile)
    log.debug("outputFile   = {}", outputFile)
    log.debug("inputPath    = {}", inputPath)
    log.debug("exclusions   = {}", exclusions)

    if (log.logLevel() >= Logger.Level.Debug) {
      log.debug("command      = {}", command.joinToString(" "))
    }

    log.close()
  }

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

    logConfig(command)

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
