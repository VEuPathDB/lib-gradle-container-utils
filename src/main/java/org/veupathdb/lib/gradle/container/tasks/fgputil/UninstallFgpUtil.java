package org.veupathdb.lib.gradle.container.tasks.fgputil;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.tasks.base.VendorCleanAction;

import java.io.File;

/**
 * FgpUtil Uninstallation
 * <p>
 * Removes FgpUtil libraries from the project's configured vendor directory.
 *
 * @since 1.0.0
 */
public class UninstallFgpUtil extends VendorCleanAction {

  public static final String TaskName = "uninstall-fgputil";

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
