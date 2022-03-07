package org.veupathdb.lib.gradle.container.tasks.jaxrs

import org.veupathdb.lib.gradle.container.tasks.base.BinCleanAction
import java.io.File

/**
 * RAML 4 Jax-RS Uninstallation
 * <p>
 * Removes all built artifacts for RAML 4 Jax-RS from the configured bin
 * directory.
 *
 * @since 1.1.0
 */
open class UninstallRaml4JaxRS : BinCleanAction() {

  companion object {
    const val TaskName = "uninstall-raml-4-jax-rs"
  }

  override val pluginDescription: String
    get() = "Uninstalls the Raml for Jax RS tooling."

  override fun filerPredicate(file: File): Boolean {
    return log.map(file, file.name.equals(InstallRaml4JaxRS.OutputFile))
  }
}
