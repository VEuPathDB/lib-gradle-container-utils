package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public abstract class VendorBuildAction extends BuildAction {
  @Override
  @Internal
  @NotNull
  protected File getDependencyRoot() {
    return log().getter(new File(RootDir, getOptions().getVendorDirectory()));
  }
}
