package org.veupathdb.lib.gradle.container.tasks;

import org.veupathdb.lib.gradle.container.tasks.base.BinExecAction;

import java.util.Arrays;
import java.util.List;

public class GenerateJaxRS extends BinExecAction {
  public static final String TaskName = "generate-jaxrs";

  @Override
  protected String pluginDescription() {
    return "Generates JaxRS Java code based on the project's RAML API spec.";
  }

  @Override
  protected String getCommandName() {
    return "java";
  }

  @Override
  protected void appendArguments(final List<String> args) {
    final var props = serviceProperties();
    final var base  = props.appPackageRoot() + "." + props.appPackageService() + ".generated.";

    args.addAll(Arrays.asList(
      "-jar",                  InstallRaml4JaxRS.OutputFile,
      "--directory",           "src/main/java",
      "--generate-types-with", "jackson",
      "--model-package",       base + "model",
      "--resource-package",    base + "resources",
      "--support-package",     base + "support"
    ));
  }
}