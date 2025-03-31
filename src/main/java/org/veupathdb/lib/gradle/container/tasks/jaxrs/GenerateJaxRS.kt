package org.veupathdb.lib.gradle.container.tasks.jaxrs

import org.veupathdb.lib.gradle.container.tasks.base.exec.BinExecAction

import java.io.File

/**
 * Generate Jax-RS Classes
 * <p>
 * Generates Jax-RS annotated Java classes based on the project's RAML
 * definitions.
 *
 * @since 1.1.0
 */
open class GenerateJaxRS : BinExecAction() {

  companion object {
    const val TaskName = "generate-jaxrs"

    private const val Command = "java"

    private const val FlagDirectory = "--directory"

    private const val FlagGenTypesWith = "--generate-types-with"

    private const val FlagModelPackage = "--model-package"

    private const val FlagResourcePackage = "--resource-package"

    private const val FlagSupportPackage = "--support-package"

    private const val FlagJar = "-jar"

    private const val ParamGenTypesWith = "jackson"

    const val DefaultRootRamlApiDefinitionFile = "api.raml"
  }

  override val pluginDescription: String
    get() = "Generates JaxRS Java code based on the project's RAML API spec."

  override fun getCommandName(): String {
    // quick trick here before executing; remove existing generated files so
    //   resulting contents accurately reflect the RAML (no extra lingering files)
    val packagePath = generatedPackagePath.replace('.','/')
    File("$sourceDirectory/$packagePath").deleteRecursively()

    return Command
  }

  override fun appendArguments(args: MutableList<String>) {
    log.open(args)

    log.debug("api RAML path = {}", options.raml.rootApiDefinition)

    args.addAll(listOf(
      FlagJar, InstallRaml4JaxRS.outputFileName(options.raml.raml4JaxRSDownloadURL),
      options.raml.rootApiDefinition.path,
      FlagDirectory, sourceDirectory,
      FlagGenTypesWith, ParamGenTypesWith,
      FlagModelPackage, modelPackagePath,
      FlagResourcePackage, resourcePackagePath,
      FlagSupportPackage, supportPackagePath
    ))

    args.addAll(execConfiguration.arguments)

    log.close()
  }

  override val execConfiguration get() = options.raml

  private val basePackage by lazy { projectConfig().projectPackage }

  private val sourceDirectory get() = "$ProjectDir/src/main/java"

  private val modelPackagePath get() = "$generatedPackagePath.$GeneratedModelDirectory"

  private val resourcePackagePath get() = "$generatedPackagePath.$GeneratedResourceDirectory"

  private val supportPackagePath get() = "$generatedPackagePath.$GeneratedSupportDirectory"

  private val generatedPackagePath get() = "$basePackage.generated"
}
