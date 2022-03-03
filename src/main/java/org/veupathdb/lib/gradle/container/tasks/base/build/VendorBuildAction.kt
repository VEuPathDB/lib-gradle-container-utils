package org.veupathdb.lib.gradle.container.tasks.base.build

import org.gradle.api.tasks.Internal
import org.jetbrains.annotations.NotNull

import java.io.File

/**
 * Vendor Build Action
 * <p>
 * Base class for vendored dependency build tasks.
 *
 * @since 1.1.0
 */
abstract class VendorBuildAction : BuildAction() {
  @Internal
  override fun getDependencyRoot(): File =
    log.getter(File(RootDir, globalBuildConfiguration().vendorDirectory))

  override fun globalBuildConfiguration() = options.vendorBuilds
}
