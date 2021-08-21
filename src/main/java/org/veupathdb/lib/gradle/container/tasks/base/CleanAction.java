package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

/**
 * Clean Action
 * <p>
 * Base class for clean/uninstall tasks.
 *
 * @since 1.1.0
 */
public abstract class CleanAction extends Action {

  /**
   * Returns the directory from which the clean targets will be removed.
   *
   * @return The directory from which the clean targets will be removed.
   *
   * @since 1.1.0
   */
  @Internal
  @NotNull
  protected abstract File getTargetDirectory();

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
  protected abstract boolean filerPredicate(@NotNull final File file);

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
  protected void delete(@NotNull final File file) {
    log().open(file);
    util().deleteRecursive(file);
    log().close();
  }

  @Override
  public void execute() {
    log().open();
    for (final var file : Objects.requireNonNull(getTargetDirectory().listFiles()))
      if (filerPredicate(file))
        delete(file);
    log().close();
  }
}
