package org.veupathdb.lib.gradle.container.tasks.base

import org.gradle.api.tasks.Internal

import java.io.File

/**
 * Vendor Clean Action
 * <p>
 * Base class for vendored dependency clean tasks.
 *
 * @since 1.1.0
 */
abstract class VendorCleanAction : CleanAction() {

  @get:Internal
  override val targetDirectory: File
    get() = log.getter(File(RootDir, options.vendorBuilds.vendorDirectory))
}
