package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.veupathdb.lib.gradle.container.ContainerUtilsPlugin;
import org.veupathdb.lib.gradle.container.config.Options;
import org.veupathdb.lib.gradle.container.config.ServiceProperties;
import org.veupathdb.lib.gradle.container.util.Logger;

import java.io.File;

public abstract class Action extends DefaultTask {
  private static final String Group = "VEuPathDB";

  private static ServiceProperties svcProps;

  @NotNull
  protected final File RootDir;

  @Nullable
  private Utils util;

  @Nullable
  private Logger log;

  @Nullable
  private Options options;

  protected Action() {
    this.RootDir = getProject().getRootDir();
  }

  public static void init(@NotNull final Action action) {
    action.register();
  }

  protected abstract void execute();

  @NotNull
  protected abstract String pluginDescription();

  protected void register() {
    log().open("Action#register()");

    setDescription(pluginDescription());
    setGroup(Group);
    getActions().add(t -> execute());

    log().close();
  }

  @NotNull
  protected ServiceProperties serviceProperties() {
    log().open("Action#serviceProperties()");

    if (svcProps != null)
      return svcProps;

    log().debug("Loading service properties from file.");

    return log().close(svcProps = util().loadServiceProperties(RootDir));
  }

  @NotNull
  @Internal
  protected Options getOptions() {
    log().open();

    if (options != null)
      return log().close(options);

    log().debug("Reading options from gradle config.");

    return options = log().close((Options) getProject()
      .getExtensions()
      .getByName(ContainerUtilsPlugin.ExtensionName));
  }

  @NotNull
  protected Logger log() {
    return log == null ? (log = new Logger(getOptions().getLogLevel())) : log;
  }

  @NotNull
  protected Utils util() {
    return util == null ? (util = new Utils(log())) : util;
  }
}
