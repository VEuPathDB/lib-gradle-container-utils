package org.veupathdb.lib.gradle.container;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.veupathdb.lib.gradle.container.tasks.FgpUtil;
import org.veupathdb.lib.gradle.container.tasks.Git;

import java.io.File;

public class ContainerUtilsPlugin implements Plugin<Project> {
  public void apply(Project project) {
    project.getExtensions().create(FgpUtil.ExtensionName, Git.GitExtension.class);

    // Register a task
    project.getTasks().register("install-fgputil", FgpUtil::execute);
  }
}
