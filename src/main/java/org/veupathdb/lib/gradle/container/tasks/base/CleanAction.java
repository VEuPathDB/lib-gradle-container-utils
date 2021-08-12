package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

public abstract class CleanAction extends Action {
  @Internal
  @NotNull
  protected abstract File getTargetDirectory();

  protected abstract boolean filerPredicate(@NotNull final File file);

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
