package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.ContainerUtilsPlugin;
import org.veupathdb.lib.gradle.container.config.Options;
import org.veupathdb.lib.gradle.container.config.ServiceProperties;

import java.io.File;
import java.io.IOException;

public abstract class Action extends DefaultTask {
  private static final String Group = "VEuPathDB";

  private static ServiceProperties svcProps;

  @NotNull
  protected final Logger Log;

  @NotNull
  protected final File RootDir;

  @NotNull
  protected final Options Options;

  protected Action() {
    this.Log     = getLogger();
    this.RootDir = getProject().getRootDir();
    this.Options = (Options) getProject()
      .getExtensions()
      .getByName(ContainerUtilsPlugin.ExtensionName);
  }

  public static void init(@NotNull final Action action) {
    action.register();
  }

  protected abstract void execute();

  @NotNull
  protected abstract String pluginDescription();

  protected void register() {
    setDescription(pluginDescription());
    setGroup(Group);
    getActions().add(t -> execute());
  }

  @NotNull
  protected ServiceProperties serviceProperties() {
    Log.trace("Action#serviceProperties()");

    if (svcProps != null)
      return svcProps;

    try {
      Log.debug("Loading service properties from file.");
      return svcProps = Utils.loadServiceProperties(RootDir);
    } catch (IOException e) {
      Log.error("Failed to read service properties file.");
      throw new RuntimeException("Failed to read service properties file.", e);
    }
  }
}
