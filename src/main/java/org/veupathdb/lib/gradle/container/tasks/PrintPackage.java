package org.veupathdb.lib.gradle.container.tasks;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.tasks.base.Action;

/**
 * Print Package
 * <p>
 * A makefile support task used to fetch the package name for use in util shell
 * scripts.
 *
 * @since 2.0.0
 *
 * @deprecated This will be removed after the makefile has been trimmed down to
 * use Gradle for all tasks.
 */
@Deprecated
public class PrintPackage extends Action {
  public static final String TaskName = "print-package";

  @Override
  public void execute() {
    System.out.println(projectConfig().getProjectPackage());
  }

  @Override
  public @NotNull String pluginDescription() {
    return "Print the current project's root package.";
  }
}
