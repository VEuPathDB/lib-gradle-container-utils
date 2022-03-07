package org.veupathdb.lib.gradle.container.tasks.base.build

import org.gradle.api.tasks.Internal

import java.io.File

/**
 * Bin Build Action
 * <p>
 * Base class for build/bin dependency build tasks.
 *
 * @since 1.1.0
 */
abstract class BinBuildAction : BuildAction() {

  /**
   * Returns a reference to the output bin directory.
   * <p>
   * This method is the same as {@link #getDependencyRoot()} just with a more
   * descriptive name.
   *
   * @return A reference to the output bin directory.
   *
   * @since 1.1.0
   */
  @Internal
  protected fun getBinRoot(): File =
    log.getter(File(RootDir, globalBuildConfiguration().binDirectory))

  @Internal
  override fun getDependencyRoot() = getBinRoot()

  override fun globalBuildConfiguration() = options.binBuilds
}
