package org.veupathdb.lib.gradle.container.tasks.fgputil

import org.jetbrains.annotations.NotNull
import org.veupathdb.lib.gradle.container.tasks.base.VendorCleanAction

import java.io.File

/**
 * FgpUtil Uninstallation
 * <p>
 * Removes FgpUtil libraries from the project's configured vendor directory.
 *
 * @since 1.0.0
 */
open class UninstallFgpUtil : VendorCleanAction() {

  companion object {
    const val TaskName = "uninstall-fgputil"
  }

  override val pluginDescription: String
    get() {
      return "Removes FgpUtil jars from the dependency directory."
    }

  override fun filerPredicate(file: File): Boolean {
    return log.map(file, file.name.startsWith("fgputil"))
  }
}
