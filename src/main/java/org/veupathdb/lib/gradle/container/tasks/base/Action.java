package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.DefaultTask;
import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.ContainerUtilsPlugin;
import org.veupathdb.lib.gradle.container.config.Options;
import org.veupathdb.lib.gradle.container.config.ServiceProperties;
import org.veupathdb.lib.gradle.container.util.Logger;

import java.io.File;

public abstract class Action extends DefaultTask {
  private static final String Group = "VEuPathDB";

  private static ServiceProperties svcProps;

  @NotNull
  protected final Logger Log;

  @NotNull
  protected final File RootDir;

  @NotNull
  protected final Options Options;

  @NotNull
  protected final Utils Util;

  protected Action() {
    this.RootDir = getProject().getRootDir();
    this.Options = (Options) getProject()
      .getExtensions()
      .getByName(ContainerUtilsPlugin.ExtensionName);
    this.Log     = new Logger(Options.getLogLevel());
    this.Util    = new Utils(this.Log);

    System.out.println(Options);
  }

  public static void init(@NotNull final Action action) {
    action.register();
  }

  protected abstract void execute();

  @NotNull
  protected abstract String pluginDescription();

  protected void register() {
    Log.open("Action#register()");

    setDescription(pluginDescription());
    setGroup(Group);
    getActions().add(t -> execute());

    Log.close();
  }

  @NotNull
  protected ServiceProperties serviceProperties() {
    Log.open("Action#serviceProperties()");

    if (svcProps != null)
      return svcProps;

    Log.debug("Loading service properties from file.");

    return Log.close(svcProps = Util.loadServiceProperties(RootDir));
  }
}
