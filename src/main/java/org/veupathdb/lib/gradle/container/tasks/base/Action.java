package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.veupathdb.lib.gradle.container.ContainerUtilsPlugin;
import org.veupathdb.lib.gradle.container.config.Options;
import org.veupathdb.lib.gradle.container.config.ProjectConfiguration;
import org.veupathdb.lib.gradle.container.util.Logger;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

/**
 * Action
 * <p>
 * Base class for all tasks in this plugin.
 *
 * @since 1.0.0
 */
public abstract class Action extends DefaultTask {
  private static final String Group = "VEuPathDB";

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
   *
   * @since 1.1.0
   */
  public static void init(@NotNull final Action action) {
    action.register();
  }

  //
  //
  // Abstract Methods
  //
  //

  /**
   * The primary execution of this Action's functionality.
   *
   * @since 1.1.0
   */
  public abstract void execute();

  /**
   * Returns a description of this Action to use when rendering Gradle help
   * text.
   *
   * @return The current Action's description.
   *
   * @since 1.1.0
   */
  @NotNull
  public abstract String pluginDescription();

  //
  //
  // Implementation Methods
  //
  //


  /**
   * Populate this task's declared inputs for use with incremental builds.
   * <p>
   * The declared inputs are used in a manner similar to {@code make} in that
   * if these files have not changed, the task may not run (depending on
   * declared outputs).
   *
   * @param files Collection of declared inputs.
   *
   * @since 2.0.0
   */
  public void fillIncrementalInputFiles(@NotNull Collection<File> files) {
    log().open();
    files.add(new File("build.gradle.kts"));
    log().close();
  }

  /**
   * Populate this task's declared outputs for use with incremental builds.
   * <p>
   * The declared outputs are used in a manner similar to {@code make} in that
   * if these files still exist and are not older than the declared input files,
   * the task will not run.
   *
   * @param files Collection of declared outputs.
   *
   * @since 2.0.0
   */
  public void fillIncrementalOutputFiles(@NotNull Collection<File> files) {
    log().open();
    // Do nothing
    log().close();
  }

  /**
   * Register configures the current Action after instantiation.
   *
   * @since 1.1.0
   */
  protected void register() {
    // No logging, logger options are not yet loaded
    setDescription(pluginDescription());
    setGroup(Group);
    getActions().add(t -> execute());

    // Fill Inputs and outputs for incremental builds.
    final var files = new HashSet<File>(10);
    fillIncrementalInputFiles(files);
    getInputs().files(files.toArray());

    files.clear();
    fillIncrementalOutputFiles(files);
    getOutputs().files(files.toArray());
  }

  @NotNull
  protected ProjectConfiguration projectConfig() {
    return getOptions().getProjectConfig();
  }

  /**
   * Returns the options configured for this plugin.
   * <p>
   * Options are lazily loaded from the global project configuration on first
   * call.
   *
   * @return The options configured for this plugin.
   *
   * @since 1.0.0
   */
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

  /**
   * Returns the logger for this plugin.
   *
   * @return This plugin's logger.
   *
   * @since 1.1.0
   */
  @NotNull
  protected Logger log() {
    return log == null
      ? (log = new Logger(getOptions().getLogLevel(), RootDir))
      : log;
  }

  /**
   * Returns an instance of the plugin utils type.
   *
   * @return An instance of the plugin utils type.
   *
   * @since 1.1.0
   */
  @NotNull
  protected Utils util() {
    return util == null ? (util = new Utils(log())) : util;
  }

  @Internal
  protected @NotNull Optional<String> getProperty(@NotNull final String key) {
    return Optional.ofNullable(String.valueOf(getProject().getProperties().get(key)));
  }

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
  protected @NotNull Optional<String> getPropOrEnv(
    @NotNull final String propKey,
    @NotNull final String envKey
  ) {
    return getProperty(propKey).or(() -> getEnv(envKey));
  }
}
