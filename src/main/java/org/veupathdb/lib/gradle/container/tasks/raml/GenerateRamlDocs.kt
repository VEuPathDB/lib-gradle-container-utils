package org.veupathdb.lib.gradle.container.tasks.raml

import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import org.veupathdb.lib.gradle.container.tasks.base.exec.ExecAction
import org.veupathdb.lib.gradle.container.tasks.base.exec.RedirectConfig
import java.io.File

/**
 * Generate RAML Documentation
 * <p>
 * Generates HTML API documentation from the project's root API RAML file.
 *
 * @since 1.1.0
 */
open class GenerateRamlDocs : ExecAction() {

  companion object {
    const val TaskName = "generate-raml-docs"

    const val DefaultDocFileName = "api.html"
  }

  @get:InputFile
  val apiRootDefinition
    get() = execConfiguration.rootApiDefinition

  @get:InputDirectory
  val inputDirectory
    get() = execConfiguration.schemaRootDir

  @get:OutputFile
  val docsOutput
    get() = execConfiguration.apiDocOutputFile

  @get:OutputFile
  val resourceOutput
    get() = File(execConfiguration.resourceDocsDir, DefaultDocFileName)

  override fun register() {
    super.register()

    dependsOn(ExecMergeRaml.TaskName)
  }

  override fun getWorkDirectory() = ProjectDir

  override val pluginDescription
    get() = "Generates HTML documentation from the RAML API spec."

  override fun getCommandName() = "raml2html"

  override fun appendArguments(args: MutableList<String>) {
    log.open(args)

    args.addAll(listOf(apiRootDefinition.path, "--theme", "raml2html-modern-theme"))

    log.close()
  }

  override val execConfiguration get() = options.raml

  override fun getStdOutRedirect(): RedirectConfig? {
    return log.getter(RedirectConfig.toFile(DefaultDocFileName))
  }

  override fun postExec() {
    log.open()

    log.debug("Copying generated docs to target doc directories")

    util.moveFile(
      File(DefaultDocFileName),
      docsOutput.also { it.ensureParentDirsCreated() },
      resourceOutput.also { it.ensureParentDirsCreated() }
    )

    log.close()
  }
}
