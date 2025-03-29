package org.veupathdb.lib.gradle.container.tasks.raml

import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import org.veupathdb.lib.gradle.container.tasks.base.exec.ExecAction
import org.veupathdb.lib.gradle.container.tasks.base.exec.RedirectConfig
import java.io.File
import java.util.Arrays

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

  override fun getWorkDirectory() = ProjectDir

  override val pluginDescription
    get() = "Generates HTML documentation from the RAML API spec."

  override fun getCommandName() = "raml2html"

  override fun appendArguments(args: MutableList<String>) {
    log.open(args)

    args.addAll(listOf(execConfiguration.rootApiDefinition.path, "--theme", "raml2html-modern-theme"))

    log.close()
  }

  override val execConfiguration get() = options.raml

  override fun getStdOutRedirect(): RedirectConfig? {
    return log.getter(RedirectConfig.toFile(DefaultDocFileName))
  }

  override fun postExec() {
    log.open()

    log.debug("Copying generated docs to target doc directories")

    val conf = execConfiguration

    util.moveFile(
      File(DefaultDocFileName),
      conf.apiDocOutputFile.also { it.ensureParentDirsCreated() },
      File(conf.resourceDocsDir.also { it.ensureParentDirsCreated() }, DefaultDocFileName)
    )

    log.close()
  }
}
