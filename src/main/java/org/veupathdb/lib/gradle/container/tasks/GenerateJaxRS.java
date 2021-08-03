package org.veupathdb.lib.gradle.container.tasks;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.tasks.base.BinExecAction;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class GenerateJaxRS extends BinExecAction {
  public static final String TaskName = "generate-jaxrs";

  @Override
  @NotNull
  protected String pluginDescription() {
    return "Generates JaxRS Java code based on the project's RAML API spec.";
  }

  @Override
  @NotNull
  protected String getCommandName() {
    return "java";
  }

  @Override
  protected void appendArguments(@NotNull final List<String> args) {
    Log.trace("GenerateJaxRS#appendArguments(%s)", args);

    final var props = serviceProperties();
    final var base  = props.appPackageRoot() + "." + props.appPackageService() + ".generated.";

    args.addAll(Arrays.asList(
      "-jar",                  InstallRaml4JaxRS.OutputFile,
      new File(RootDir.getParentFile(), Options.getApiDocRoot()).getName(),
      "--directory",           "../src/main/java",
      "--generate-types-with", "jackson",
      "--model-package",       base + "model",
      "--resource-package",    base + "resources",
      "--support-package",     base + "support"
    ));
  }
}
