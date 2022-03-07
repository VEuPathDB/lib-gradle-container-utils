package org.veupathdb.lib.gradle.container.tasks.raml

import org.jetbrains.annotations.NotNull
import org.veupathdb.lib.gradle.container.config.RedirectConfig
import org.veupathdb.lib.gradle.container.tasks.base.exec.ExecAction
import org.veupathdb.lib.gradle.container.tasks.base.exec.ExecConfiguration

import java.io.File
import java.util.Arrays
import java.util.List
import java.util.Optional

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
  }

  override fun getWorkDirectory() = RootDir

  override val pluginDescription
    get() = "Generates HTML documentation from the RAML API spec."

  override fun getCommandName() = "raml2html"

  override fun appendArguments(args: MutableList<String>) {
    log.open(args)

    args.addAll(Arrays.asList("api.raml", "--theme", "raml2html-modern-theme"))

    log.close()
  }

  override val execConfiguration get() = options.generateJaxRS

  override fun getStdOutRedirect(): RedirectConfig? {
    return log.getter(RedirectConfig.toFile(execConfiguration.apiDocFileName))
  }

  override fun postExec() {
    log.open()

    log.debug("Copying generated docs to target doc directories")

    val conf = execConfiguration

    util.moveFile(
      File(conf.apiDocFileName),
      File(util.getOrCreateDir(File(RootDir, conf.repoDocsDir)), conf.apiDocFileName),
      File(util.getOrCreateDir(File(RootDir, conf.resourceDocsDir)), conf.apiDocFileName)
    )

    log.close()
  }
}
