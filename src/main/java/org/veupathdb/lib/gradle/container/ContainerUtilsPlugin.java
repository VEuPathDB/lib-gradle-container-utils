package org.veupathdb.lib.gradle.container;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.veupathdb.lib.gradle.container.config.Options;
import org.veupathdb.lib.gradle.container.tasks.InstallFgpUtil;
import org.veupathdb.lib.gradle.container.tasks.InstallRaml4JaxRS;
import org.veupathdb.lib.gradle.container.tasks.UninstallFgpUtil;
import org.veupathdb.lib.gradle.container.tasks.UninstallRaml4JaxRS;

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
  }
}
