package org.veupathdb.lib.gradle.container.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.Internal;
import org.veupathdb.lib.gradle.container.ContainerUtilsPlugin;

import java.io.File;

public class ExtTask extends DefaultTask {
  protected final Logger Log     = getLogger();
  protected final File   RootDir = getProject().getRootDir();

  @Internal
  private ContainerUtilsPlugin.Options options;
  protected ContainerUtilsPlugin.Options getOptions() {
    return options == null
      ? options = (ContainerUtilsPlugin.Options) getExtensions()
        .findByName(ContainerUtilsPlugin.ExtensionName)
      : options;
  }
}
