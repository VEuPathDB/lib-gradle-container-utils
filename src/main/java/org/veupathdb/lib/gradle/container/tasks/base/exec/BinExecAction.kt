package org.veupathdb.lib.gradle.container.tasks.base.exec

import org.gradle.api.tasks.Internal

import java.io.File

/**
 * Bin Exec Action
 * <p>
 * Base class for build/bin dependency execution tasks.
 *
 * @since 1.1.0
 */
abstract class BinExecAction : ExecAction() {
  @Internal
  override fun getWorkDirectory(): File {
    return log.getter(File(RootDir, options.utils.binDirectory))
  }
}
