package org.veupathdb.lib.gradle.container.config;

import org.gradle.api.Action;
import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.tasks.docker.DockerConfig;
import org.veupathdb.lib.gradle.container.tasks.base.exec.ExecConfiguration;
import org.veupathdb.lib.gradle.container.tasks.fgputil.FgpUtilConfiguration;
import org.veupathdb.lib.gradle.container.tasks.base.build.GlobalBinBuildConfiguration;
import org.veupathdb.lib.gradle.container.tasks.base.build.GlobalBuildConfiguration;
import org.veupathdb.lib.gradle.container.tasks.base.build.GlobalVendorBuildConfiguration;
import org.veupathdb.lib.gradle.container.tasks.jaxrs.Raml4JaxRSBuildConfig;
import org.veupathdb.lib.gradle.container.tasks.raml.GenRamlConfig;
import org.veupathdb.lib.gradle.container.util.Logger;

import java.util.Objects;

import static org.veupathdb.lib.gradle.container.tasks.base.Defaults.*;

/**
 * Gradle Plugin Extension
 * <p>
 * Allows the configuration of the various tasks in this plugin as well as base
 * options for building the project.
 *
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class Options {

  private final ProjectConfiguration           project            = new ProjectConfiguration();
  private final FgpUtilConfiguration           fgputil            = new FgpUtilConfiguration();
  private final GlobalBinBuildConfiguration    globalBinConfig    = new GlobalBinBuildConfiguration();
  private final GlobalVendorBuildConfiguration globalVendorConfig = new GlobalVendorBuildConfiguration();
  private final Raml4JaxRSBuildConfig          raml4jaxrs         = new Raml4JaxRSBuildConfig();
  private final ExecConfiguration              generateJaxRS      = new ExecConfiguration();
  private final GenRamlConfig                  generateRamlDocs   = new GenRamlConfig();
  private final DockerConfig                   docker             = new DockerConfig();

  @NotNull
  private String rootApiDefinition = DefaultApiDocsRoot;

  private byte logLevel = Logger.LogLevelInfo;

  /**
   * Modify the service project's settings.
   *
   * @param configure Action that will be called with a project configuration
   *                  instance.
   */
  public void project(@NotNull Action<? super ProjectConfiguration> configure) {
    configure.execute(project);
  }

  /**
   * Returns the service project's configuration.
   *
   * @return The service project's configuration.
   */
  public ProjectConfiguration getProjectConfig() {
    return project;
  }

  /**
   * Modify the service project's FgpUtil dependency settings.
   *
   * @param configure Action that will be called with the FgpUtil configuration.
   */
  public void fgputil(Action<? super FgpUtilConfiguration> configure) {
    configure.execute(fgputil);
  }

  /**
   * Returns the service project's FgpUtil dependency settings.
   *
   * @return The service project's FgpUtil dependency settings.
   */
  public FgpUtilConfiguration getFgpUtilConfig() {
    return fgputil;
  }

  /**
   * Modify the service project's build dependency build configuration.
   *
   * @param configure Action that will be called with the build dependency
   *                  config.
   */
  public void binBuilds(Action<? super GlobalBuildConfiguration> configure) {
    configure.execute(globalBinConfig);
  }

  /**
   * Returns the service project's build dependency build configuration.
   *
   * @return The service project's build dependency build configuration.
   */
  public GlobalBinBuildConfiguration getGlobalBinConfig() {
    return globalBinConfig;
  }

  /**
   * Modify the service project's vendor dependency build configuration.
   *
   * @param configure Action that will be called with the vendor dependency
   *                  config.
   */
  public void vendorBuilds(Action<? super GlobalVendorBuildConfiguration> configure) {
    configure.execute(globalVendorConfig);
  }

  /**
   * Returns the service project's vendor dependency build configuration.
   *
   * @return The service project's vendor dependency build configuration.
   */
  public GlobalVendorBuildConfiguration getGlobalVendorConfig() {
    return globalVendorConfig;
  }

  /**
   * Modify the service project's RAML 4 Jax-RS code generator build
   * configuration.
   *
   * @param configure Action that will be called with the RAML 4 Jax-RS build
   *                  config.
   */
  public void raml4jaxrs(Action<? super Raml4JaxRSBuildConfig> configure) {
    configure.execute(raml4jaxrs);
  }

  /**
   * Returns the service project's RAML 4 Jax-RS code generator build
   * configuration.
   *
   * @return the service project's RAML 4 Jax-RS code generator build
   * configuration.
   */
  public Raml4JaxRSBuildConfig getRaml4JaxRSConfig() {
    return raml4jaxrs;
  }

  /**
   * Modify the service project's Jax-RS code generation configuration.
   *
   * @param configure Action that will be called with the service project's
   *                  Jax-RS code generation config.
   */
  public void generateJaxRS(Action<? super ExecConfiguration> configure) {
    configure.execute(generateJaxRS);
  }

  /**
   * Returns the service project's Jax-RS code generation configuration.
   *
   * @return The service project's Jax-RS code generation configuration.
   */
  public ExecConfiguration getGenerateJaxRSConfig() {
    return generateJaxRS;
  }

  /**
   * Modify the service project's API documentation generation configuration.
   *
   * @param configure Action that will be called with the service project's
   *                  API doc generation config.
   */
  public void generateRamlDocs(Action<? super GenRamlConfig> configure) {
    configure.execute(generateRamlDocs);
  }

  /**
   * Returns the service project's API documentation generation configuration.
   *
   * @return The service project's API documentation generation configuration.
   */
  public GenRamlConfig getGenerateRamlDocsConfig() {
    return generateRamlDocs;
  }

  /**
   * Modify the service project's Docker build configuration.
   *
   * @param configure Action that will be called with the service project's
   *                  Docker build config.
   */
  public void docker(Action<? super DockerConfig> configure) {
    configure.execute(docker);
  }

  /**
   * Returns the service project's Docker build configuration.
   *
   * @return The service project's Docker build configuration.
   */
  public DockerConfig getDockerConfig() {
    return docker;
  }

  /**
   * Returns the configured relative path to the current project's root API
   * definition file (RAML, or OpenAPI).
   *
   * @return The configured relative path to the current project's root API
   * definition file.
   *
   * @since 2.0.0
   */
  @NotNull
  public String getRootApiDefinition() {
    return rootApiDefinition;
  }

  /**
   * Configures the relative path to the current project's root API definition
   * file (RAML or OpenAPI).
   *
   * @param apiDocFile Relative path to the current project's root API
   *                   definition file.
   *
   * @since 2.0.0
   */
  public void setRootApiDefinition(@NotNull final String apiDocFile) {
    this.rootApiDefinition = Objects.requireNonNull(apiDocFile);
  }

  /**
   * Returns the currently configured log level value.
   *
   * @return The currently configured log level value.
   *
   * @since 1.1.0
   */
  public byte getLogLevel() {
    return logLevel;
  }

  /**
   * Configures the log level value.
   *
   * @param logLevel New log level.
   *
   * @since 1.1.0
   */
  public void setLogLevel(byte logLevel) {
    this.logLevel = logLevel;
  }

  @Override
  public String toString() {
    return "Options{" +
      "rootApiDefinition='" + rootApiDefinition + '\'' +
      ", logLevel=" + logLevel +
      ", project=" + project +
      ", fgputil=" + fgputil +
      ", globalBinConfig=" + globalBinConfig +
      ", globalVendorConfig=" + globalVendorConfig +
      ", raml4jaxrs=" + raml4jaxrs +
      ", generateJaxRS=" + generateJaxRS +
      ", generateRamlDocs=" + generateRamlDocs +
      ", docker=" + docker +
      '}';
  }
}
