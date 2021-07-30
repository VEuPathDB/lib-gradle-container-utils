package org.veupathdb.lib.gradle.container.tasks;

import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.Internal;
import org.veupathdb.lib.gradle.container.ContainerUtilsPlugin;

import java.io.File;

public class Action {
  protected final Task Task;

  protected final Logger Log;
  protected final File   RootDir;

  protected Action(final Task task) {
    this.Task    = task;
    this.Log     = task.getLogger();
    this.RootDir = task.getProject().getRootDir();
  }

  @Internal
  private ContainerUtilsPlugin.Options options;

  @Internal
  protected ContainerUtilsPlugin.Options getOptions() {
    return options == null
      ? options = (ContainerUtilsPlugin.Options) Task.getProject()
        .getExtensions()
        .getByName(ContainerUtilsPlugin.ExtensionName)
      : options;
  }
}
