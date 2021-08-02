package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.Internal;
import org.veupathdb.lib.gradle.container.ContainerUtilsPlugin;
import org.veupathdb.lib.gradle.container.config.Options;

import java.io.File;

public abstract class Action extends DefaultTask {
  private static final String Group = "VEuPathDB";

  protected final Logger Log;
  protected final File   RootDir;

  @Internal
  private Options options;

  protected Action() {
    this.Log     = getLogger();
    this.RootDir = getProject().getRootDir();
  }

  public static void init(final Action action) {
    action.register();
  }

  protected abstract void execute();

  protected abstract String pluginDescription();

  protected void register() {
    setDescription(pluginDescription());
    setGroup(Group);
    getActions().add(t -> execute());
  }

  protected Options getOptions() {
    return options == null
      ? options = (Options) getProject()
        .getExtensions()
        .getByName(ContainerUtilsPlugin.ExtensionName)
      : options;
  }
}
