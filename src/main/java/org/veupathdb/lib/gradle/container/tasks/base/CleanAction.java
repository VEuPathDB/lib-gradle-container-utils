package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.tasks.Internal;

import java.io.File;
import java.util.Objects;

public abstract class CleanAction extends Action {
  @Internal
  protected abstract File getTargetDirectory();

  protected abstract boolean filerPredicate(final File file);

  protected void delete(final File file) {
    Log.trace("CleanAction#delete(File)");
    Utils.deleteRecursive(file);
  }

  @Override
  protected void execute() {
    for (final var file : Objects.requireNonNull(getTargetDirectory().listFiles()))
      if (filerPredicate(file))
        delete(file);
  }
}
