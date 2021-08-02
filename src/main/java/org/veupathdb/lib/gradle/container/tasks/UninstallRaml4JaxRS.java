package org.veupathdb.lib.gradle.container.tasks;

import org.veupathdb.lib.gradle.container.tasks.base.BinCleanAction;

import java.io.File;

public class UninstallRaml4JaxRS extends BinCleanAction {
  public static final String TaskName = "ramlGenUninstall";

  @Override
  protected String pluginDescription() {
    return "Uninstalls the Raml for Jax RS tooling.";
  }

  @Override
  protected boolean filerPredicate(File file) {
    return file.getName().equals(InstallRaml4JaxRS.OutputFile);
  }
}
