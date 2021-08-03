package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public abstract class BinCleanAction extends CleanAction {
  @Override
  @Internal
  @NotNull
  protected File getTargetDirectory() {
    return log().getter(new File(RootDir, getOptions().getBinDirectory()));
  }
}
