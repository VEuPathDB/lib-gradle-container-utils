package org.veupathdb.lib.gradle.container.tasks.jaxrs;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.tasks.base.BinCleanAction;
import org.veupathdb.lib.gradle.container.tasks.jaxrs.InstallRaml4JaxRS;

import java.io.File;

/**
 * RAML 4 Jax-RS Uninstallation
 * <p>
 * Removes all built artifacts for RAML 4 Jax-RS from the configured bin
 * directory.
 *
 * @since 1.1.0
 */
public class UninstallRaml4JaxRS extends BinCleanAction {
  public static final String TaskName = "uninstall-raml-4-jax-rs";

  @Override
  @NotNull
  public String pluginDescription() {
    return "Uninstalls the Raml for Jax RS tooling.";
  }

  @Override
  protected boolean filerPredicate(@NotNull final File file) {
    return log().map(file, file.getName().equals(InstallRaml4JaxRS.OutputFile));
  }
}
