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
@Deprecated("Will be removed after makefile has been shifted to gradle.")
open class PrintPackage : Action() {

  companion object {
    const val TaskName = "print-package";
  }

  override fun execute() {
    println(projectConfig().projectPackage);
  }

  override val pluginDescription: String
    get() = "Print the current project's root package.";
}
