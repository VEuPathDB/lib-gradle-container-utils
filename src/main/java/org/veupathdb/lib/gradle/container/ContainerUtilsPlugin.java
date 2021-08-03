package org.veupathdb.lib.gradle.container;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.veupathdb.lib.gradle.container.config.Options;
import org.veupathdb.lib.gradle.container.tasks.*;

public class ContainerUtilsPlugin implements Plugin<Project> {
  public static final String ExtensionName = "containerBuild";

  public void apply(final Project project) {
    // Register Global Options
    project.getExtensions().create(ExtensionName, Options.class);

    // Register Tasks
    final var tasks = project.getTasks();

    tasks.create(InstallFgpUtil.TaskName, InstallFgpUtil.class, InstallFgpUtil::init);
    tasks.create(UninstallFgpUtil.TaskName, UninstallFgpUtil.class, UninstallFgpUtil::init);

    tasks.create(InstallRaml4JaxRS.TaskName, InstallRaml4JaxRS.class, InstallRaml4JaxRS::init);
    tasks.create(UninstallRaml4JaxRS.TaskName, UninstallRaml4JaxRS.class, UninstallRaml4JaxRS::init);

    tasks.create(GenerateJaxRS.TaskName, GenerateJaxRS.class, GenerateJaxRS::init);
    tasks.create(GenerateRamlDocs.TaskName, GenerateRamlDocs.class, GenerateRamlDocs::init);
  }
}
