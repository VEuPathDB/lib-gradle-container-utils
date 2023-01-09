package org.veupathdb.lib.gradle.container

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.veupathdb.lib.gradle.container.config.Options
import org.veupathdb.lib.gradle.container.tasks.*
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
    const val ExtensionName = "containerBuild"

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
    project.extensions.create(ExtensionName, Options::class.java)

    // Register Tasks
    val tasks = project.tasks

    tasks.create(InstallRaml4JaxRS.TaskName, InstallRaml4JaxRS::class.java, Action::init)
    tasks.create(UninstallRaml4JaxRS.TaskName, UninstallRaml4JaxRS::class.java, Action::init)

    tasks.create(GenerateJaxRS.TaskName, GenerateJaxRS::class.java, Action::init)
    tasks.create(GenerateRamlDocs.TaskName, GenerateRamlDocs::class.java, Action::init)

    tasks.create(InstallMergeRaml.TaskName, InstallMergeRaml::class.java, Action::init)
    tasks.create(ExecMergeRaml.TaskName, ExecMergeRaml::class.java, Action::init)

    tasks.create(JaxRSPatchDiscriminators.TaskName, JaxRSPatchDiscriminators::class.java, Action::init)
    tasks.create(JaxRSPatchEnumValue.TaskName, JaxRSPatchEnumValue::class.java, Action::init)
    tasks.create(JaxRSGenerateStreams.TaskName, JaxRSGenerateStreams::class.java, Action::init)
    tasks.create(JaxRSPatchJakartaImports.TaskName, JaxRSPatchJakartaImports::class.java, Action::init)
    tasks.create(JaxRSPatchBoxedTypes.TaskName, JaxRSPatchBoxedTypes::class.java, Action::init)
    tasks.create(JaxRSPatchFileResponses.TaskName, JaxRSPatchFileResponses::class.java, Action::init)

    tasks.create(DockerBuild.TaskName, DockerBuild::class.java, Action::init)

    tasks.create(PrintPackage.TaskName, PrintPackage::class.java, Action::init)

    tasks.create(CheckEnv.TaskName, CheckEnv::class.java, Action::init)

    tasks.create(DownloadDependencies.TaskName, DownloadDependencies::class.java, DownloadDependencies::init)

    project.afterEvaluate { x -> afterEvaluate(x) }
  }

  private fun afterEvaluate(project: Project) {
    project.logger.trace("afterEvaluate(Project)")

    val opts = project.extensions.getByName(ExtensionName) as Options

    project.logger.debug("Options: {}", opts)

    setProjectProps(project, opts)
    setJarProps(project, opts)
    configureTestLogging(project)
    configureRepositories(project, opts)

    val tasks = project.tasks

      // Make sure that Raml for Jax RS is installed before attempting to generate
    // java types.

    val genJaxrs = tasks.getByName(GenerateJaxRS.TaskName)

    genJaxrs.dependsOn(
      tasks.getByName(ExecMergeRaml.TaskName),
      tasks.getByName(InstallRaml4JaxRS.TaskName)
    )

    genJaxrs.finalizedBy(
      JaxRSPatchDiscriminators.TaskName,
      JaxRSPatchEnumValue.TaskName,
      JaxRSGenerateStreams.TaskName,
      JaxRSPatchJakartaImports.TaskName,
      JaxRSPatchBoxedTypes.TaskName,
      JaxRSPatchFileResponses.TaskName,
    )

    // Register merge raml dependencies
    tasks.getByName(ExecMergeRaml.TaskName)
      .dependsOn(InstallMergeRaml.TaskName)
  }

  private fun setProjectProps(project: Project, opts: Options) {
    project.version = opts.project.version
    project.group = opts.project.group
  }

  private fun setJarProps(project: Project, opts: Options) {
    project.logger.debug("Configuring jar tasks.")
    val conf = opts.project

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

  private fun configureRepositories(project: Project, opts: Options) {
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
