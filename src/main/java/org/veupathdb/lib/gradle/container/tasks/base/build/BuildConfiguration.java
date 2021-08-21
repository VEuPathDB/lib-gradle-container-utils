package org.veupathdb.lib.gradle.container.tasks.base.build;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Generic Build Configuration
 * <p>
 * Provides a base set of options relevant to most if not all tasks that build
 * external projects dependencies to this project.
 */
public class BuildConfiguration {

  @NotNull
  private String url;

  /**
   * Target version of the dependency to build.
   */
  @NotNull
  private String targetVersion;

  protected BuildConfiguration(@NotNull String url, @NotNull String version) {
    this.url           = Objects.requireNonNull(url);
    this.targetVersion = Objects.requireNonNull(version);
  }

  /**
   * Returns the target version of the dependency to build.
   * <p>
   * If unset, defaults to the default Git branch name {@code main}.
   *
   * @return the target version of the dependency to build.
   */
  @NotNull
  public String getTargetVersion() {
    return targetVersion;
  }

  /**
   * Configures the target version of the dependency to build.
   * <p>
   * If unset, defaults to the default Git branch name {@code main}.
   *
   * @param targetVersion New target version of the dependency to build.
   *
   * @throws NullPointerException If the given {@code targetVersion} value is
   *                              {@code null}.
   */
  public void setTargetVersion(@NotNull String targetVersion) {
    this.targetVersion = Objects.requireNonNull(targetVersion);
  }

  /**
   * Returns the URL to the dependency's download location.
   *
   * @return The URL to the dependency's download location.
   */
  public @NotNull String getUrl() {
    return url;
  }

  /**
   * Configures the URL to the dependency's download location.
   *
   * @param url The URL to the dependency's download location.
   *
   * @throws NullPointerException If the given {@code url} value is
   *                              {@code null}.
   */
  public void setUrl(@NotNull String url) {
    this.url = Objects.requireNonNull(url);
  }

  @Override
  public String toString() {
    return "BuildConfiguration{" +
      "url='" + url + '\'' +
      ", targetVersion='" + targetVersion + '\'' +
      '}';
  }
}
