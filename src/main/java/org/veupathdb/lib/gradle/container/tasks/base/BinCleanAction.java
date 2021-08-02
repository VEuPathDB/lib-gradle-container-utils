package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.tasks.Internal;

import java.io.File;

public abstract class BinCleanAction extends CleanAction {
  @Override
  @Internal
  protected File getTargetDirectory() {
    final String tmp;
    return new File(
      RootDir,
      (tmp = getOptions().getBinDirectory()) == null ? BinBuildAction.DefaultBinDirectory : tmp
    );
  }
}
