package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.Internal;
import org.veupathdb.lib.gradle.container.ContainerUtilsPlugin;
import org.veupathdb.lib.gradle.container.config.Options;
import org.veupathdb.lib.gradle.container.config.ServiceProperties;

import java.io.File;
import java.io.IOException;

public abstract class Action extends DefaultTask {
  private static final String Group = "VEuPathDB";

  private static ServiceProperties svcProps;

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
