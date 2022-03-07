package org.veupathdb.lib.gradle.container.config

import org.gradle.api.Action
import org.veupathdb.lib.gradle.container.tasks.base.Defaults.DefaultApiDocsRoot
import org.veupathdb.lib.gradle.container.tasks.docker.DockerConfig
import org.veupathdb.lib.gradle.container.tasks.base.exec.ExecConfiguration
import org.veupathdb.lib.gradle.container.tasks.fgputil.FgpUtilConfiguration
import org.veupathdb.lib.gradle.container.tasks.base.build.GlobalBinBuildConfiguration
import org.veupathdb.lib.gradle.container.tasks.base.build.GlobalBuildConfiguration
import org.veupathdb.lib.gradle.container.tasks.base.build.GlobalVendorBuildConfiguration
import org.veupathdb.lib.gradle.container.tasks.jaxrs.Raml4JaxRSBuildConfig
import org.veupathdb.lib.gradle.container.tasks.raml.GenRamlConfig
import org.veupathdb.lib.gradle.container.util.Logger


/**
 * Gradle Plugin Extension
 * <p>
 * Allows the configuration of the various tasks in this plugin as well as base
 * options for building the project.
 *
 * @since 1.0.0
 */
@Suppress("unused")
open class Options {

  val project          = ProjectConfiguration()
  val fgputil          = FgpUtilConfiguration()
  val binBuilds        = GlobalBinBuildConfiguration()
  val vendorBuilds     = GlobalVendorBuildConfiguration()
  val raml4jaxrs       = Raml4JaxRSBuildConfig()
  val generateJaxRS    = GenRamlConfig()
  val generateRamlDocs = GenRamlConfig()
  val docker           = DockerConfig()

  var rootApiDefinition = DefaultApiDocsRoot

  var logLevel = Logger.Level.Info

  /**
   * Modify the service project's settings.
   *
   * @param configure Action that will be called with a project configuration
   *                  instance.
   */
  fun project(configure: Action<in ProjectConfiguration>) =
    configure.execute(project)

  /**
   * Modify the service project's FgpUtil dependency settings.
   *
   * @param configure Action that will be called with the FgpUtil configuration.
   */
  fun fgputil(configure: Action<in FgpUtilConfiguration>) =
    configure.execute(fgputil)

  /**
   * Modify the service project's build dependency build configuration.
   *
   * @param configure Action that will be called with the build dependency
   *                  config.
   */
  fun binBuilds(configure: Action<in GlobalBuildConfiguration>) =
    configure.execute(binBuilds)

  /**
   * Modify the service project's vendor dependency build configuration.
   *
   * @param configure Action that will be called with the vendor dependency
   *                  config.
   */
  fun vendorBuilds(configure: Action<in GlobalVendorBuildConfiguration>) =
    configure.execute(vendorBuilds)

  /**
   * Modify the service project's RAML 4 Jax-RS code generator build
   * configuration.
   *
   * @param configure Action that will be called with the RAML 4 Jax-RS build
   *                  config.
   */
  fun raml4jaxrs(configure: Action<in Raml4JaxRSBuildConfig>) =
    configure.execute(raml4jaxrs)

  /**
   * Modify the service project's Jax-RS code generation configuration.
   *
   * @param configure Action that will be called with the service project's
   *                  Jax-RS code generation config.
   */
  fun generateJaxRS(configure: Action<in ExecConfiguration>) =
    configure.execute(generateJaxRS)

  /**
   * Modify the service project's API documentation generation configuration.
   *
   * @param configure Action that will be called with the service project's
   *                  API doc generation config.
   */
  fun generateRamlDocs(configure: Action<in GenRamlConfig>) =
    configure.execute(generateRamlDocs)

  /**
   * Modify the service project's Docker build configuration.
   *
   * @param configure Action that will be called with the service project's
   *                  Docker build config.
   */
  fun docker(configure: Action<in DockerConfig>) = configure.execute(docker)

  override fun toString() =
    "Options{" +
      "rootApiDefinition='" + rootApiDefinition + '\'' +
      ", logLevel=" + logLevel +
      ", project=" + project +
      ", fgputil=" + fgputil +
      ", globalBinConfig=" + binBuilds +
      ", globalVendorConfig=" + vendorBuilds +
      ", raml4jaxrs=" + raml4jaxrs +
      ", generateJaxRS=" + generateJaxRS +
      ", generateRamlDocs=" + generateRamlDocs +
      ", docker=" + docker +
      '}'
}
