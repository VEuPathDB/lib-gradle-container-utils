package org.veupathdb.lib.gradle.container.tasks.base.build

import org.gradle.api.tasks.Internal
import org.veupathdb.lib.gradle.container.tasks.base.Action

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption

/**
 * Build Action
 * <p>
 * Base class for dependency building tasks.
 *
 * @since 1.1.0
 */
abstract class BuildAction : Action() {

  companion object {
    /**
     * State where the current task's target has not yet been built, or has
     * been completely wiped out, meaning a new download, build, and install is
     * needed.
     *
     * @since 1.1.0
     */
    protected const val StateNew: Byte    = 1

    /**
     * State where the current task's target has been built, but the configured
     * version of the dependency has been updated in the Gradle build file.
     *
     * @since 1.1.0
     */
    protected const val StateUpdate: Byte = 2

    /**
     * State where the current task's target has been built and is at the version
     * currently configured in the Gradle build file.
     *
     * @since 1.1.0
     */
    protected const val StateSkip: Byte   = 127
  }

  /**
   * Lazily populated root build directory.
   * <p>
   * Typically the bin or vendor directory.
   *
   * @since 1.1.0
   */
  private var buildDirectory: File? = null

  //
  //
  // Abstract Methods
  //
  //

  /**
   * Returns the name of the dependency this {@code BuildAction} will build.
   *
   * @since 1.1.0
   */
  @Internal
  protected abstract fun getDependencyName(): String

  /**
   * Returns a {@code File} instance representing a lock file for the built
   * dependency.
   * <p>
   * <b>WARNING</b>: This method makes no guarantee that the lock file actually
   * exists.
   *
   * @since 1.1.0
   */
  @Internal
  protected abstract fun getLockFile(): File

  /**
   * Returns the root directory where dependencies will be located once built.
   * <p>
   * <b>WARNING</b>: This method makes no guarantee that the dependency root
   * directory actually exists.
   *
   * @since 1.1.0
   */
  @Internal
  protected abstract fun getDependencyRoot(): File

  /**
   * Build dependency project and install it in the target location.
   *
   * @since 1.1.0
   */
  protected abstract fun install()

  /**
   * Performs the action or actions necessary to retrieve the dependency's
   * source code.
   *
   * @return The directory containing the unpacked source files required to
   * build the dependency.
   *
   * @since 1.1.0
   */
  protected abstract fun download(): File

  /**
   * Removes all artifacts generated by the dependency install.  Additionally,
   * deletes the lock file for the dependency.
   *
   * @since 1.1.0
   */
  protected abstract fun clean()

  /**
   * Return the build configuration for the specific build.
   *
   * @return the build configuration for the specific build.
   *
   * @since 2.0.0
   */
  protected abstract fun buildConfiguration(): BuildConfiguration

  /**
   * Return the global build configuration.
   *
   * @return the global build configuration.
   *
   * @since 2.0.0
   */
  protected abstract fun globalBuildConfiguration(): GlobalBuildConfiguration

  //
  //
  // Instance Methods
  //
  //

  override fun fillIncrementalOutputFiles(files: MutableCollection<File>) {
    super.fillIncrementalOutputFiles(files)
    files.add(getLockFile())
  }

  /**
   * Returns the target directory for the built artifacts from this task.
   * <p>
   * This will typically be the bin or vendor directory.
   *
   * @return The target directory for the built artifacts from this task.
   *
   * @since 1.1.0
   */
  @Internal
  protected fun getBuildTargetDirectory(): File {
    log.open()

    if (buildDirectory == null)
      throw IllegalStateException("Cannot get build directory before it's been created.")

    return log.close(buildDirectory!!)
  }

  override fun execute() {
    log.open()

    log.info("Checking {}", this::getDependencyName)

    val state = determineState()
    when (state) {
      StateNew -> {
        log.info("Not Found. Installing.")
      createBuildRootIfNotExists()
    }
      StateUpdate -> {
        log.info("Version change detected. Updating.")
      clean()
    }
      StateSkip -> {
        log.info("Already up to date. Skipping.")
      log.close()
      return
    }
      else -> {
        log.error("Unrecognized state {}", state)
      throw RuntimeException("Unrecognized state " + state)
    }
    }

    buildDirectory = download()
    install()
    writeLockFile()
    postBuildCleanup()

    log.close()
  }

  /**
   * Returns the version of the dependency built in the last full run of this
   * task as recorded in the build's lock file.
   *
   * @return Previously built version of this dependency.
   *
   * @since 1.1.0
   */
  @Internal
  protected fun getLockVersion(): String {
    return log.getter(util.readFile(getLockFile()))
  }

  /**
   * Deletes the build directory for the installed dependency.
   *
   * @since 1.1.0
   */
  protected fun postBuildCleanup() {
    log.open()
    log.debug("Beginning post build cleanup.")

    util.deleteRecursive(getBuildTargetDirectory())

    log.close()
  }

  /**
   * Determines what action needs to be taken (if any) for the current
   * installation.
   *
   * @return One of {@link #StateNew}, {@link #StateUpdate}, or
   * {@link #StateSkip}.
   *
   * @since 1.1.0
   */
  protected fun determineState(): Byte {
    log.open()

    if (!getDependencyRoot().exists() || !getLockFile().exists())
      return log.close(StateNew)

    return log.close(
      if (getLockVersion() == buildConfiguration().targetVersion)
        StateSkip else StateUpdate)
  }

  /**
   * Writes the just built dependency version to the task's target lock file.
   *
   * @since 1.1.0
   */
  protected fun writeLockFile() {
    log.open()

    try {
      Files.writeString(
        getLockFile().toPath(),
        buildConfiguration().targetVersion,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING
      )
    } catch (e: Exception) {
      log.error("Failed to write lock file " + getLockFile())
      throw RuntimeException("Failed to write lock file " + getLockFile(), e)
    }

    log.close()
  }

  /**
   * Creates the build root (see {@link #getDependencyRoot()}) if it does not
   * already exist.
   *
   * @see #getDependencyRoot()
   *
   * @since 1.1.0
   */
  protected fun createBuildRootIfNotExists() {
    log.open()

    val dir = getDependencyRoot()

    if (!dir.exists() && !dir.mkdirs()) {
      log.error("Failed to create build root $dir")
      throw RuntimeException("Failed to create build root $dir")
    }

    log.close()
  }

}
