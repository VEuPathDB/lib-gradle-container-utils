package org.veupathdb.lib.gradle.container.tasks.base

import org.gradle.api.tasks.Internal
import org.jetbrains.annotations.NotNull

import java.io.File

/**
 * Bin Clean Action
 * <p>
 * Base class for build/bin dependency clean tasks.
 *
 * @since 1.1.0
 */
abstract class BinCleanAction : CleanAction() {
  @get:Internal
  override val targetDirectory: File
    get() = log.getter(File(RootDir, options.binBuilds.binDirectory))
}
