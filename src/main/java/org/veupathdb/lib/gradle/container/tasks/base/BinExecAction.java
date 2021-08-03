package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public abstract class BinExecAction extends ExecAction {
  @Override
  @Internal
  @NotNull
  protected File getWorkDirectory() {
    return log().getter(new File(RootDir, getOptions().getBinDirectory()));
  }
}
