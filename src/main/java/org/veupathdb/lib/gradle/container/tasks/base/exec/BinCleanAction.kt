package org.veupathdb.lib.gradle.container.tasks.base.exec

import org.gradle.api.tasks.Internal
import org.veupathdb.lib.gradle.container.tasks.base.CleanAction

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
    get() = log.getter(File(RootDir, options.utils.binDirectory))
}
