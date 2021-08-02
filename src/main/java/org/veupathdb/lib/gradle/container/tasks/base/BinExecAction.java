package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.tasks.Internal;

import java.io.File;

public abstract class BinExecAction extends ExecAction {
  @Override
  @Internal
  protected File getWorkDirectory() {
    final String tmp;
    return new File(
      RootDir,
      (tmp = getOptions().getBinDirectory()) == null ? Defaults.DefaultBinDirectory : tmp
    );
  }
}
