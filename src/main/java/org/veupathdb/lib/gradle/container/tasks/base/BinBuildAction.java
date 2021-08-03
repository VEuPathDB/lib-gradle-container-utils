package org.veupathdb.lib.gradle.container.tasks.base;


import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public abstract class BinBuildAction extends BuildAction {
  @Internal
  @NotNull
  protected File getBinRoot() {
    return log().getter(new File(getProject().getRootDir(), getOptions().getBinDirectory()));
  }

  @Override
  @Internal
  @NotNull
  protected File getDependencyRoot() {
    return getBinRoot();
  }
}
