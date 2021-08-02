package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.tasks.Internal;

import java.io.File;

public abstract class VendorBuildAction extends BuildAction {
  private File vendorRoot;

  @Override
  @Internal
  protected File getDependencyRoot() {
    if (vendorRoot != null)
      return vendorRoot;

    final String tmp;
    return vendorRoot = new File(RootDir, (tmp = Options.getVendorDirectory()) == null
      ? Defaults.DefaultVendorDirectory
      : tmp
    );
  }
}
