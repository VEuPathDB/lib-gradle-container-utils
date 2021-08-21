package org.veupathdb.lib.gradle.container.tasks.fgputil;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.tasks.base.build.BuildConfiguration;

/**
 * FgpUtil Build Configuration
 * <p>
 * Configuration for building FgpUtil.
 */
public class FgpUtilConfiguration extends BuildConfiguration {
  public FgpUtilConfiguration() {
    super("https://github.com/VEuPathDB/FgpUtil", "master");
  }

  /**
   * Returns the target FgpUtil version/branch to build.
   * <p>
   * If unset, defaults to the default Git branch name {@code master}.
   *
   * @return the target version/branch of FgpUtil to build.
   */
  @Override
  public @NotNull String getTargetVersion() {
    return super.getTargetVersion();
  }

  /**
   * Configures the target version/branch of FgpUtil to build.
   * <p>
   * If unset, defaults to the default Git branch name {@code master}.
   *
   * @param targetVersion New target version of FgpUtil to build.
   *
   * @throws NullPointerException If the given {@code targetVersion} value is
   *                              {@code null}.
   */
  @Override
  public void setTargetVersion(@NotNull String targetVersion) {
    super.setTargetVersion(targetVersion);
  }

  /**
   * Returns the URL to FgpUtil's Git repo.
   *
   * @return the URL to FgpUtil's Git repo.
   */
  public @NotNull String getUrl() {
    return super.getUrl();
  }

  /**
   * Configures the URL to the FgpUtil Git repo to clone.
   *
   * @param url The URL to the FgpUtil Git repo to clone.
   *
   * @throws NullPointerException If the given {@code url} value is
   * {@code null}.
   */
  public void setUrl(@NotNull String url) {
    super.setUrl(url);
  }

  @Override
  public String toString() {
    return "FgpUtilConfiguration{" +
      "targetVersion='" + getTargetVersion() + '\'' +
      "url='" + getUrl() + '\'' +
      '}';
  }
}
