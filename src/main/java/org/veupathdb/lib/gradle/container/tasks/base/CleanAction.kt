package org.veupathdb.lib.gradle.container.tasks.base

import org.gradle.api.tasks.Internal

import java.io.File

/**
 * Clean Action
 * <p>
 * Base class for clean/uninstall tasks.
 *
 * @since 1.1.0
 */
abstract class CleanAction : Action() {

  /**
   * Returns the directory from which the clean targets will be removed.
   *
   * @return The directory from which the clean targets will be removed.
   *
   * @since 1.1.0
   */
  @get:Internal
  protected abstract val targetDirectory: File

  /**
   * File predicate that tests whether the given file should be removed in the
   * clean.
   *
   * @param file File to test.
   *
   * @return {@code true} if the file should be deleted, else {@code false}.
   *
   * @since 1.1.0
   */
  protected abstract fun filerPredicate(file: File): Boolean

  /**
   * Deletes the given file or directory.
   * <p>
   * If the input is a directory, this method will recursively remove the
   * directory and it's contents.
   *
   * @param file File or directory to delete.
   *
   * @since 1.1.0
   */
  protected fun delete(file: File) {
    log.open(file)
    util.deleteRecursive(file)
    log.close()
  }

  override fun execute() {
    log.open()
    for (file in targetDirectory.listFiles()!!)
      if (filerPredicate(file))
        delete(file)
    log.close()
  }
}
