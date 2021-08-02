package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.tasks.Internal;

import java.io.File;

public abstract class VendorCleanAction extends CleanAction {
  @Override
  @Internal
  protected File getTargetDirectory() {
    final String tmp;
    return new File(
      RootDir,
      (tmp = Options.getVendorDirectory()) == null ? Defaults.DefaultVendorDirectory : tmp
    );
  }
}
