package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.tasks.Internal;

import java.io.File;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Stack;

public abstract class CleanAction extends Action {
  @Internal
  protected abstract File getTargetDirectory();

  protected abstract boolean filerPredicate(final File file);

  protected void delete(final File file) {
    Log.trace("CleanAction#delete(File)");

    if (file.isDirectory()) {
      deleteRecursive(file);
    } else if (!file.delete()) {
      Log.error("Failed to delete file " + file);
      throw new RuntimeException("Failed to delete file " + file);
    }
  }

  @Override
  protected void execute() {
    for (final var file : Objects.requireNonNull(getTargetDirectory().listFiles())) {
      if (filerPredicate(file)) {
        delete(file);
      }
    }
  }

  private void deleteRecursive(final File dir) {
    Log.trace("CleanAction#deleteRecursive(File)");

    final var queue1 = new LinkedList<File>();
    final var queue2 = new Stack<File>();

    File file;

    while (queue1.isEmpty()) {
      file = queue1.pop();

      Log.debug("Running recursive delete in {}", file);

      if (file.isDirectory()) {
        queue1.push(file);
        queue2.push(file);
      } else {
        Log.debug("Deleting file {}", file);
        if (!file.delete()) {
          Log.error("Failed to delete file " + file);
          throw new RuntimeException("Failed to delete file " + file);
        }
      }
    }

    while (!queue2.isEmpty()) {
      file = queue2.pop();

      Log.debug("Deleting directory {}", file);

      if (!file.delete()) {
        Log.error("Failed to delete file " + file);
        throw new RuntimeException("Failed to delete file " + file);
      }
    }

  }
}
