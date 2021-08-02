package org.veupathdb.lib.gradle.container.tasks.base;


import org.gradle.api.tasks.Internal;

import java.io.File;

public abstract class BinBuildAction extends BuildAction {
  private File binRoot;

  @Internal
  protected File getBinRoot() {
    return binRoot == null
      ? (binRoot = new File(getProject().getRootDir(), getConfiguredBinPath()))
      : binRoot;
  }

  @Override
  @Internal
  protected File getDependencyRoot() {
    return getBinRoot();
  }

  @Internal
  protected String getConfiguredBinPath() {
    Log.trace("BinAction#getConfiguredBinPath()");

    final String tmp;

    return (tmp = getOptions().getBinDirectory()) == null ? Defaults.DefaultBinDirectory : tmp;
  }
}
