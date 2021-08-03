package org.veupathdb.lib.gradle.container.config;

import org.jetbrains.annotations.NotNull;

import static org.veupathdb.lib.gradle.container.tasks.base.Defaults.*;

@SuppressWarnings("unused")
public class Options {
  @NotNull
  private String vendorDirectory = DefaultVendorDirectory;

  @NotNull
  private String fgpUtilVersion = DefaultFgpUtilVersion;

  @NotNull
  private String ramlForJaxRSVersion = DefaultRamlForJaxRSVersion;

  @NotNull
  private String binDirectory = DefaultBinDirectory;

  @NotNull
  private String repoDocsDirectory = DefaultDocsDirectory;

  @NotNull
  private String apiDocRoot = DefaultApiDocsRoot;

  private byte logLevel = 1;

  /**
   * Returns the currently configured custom vendor directory.
   * <p>
   * <b>Note</b>: If this directory doesn't already exist, on first vendored
   * dependency installation, it will be created.
   *
   * @return The currently configured custom vendor directory.  {@code null} if
   * no custom directory has been set and the default is being used.
   */
  @NotNull
  public String getVendorDirectory() {
    return vendorDirectory;
  }

  /**
   * Sets the target directory for building and storing vendored dependencies.
   * <p>
   * <b>Note</b>: If this directory doesn't already exist, on first vendored
   * dependency installation, it will be created.
   *
   * @param directory Target vendor directory.
   */
  public void setVendorDirectory(@NotNull final String directory) {
    this.vendorDirectory = directory;
  }

  /**
   * Returns the currently configured target FgpUtil tag, branch, or commit.
   * <p>
   * This value determines what version of FgpUtil will be used when building
   * this project.  If this value changes after FgpUtil has already been built
   * and vendored, run the installation command again to update.
   * <p>
   * <b>Note</b>: If this value is a branch name it will only build that branch
   * once.  If new commits are available to FgpUtil that are desired, run the
   * FgpUtil uninstallation command, then run the installation again.
   *
   * @return The currently configured target FgpUtil tag, branch, or commit.
   */
  @NotNull
  public String getFgpUtilVersion() {
    return fgpUtilVersion;
  }

  /**
   * Configures the target FgpUtil tag, branch, or commit to use when building
   * this project.
   * <p>
   * If updating this value after FgpUtil has already been built and vendored,
   * run the installation command again to update.
   * <p>
   * <b>Note</b>: If this value is a branch name it will only build that branch
   * once.  If new commits are available to FgpUtil that are desired, run the
   * FgpUtil uninstallation command, then run the installation again.
   *
   * @param version FgpUtil tag, branch, or commit to use when building
   *                this project.
   */
  public void setFgpUtilVersion(@NotNull final String version) {
    this.fgpUtilVersion = version;
  }

  /**
   * Returns the currently configured target Raml for Jax RS branch.
   * <p>
   * This value allows overriding the default Raml for Jax RS branch
   * configuration.
   * <p>
   * If updating this value after Raml for Jax RS has already been installed,
   * run the installation command again to update.
   *
   * @return The currently configured override Raml for Jax RS branch.
   */
  @NotNull
  public String getRamlForJaxRSVersion() {
    return ramlForJaxRSVersion;
  }

  /**
   * Configures an override for the target branch of Raml for Jax RS.
   * <p>
   * If updating this value after Raml for Jax RS has already been installed,
   * run the installation command again to update.
   *
   * @param branch Raml for Jax RS branch name.
   */
  public void setRamlForJaxRSVersion(@NotNull final String branch) {
    this.ramlForJaxRSVersion = branch;
  }

  @NotNull
  public String getBinDirectory() {
    return binDirectory;
  }

  public void setBinDirectory(@NotNull final String directory) {
    this.binDirectory = directory;
  }

  @NotNull
  public String getRepoDocsDirectory() {
    return repoDocsDirectory;
  }

  public void setRepoDocsDirectory(@NotNull final String directory) {
    this.repoDocsDirectory = directory;
  }

  @NotNull
  public String getApiDocRoot() {
    return apiDocRoot;
  }

  public void setApiDocRoot(@NotNull final String apiDocFile) {
    this.apiDocRoot = apiDocFile;
  }

  public byte getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(byte logLevel) {
    this.logLevel = logLevel;
  }

  @Override
  public String toString() {
    return "Options{" +
      "vendorDirectory='" + vendorDirectory + '\'' +
      ", fgpUtilVersion='" + fgpUtilVersion + '\'' +
      ", ramlForJaxRSVersion='" + ramlForJaxRSVersion + '\'' +
      ", binDirectory='" + binDirectory + '\'' +
      ", repoDocsDirectory='" + repoDocsDirectory + '\'' +
      ", apiDocRoot='" + apiDocRoot + '\'' +
      ", logLevel=" + logLevel +
      '}';
  }
}
