package org.veupathdb.lib.gradle.container.tasks.jaxrs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.veupathdb.lib.gradle.container.tasks.base.exec.BinExecAction;
import org.veupathdb.lib.gradle.container.tasks.base.exec.ExecConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Generate Jax-RS Classes
 * <p>
 * Generates Jax-RS annotated Java classes based on the project's RAML
 * definitions.
 *
 * @since 1.1.0
 */
public class GenerateJaxRS extends BinExecAction {
  public static final  String TaskName = "generate-jaxrs";
  private static final String TaskDesc = "Generates JaxRS Java code based on the project's RAML API spec.";


  private static final String Command             = "java";
  private static final String FlagDirectory       = "--directory";
  private static final String FlagGenTypesWith    = "--generate-types-with";
  private static final String FlagModelPackage    = "--model-package";
  private static final String FlagResourcePackage = "--resource-package";
  private static final String FlagSupportPackage  = "--support-package";
  private static final String FlagJar             = "-jar";


  private static final String ParamGenTypesWith = "jackson";

  //
  //
  // Instance Fields
  //
  //

  /**
   * Lazily populated base package path for this project.
   * <p>
   * Example Value:
   * <pre>{@code
   * "org.veupathdb.service.demo"
   * }</pre>
   */
  @Nullable
  private String basePackage;

  //
  //
  // Override/Implementation Methods
  //
  //

  @Override
  public void fillIncrementalInputFiles(@NotNull Collection<File> files) {
    super.fillIncrementalInputFiles(files);
  }

  @Override
  @NotNull
  public String pluginDescription() {
    return TaskDesc;
  }

  @Override
  @NotNull
  protected String getCommandName() {
    return Command;
  }

  @Override
  protected void appendArguments(@NotNull final List<String> args) {
    log().open(args);

    args.addAll(Arrays.asList(
      FlagJar,             InstallRaml4JaxRS.OutputFile,
      new File(RootDir, getOptions().getRootApiDefinition()).getPath(),
      FlagDirectory,       getSourceDirectory(),
      FlagGenTypesWith,    ParamGenTypesWith,
      FlagModelPackage,    getModelPackagePath(),
      FlagResourcePackage, getResourcePackagePath(),
      FlagSupportPackage,  getSupportPackagePath()
    ));

    args.addAll(execConfiguration().getArguments());

    log().close();
  }

  @Override
  protected @NotNull ExecConfiguration execConfiguration() {
    return getOptions().getGenerateJaxRSConfig();
  }

  //
  //
  // Internal Methods
  //
  //

  /**
   * Returns the relative path to this project's java source directory.
   * <p>
   * <b>NOTE</b>: This directory is generally not the "src/" directory, but
   * instead the directory containing the package directories.  For standard
   * projects, this path will be {@code src/main/java}
   * <p>
   * TODO: This value should be configurable.
   *
   * @return The relative path to this project's java source directory.
   */
  @NotNull
  private String getSourceDirectory() {
    return "../src/main/java";
  }

  /**
   * Returns the configured base package path for the current project.
   * <p>
   * This value comes from the {@code build.gradle.kts} configuration for this
   * plugin, or if that value is not set, falls back to the value configured
   * in this project's {@code service.properties} file.
   * <p>
   * Example Output:
   * <pre>{@code
   * org.veupathdb.service.demo
   * }</pre>
   *
   * @return The configured base package for the current project.
   */
  @NotNull
  private String getBasePackagePath() {
    if (basePackage != null)
      return basePackage;

    return basePackage = projectConfig().getProjectPackage();
  }

  /**
   * Returns the target package path for the output Jax-RS model classes.
   * <p>
   * Example Output:
   * <pre>{@code
   * "org.veupathdb.service.demo.generated.model"
   * }</pre>
   *
   * TODO: The package suffix for the model class path should be configurable.
   *
   * @return The target package path for the output Jax-RS model classes.
   */
  @NotNull
  private String getModelPackagePath() {
    return getGeneratedPackagePath() + ".model";
  }

  /**
   * Returns the target package path for the output Jax-RS resource classes.
   * <p>
   * Example Output:
   * <pre>{@code
   * "org.veupathdb.service.demo.generated.resources"
   * }</pre>
   *
   * TODO: The package suffix for the resource class path should be
   *       configurable.
   *
   * @return The target package path for the output Jax-RS resource classes.
   */
  @NotNull
  private String getResourcePackagePath() {
    return getGeneratedPackagePath() + ".resources";
  }

  /**
   * Returns the target package path for the output Jax-RS support classes.
   * <p>
   * Example Output:
   * <pre>{@code
   * "org.veupathdb.service.demo.generated.support"
   * }</pre>
   *
   * TODO: The package suffix for the support class path should be configurable.
   *
   * @return The target package path for the output Jax-RS support classes.
   */
  @NotNull
  private String getSupportPackagePath() {
    return getGeneratedPackagePath() + ".support";
  }

  /**
   * Returns the target package path for the generated Jax-RS java code root.
   * <p>
   * Example Output:
   * <pre>{@code
   * "org.veupathdb.service.demo.generated"
   * }</pre>
   *
   * TODO: The package suffix for the generated code root should be
   *       configurable.
   *
   * @return the target package path for the generated Jax-RS java code root.
   */
  @NotNull
  private String getGeneratedPackagePath() {
    return getBasePackagePath() + ".generated";
  }
}
