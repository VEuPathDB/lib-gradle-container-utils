package org.veupathdb.lib.gradle.container.tasks.base

import org.gradle.api.DefaultTask
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Internal
import org.veupathdb.lib.gradle.container.ContainerUtilsPlugin
import org.veupathdb.lib.gradle.container.config.Options
import org.veupathdb.lib.gradle.container.config.ServiceConfiguration
import org.veupathdb.lib.gradle.container.util.Logger
import java.io.File
import java.util.Optional

/**
 * Action
 * <p>
 * Base class for all tasks in this plugin.
 *
 * @since 1.0.0
 */
abstract class Action : DefaultTask() {

  companion object {
    const val Group = "VEuPathDB"

    /**
     * Static initializer for new task instances.
     * <p>
     * This method is called by Gradle when instantiating a new {@code Action}
     * instance.
     *
     * @param action New {@code Action} instance, created by Gradle.
     *
     * @since 1.1.0
     */
    @JvmStatic
    fun init(action: Action) {
      action.register()
    }
  }

  @get:Internal
  protected val RootDir: File = project.rootDir

  @get:Internal
  protected val ProjectDir: File = project.projectDir

  @get:Internal
  protected val util by lazy { Utils(log) }

  @get:Internal
  protected val log by lazy { Logger(options::logLevel, RootDir) }

  @get:Internal
  protected val options by lazy {
    project.extensions.getByName(ContainerUtilsPlugin.ExtensionName) as Options
  }

  @get:Internal
  protected val javaPluginExtension by lazy {
    project.extensions.getByType(JavaPluginExtension::class.java)
  }


  //
  //
  // Abstract Methods
  //
  //

  /**
   * The primary execution of this Action's functionality.
   *
   * @since 1.1.0
   */
  abstract fun execute()

  /**
   * Returns a description of this Action to use when rendering Gradle help
   * text.
   *
   * @return The current Action's description.
   *
   * @since 1.1.0
   */
  @get:Internal
  abstract val pluginDescription: String

  //
  //
  // Implementation Methods
  //
  //

  /**
   * Populate this task's declared inputs for use with incremental builds.
   * <p>
   * The declared inputs are used in a manner similar to {@code make} in that
   * if these files have not changed, the task may not run (depending on
   * declared outputs).
   *
   * @param files Collection of declared inputs.
   *
   * @since 2.0.0
   */
  open fun fillIncrementalInputFiles(files: MutableCollection<File>) {
    log.open()
    files.add(File("build.gradle.kts"))
    log.close()
  }

  /**
   * Populate this task's declared outputs for use with incremental builds.
   * <p>
   * The declared outputs are used in a manner similar to {@code make} in that
   * if these files still exist and are not older than the declared input files,
   * the task will not run.
   *
   * @param files Collection of declared outputs.
   *
   * @since 2.0.0
   */
  open fun fillIncrementalOutputFiles(files: MutableCollection<File>) {
    log.open()
    // Do nothing
    log.close()
  }

  /**
   * Register configures the current Action after instantiation.
   *
   * @since 1.1.0
   */
  protected fun register() {
    // No logging, logger options are not yet loaded
    description = pluginDescription
    group = Group
    actions.add { execute() }

    // Fill Inputs and outputs for incremental builds.
    val files = HashSet<File>(10)
    fillIncrementalInputFiles(files)
    inputs.files(files.toArray())

    files.clear()
    fillIncrementalOutputFiles(files)
    outputs.files(files.toArray())
  }

  protected fun projectConfig(): ServiceConfiguration = options.service

  @Internal
  protected fun getProperty(key: String) =
    Optional.ofNullable(project.properties[key].toString())

  protected fun getEnv(key: String) = Optional.ofNullable(System.getenv(key))

  /**
   * Attempts to look up a property with the given name from the properties
   * available to Gradle.  If no such property was found, attempts to retrieve
   * an environment variable with the given env var name.
   *
   * @param propKey Name of the property to look up.
   * @param envKey  Name of the environment variable to look up.
   *
   * @return If the target Gradle project property was found, returns a
   * non-empty option wrapping that value, else if the target fallback
   * environment variable was found, returns a non-empty option wrapping that
   * value.  If neither lookup found a value, returns an empty option.
   */
  protected fun getPropOrEnv(
    propKey: String,
    envKey: String,
  ): Optional<String> = getProperty(propKey).or { getEnv(envKey) }
}
