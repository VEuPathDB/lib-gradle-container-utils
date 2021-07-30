package org.veupathdb.lib.gradle.container.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.Internal;
import org.veupathdb.lib.gradle.container.ContainerUtilsPlugin;

import java.io.File;

public class Action extends DefaultTask {
  protected final Logger Log;
  protected final File   RootDir;

  protected Action() {
    this.Log     = getLogger();
    this.RootDir = getProject().getRootDir();
  }

  @Internal
  private ContainerUtilsPlugin.Options options;

  @Internal
  protected ContainerUtilsPlugin.Options getOptions() {
    return options == null
      ? options = (ContainerUtilsPlugin.Options) getProject()
        .getExtensions()
        .getByName(ContainerUtilsPlugin.ExtensionName)
      : options;
  }
}
