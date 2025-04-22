package org.veupathdb.lib.gradle.container.tasks.jaxrs

import org.gradle.api.Task
import org.veupathdb.lib.gradle.container.tasks.base.exec.BinExecAction
import org.veupathdb.lib.gradle.container.tasks.raml.ExecMergeRaml

import java.io.File

/**
 * Generate Jax-RS Classes
 * <p>
 * Generates Jax-RS annotated Java classes based on the project's RAML
 * definitions.
 *
 * @since 1.1.0
 */
abstract class GenerateJaxRS : BinExecAction() {

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

  private val basePackage by lazy { projectConfig().projectPackage }

  private val sourceDirectory
    get() = "$ProjectDir/src/main/java"

  private val modelPackagePath
    get() = "$generatedPackagePath.$GeneratedModelDirectory"

  private val resourcePackagePath
    get() = "$generatedPackagePath.$GeneratedResourceDirectory"

  private val supportPackagePath
    get() = "$generatedPackagePath.$GeneratedSupportDirectory"

  private val generatedPackagePath
    get() = "$basePackage.generated"

  override val pluginDescription: String
    get() = "Generates JaxRS Java code based on the project's RAML API spec."

  override fun register() {
    super.register()

    dependsOn(
      ExecMergeRaml.TaskName,
      InstallRaml4JaxRS.TaskName,
    )

    finalizedBy(
      JaxRSPatchDiscriminators.TaskName,
      JaxRSPatchEnumValue.TaskName,
      JaxRSGenerateStreams.TaskName,
      JaxRSPatchJakartaImports.TaskName,
      JaxRSPatchBoxedTypes.TaskName,
      JaxRSPatchFileResponses.TaskName,
      JaxRSPatchDates.TaskName,
      JaxRSPatchResponseDelegate.TaskName,
      JaxRSPatchResponseTypes.TaskName,
      JaxRSGenerateFieldNameConstants.TaskName,
      JaxRSGenerateUrlPathConstants.TaskName,
    )
  }

  override fun fillIncrementalInputFiles(files: MutableCollection<File>) {
    files.add(options.raml.rootApiDefinition)
  }

  override fun fillIncrementalOutputDirs(dirs: MutableCollection<File>) {
    dirs.add(File(sourceDirectory, modelPackagePath.replace('.', '/')))
    dirs.add(File(sourceDirectory, resourcePackagePath.replace('.', '/')))
    dirs.add(File(sourceDirectory, generatedPackagePath.replace('.', '/')))
  }

  override fun getCommandName(): String {
    // quick trick here before executing; remove existing generated files so
    //   resulting contents accurately reflect the RAML (no extra lingering files)
    val packagePath = generatedPackagePath.replace('.','/')
    File("$sourceDirectory/$packagePath").deleteRecursively()

    return Command
  }

  override fun finalizedBy(vararg paths: Any?): Task {
    return super.finalizedBy(*paths)
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
}
