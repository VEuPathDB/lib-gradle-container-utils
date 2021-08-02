package org.veupathdb.lib.gradle.container.tasks.base;

import java.io.File;

public abstract class VendorCleanAction extends CleanAction {

  private File targetDir;

  @Override
  protected File getTargetDirectory() {
    if (targetDir != null)
      return targetDir;

    final String tmp;
    return targetDir = new File(
      RootDir,
      (tmp = getOptions().getVendorDirectory()) == null ? VendorBuildAction.DefaultVendorDir : tmp
    );
  }
}
