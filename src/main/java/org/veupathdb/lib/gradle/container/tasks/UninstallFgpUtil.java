package org.veupathdb.lib.gradle.container.tasks;

import org.veupathdb.lib.gradle.container.tasks.base.VendorCleanAction;

import java.io.File;

public class UninstallFgpUtil extends VendorCleanAction {

  public static final String TaskName = "fgputilUninstall";

  @Override
  protected String pluginDescription() {
    return "Removes FgpUtil jars from the dependency directory.";
  }

  @Override
  protected boolean filerPredicate(File file) {
    return file.getName().startsWith("fgputil");
  }
}
