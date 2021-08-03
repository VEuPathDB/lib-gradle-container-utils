package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public abstract class VendorCleanAction extends CleanAction {
  @Override
  @Internal
  @NotNull
  protected File getTargetDirectory() {
    return log().getter(new File(RootDir, getOptions().getVendorDirectory()));
  }
}
