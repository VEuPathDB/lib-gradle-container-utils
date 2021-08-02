package org.veupathdb.lib.gradle.container.tasks.base;

import java.io.File;

public abstract class BinCleanAction extends CleanAction {
  @Override
  protected File getTargetDirectory() {
    final String tmp;
    return new File(
      RootDir,
      (tmp = getOptions().getBinDirectory()) == null ? BinBuildAction.DefaultBinDirectory : tmp
    );
  }
}
