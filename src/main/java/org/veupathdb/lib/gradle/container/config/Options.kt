package org.veupathdb.lib.gradle.container.config

import org.gradle.api.Action
import org.gradle.api.Project
import org.veupathdb.lib.gradle.container.tasks.base.ExternalUtilsConfig
import org.veupathdb.lib.gradle.container.tasks.docker.DockerConfig
import org.veupathdb.lib.gradle.container.tasks.raml.RamlConfig
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
open class Options(protected val project: Project) {

  val service = ServiceConfiguration()
  val utils   = ExternalUtilsConfig()
  val raml    = RamlConfig(project)
  val docker  = DockerConfig()

  var logLevel = Logger.Level.Info

  /**
   * Modify the service project's settings.
   *
   * @param configure Action that will be called with a [ServiceConfiguration]
   * instance.
   */
  fun service(configure: Action<in ServiceConfiguration>) = configure.execute(service)

  /**
   * Modify the service project's external tool configuration.
   *
   * @param configure Action that will be called with the external tool config.
   */
  fun utils(configure: Action<in ExternalUtilsConfig>) = configure.execute(utils)

  /**
   * Modify the service project's Docker build configuration.
   *
   * @param configure Action that will be called with the service project's
   * Docker build config.
   */
  fun docker(configure: Action<in DockerConfig>) = configure.execute(docker)

  /**
   * Modify the service project's RAML build configuration.
   *
   * @param configure Action that will be called with the service project's
   * RAML build config.
   */
  fun raml(configure: Action<in RamlConfig>) = configure.execute(raml)
}
