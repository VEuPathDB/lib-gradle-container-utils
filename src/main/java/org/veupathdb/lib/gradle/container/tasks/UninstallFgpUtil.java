package org.veupathdb.lib.gradle.container.tasks;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.tasks.base.VendorCleanAction;

import java.io.File;

public class UninstallFgpUtil extends VendorCleanAction {

  public static final String TaskName = "fgputilUninstall";

  @Override
  @NotNull
  public String pluginDescription() {
    return "Removes FgpUtil jars from the dependency directory.";
  }

  @Override
  protected boolean filerPredicate(@NotNull final File file) {
    return log().map(file, file.getName().startsWith("fgputil"));
  }
}
