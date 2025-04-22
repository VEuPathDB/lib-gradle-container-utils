package org.veupathdb.lib.gradle.container

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.veupathdb.lib.gradle.container.config.Options
import org.veupathdb.lib.gradle.container.tasks.base.Action
import org.veupathdb.lib.gradle.container.tasks.check.CheckEnv
import org.veupathdb.lib.gradle.container.tasks.check.DownloadDependencies
import org.veupathdb.lib.gradle.container.tasks.docker.DockerBuild
import org.veupathdb.lib.gradle.container.tasks.jaxrs.*
import org.veupathdb.lib.gradle.container.tasks.raml.ExecMergeRaml
import org.veupathdb.lib.gradle.container.tasks.raml.GenerateRamlDocs
import org.veupathdb.lib.gradle.container.tasks.raml.InstallMergeRaml


/**
 * Gradle Container Utils Plugin
 * <p>
 * Root class of the Gradle plugin.  This class is responsible for registering
 * all the tasks and configuration extensions.
 *
 * @since 1.0.0
 */
class ContainerUtilsPlugin : Plugin<Project> {

  companion object {
    const val ExtensionName = "containerService"

    private val Events = arrayOf(
      TestLogEvent.PASSED,
      TestLogEvent.FAILED,
      TestLogEvent.SKIPPED,
      TestLogEvent.STANDARD_OUT,
      TestLogEvent.STANDARD_ERROR,
    )
  }

  /**
   * Applies the plugin tasks, extensions, and configuration to the Gradle
   * project.
   *
   * @param project Gradle project.
   *
   * @since 1.0.0
   */
  override fun apply(project: Project) {
    // Register Global Options
    project.extensions.create(ExtensionName, Options::class.java, project)

    // Register Tasks
    val tasks = project.tasks

    val init: (Action) -> Unit = { Action.init(it) }

    arrayOf(
      // RAML Merge
      InstallMergeRaml.TaskName to InstallMergeRaml::class,
      ExecMergeRaml.TaskName to ExecMergeRaml::class,

      // JaxRS Code Generation
      InstallRaml4JaxRS.TaskName to InstallRaml4JaxRS::class,
      UninstallRaml4JaxRS.TaskName to UninstallRaml4JaxRS::class,

      GenerateJaxRS.TaskName to GenerateJaxRS::class,
      GenerateRamlDocs.TaskName to GenerateRamlDocs::class,

      JaxRSPatchDiscriminators.TaskName to JaxRSPatchDiscriminators::class,
      JaxRSPatchEnumValue.TaskName to JaxRSPatchEnumValue::class,
      JaxRSGenerateStreams.TaskName to JaxRSGenerateStreams::class,
      JaxRSPatchJakartaImports.TaskName to JaxRSPatchJakartaImports::class,
      JaxRSPatchBoxedTypes.TaskName to JaxRSPatchBoxedTypes::class,
      JaxRSPatchFileResponses.TaskName to JaxRSPatchFileResponses::class,
      JaxRSPatchDates.TaskName to JaxRSPatchDates::class,
      JaxRSPatchResponseDelegate.TaskName to JaxRSPatchResponseDelegate::class,
      JaxRSPatchResponseTypes.TaskName to JaxRSPatchResponseTypes::class,
      JaxRSGenerateFieldNameConstants.TaskName to JaxRSGenerateFieldNameConstants::class,
      JaxRSGenerateUrlPathConstants.TaskName to JaxRSGenerateUrlPathConstants::class,

      // Docker
      DockerBuild.TaskName to DockerBuild::class,

      // Utilities
      CheckEnv.TaskName to CheckEnv::class,
    ).forEach { (name, type) ->
      tasks.register(name, type.java, init)
    }

    tasks.register(DownloadDependencies.TaskName, DownloadDependencies::class.java, DownloadDependencies::init)

    project.afterEvaluate { x -> afterEvaluate(x) }
  }

  private fun afterEvaluate(project: Project) {
    project.logger.trace("afterEvaluate(Project)")

    val opts = project.extensions.getByName(ExtensionName) as Options

    project.logger.debug("Options: {}", opts)

    setProjectProps(project, opts)
    setJarProps(project, opts)
    configureTestLogging(project)
    configureRepositories(project)
  }

  private fun setProjectProps(project: Project, opts: Options) {
    project.version = opts.service.version
    project.group = opts.service.group
  }

  private fun setJarProps(project: Project, opts: Options) {
    project.logger.debug("Configuring jar tasks.")
    val conf = opts.service

    project.tasks.getByName("jar") { jar ->
      jar as Jar

      jar.logger.debug("Applying config to jar task {}.", jar)

      // Set DuplicateStrategy
      jar.duplicatesStrategy = DuplicatesStrategy.EXCLUDE

      // Set Archive Name
      jar.archiveFileName.set("service.jar")

      // Set Manifest Attributes
      val attrs = jar.manifest.attributes

      attrs["Main-Class"] = conf.projectPackage + "." + conf.mainClassName
      attrs["Implementation-Title"] = conf.name
      attrs["Implementation-Version"] = conf.version
    }
  }

  private fun configureTestLogging(project: Project) {
    val events = Events.asList()

    project.tasks.withType(Test::class.java).forEach { test ->
      test.testLogging { tlc ->
        tlc.events.addAll(events)
        tlc.exceptionFormat = TestExceptionFormat.FULL
        tlc.showExceptions = true
        tlc.showCauses = true
        tlc.showStackTraces = true
        tlc.showStandardStreams = true
      }
      test.enableAssertions = true
      test.ignoreFailures = true
      test.useJUnitPlatform()
    }
  }

  private fun configureRepositories(project: Project) {
    project.repositories.mavenCentral()
    project.repositories.maven { mar ->
      mar.name = "GitHubPackages"
      mar.url = project.uri("https://maven.pkg.github.com/veupathdb/maven-packages")

      val creds = mar.credentials

      creds.username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
      creds.password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
    }
  }
}
