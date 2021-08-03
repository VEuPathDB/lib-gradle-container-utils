package org.veupathdb.lib.gradle.container.tasks;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.tasks.base.BinCleanAction;

import java.io.File;

public class UninstallRaml4JaxRS extends BinCleanAction {
  public static final String TaskName = "ramlGenUninstall";

  @Override
  @NotNull
  protected String pluginDescription() {
    return "Uninstalls the Raml for Jax RS tooling.";
  }

  @Override
  protected boolean filerPredicate(@NotNull final File file) {
    return log().map(file, file.getName().equals(InstallRaml4JaxRS.OutputFile));
  }
}
