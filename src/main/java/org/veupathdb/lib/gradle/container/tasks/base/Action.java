package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.veupathdb.lib.gradle.container.ContainerUtilsPlugin;
import org.veupathdb.lib.gradle.container.config.Options;
import org.veupathdb.lib.gradle.container.config.ServiceProperties;
import org.veupathdb.lib.gradle.container.util.Logger;

import java.io.File;
import java.util.Optional;

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

  @Nullable
  private JavaPluginExtension javaPluginExtension;

  protected Action() {
    this.RootDir = getProject().getRootDir();
  }

  /**
   * Static initializer for new task instances.
   * <p>
   * This method is called by Gradle when instantiating a new {@code Action}
   * instance.
   *
   * @param action New {@code Action} instance, created by Gradle.
   */
  public static void init(@NotNull final Action action) {
    action.register();
  }

  /**
   * The primary execution of this Action's functionality.
   */
  public abstract void execute();

  /**
   * Returns a description of this Action to use when rendering Gradle help
   * text.
   *
   * @return The current Action's description.
   */
  @NotNull
  public abstract String pluginDescription();

  /**
   * Register configures the current Action after instantiation.
   */
  protected void register() {
    // No logging, logger options are not yet loaded
    setDescription(pluginDescription());
    setGroup(Group);
    getActions().add(t -> execute());
  }

  /**
   * Lazily reads and returns the contents of the project's service properties
   * file.
   * <p>
   * The properties file is only read on first call.
   *
   * @return This project's service properties.
   */
  @NotNull
  protected ServiceProperties serviceProperties() {
    log().open();

    if (svcProps != null)
      return svcProps;

    log().debug("Loading service properties from file.");

    return log().close(svcProps = util().loadServiceProperties(RootDir));
  }

  @NotNull
  @Internal
  protected Options getOptions() {
    // No logging, logger options are not yet loaded.
    if (options != null)
      return options;

    return options = (Options) getProject()
      .getExtensions()
      .getByName(ContainerUtilsPlugin.ExtensionName);
  }

  @NotNull
  @Internal
  protected JavaPluginExtension getJavaPluginExtension() {
    if (javaPluginExtension != null)
      return javaPluginExtension;

    return javaPluginExtension = getProject()
      .getExtensions()
      .getByType(JavaPluginExtension.class);
  }

  @NotNull
  protected Logger log() {
    return log == null
      ? (log = new Logger(getOptions().getLogLevel(), RootDir))
      : log;
  }

  @NotNull
  protected Utils util() {
    return util == null ? (util = new Utils(log())) : util;
  }

  @Internal
  protected @NotNull Optional<String> getProperty(@NotNull final String key) {
    return Optional.ofNullable(String.valueOf(getProject().getProperties().get(key)));
  }

  @Internal
  protected @NotNull Optional<String> getEnv(@NotNull final String key) {
    return Optional.ofNullable(System.getenv(key));
  }

  /**
   * Attempts to look up a property with the given name from the properties
   * available to Gradle.  If no such property was found, attempts to retrieve
   * an environment variable with the given env var name.
   *
   * @param propKey Name of the property to look up.
   * @param envKey  Name of the environment variable to look up.
   *
   * @return If the target Gradle project property was found, returns a
   * non-empty option wrapping that value, else if the target fallback
   * environment variable was found, returns a non-empty option wrapping that
   * value.  If neither lookup found a value, returns an empty option.
   */
  @Internal
  protected @NotNull Optional<String> getPropOrEnv(
    @NotNull final String propKey,
    @NotNull final String envKey
  ) {
    return getProperty(propKey).or(() -> getEnv(envKey));
  }
}
