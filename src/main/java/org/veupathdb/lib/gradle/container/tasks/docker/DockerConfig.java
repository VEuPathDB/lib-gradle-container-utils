package org.veupathdb.lib.gradle.container.tasks.docker;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.tasks.base.exec.ExecConfiguration;

import java.util.Objects;

/**
 * Docker Build Configuration
 * <p>
 * Configuration for building the root docker container of a project.
 *
 * @since 2.0.0
 */
@SuppressWarnings("unused")
public class DockerConfig extends ExecConfiguration {

  @NotNull
  private String imageName = "demo-service";

  @NotNull
  private String context = ".";

  @NotNull
  private String dockerFile = "Dockerfile";

  /**
   * Returns the currently configured context directory for the Docker build.
   * <p>
   * This is typically the directory containing the Dockerfile.  Unless your
   * project is structured in a non-standard way, this will also be the root
   * directory of your project.
   * <p>
   * Default Value: {@code "."}
   *
   * @return The context directory for the Docker build.
   */
  @NotNull
  public String getContext() {
    return context;
  }

  /**
   * Configures the context directory for the Docker build.
   * <p>
   * This is typically the directory containing the Dockerfile.  Unless your
   * project is structured in a non-standard way, this will also be the root
   * directory of your project.
   * <p>
   * Default Value: {@code "."}
   *
   * @param context Relative path to the project's root Docker build context.
   */
  public void setContext(@NotNull String context) {
    this.context = Objects.requireNonNull(context);
  }

  /**
   * Returns the currently configured name of the Dockerfile used to build the
   * root Docker image for this project.
   * <p>
   * Default Value: {@code "Dockerfile"}
   *
   * @return the name of the project's root Dockerfile.
   */
  @NotNull
  public String getDockerFile() {
    return dockerFile;
  }

  /**
   * Configures the name of the Dockerfile used to build the root Docker
   * container for this project.
   * <p>
   * Default Value: {@code "Dockerfile"}
   *
   * @param dockerFile Name of the project's root Dockerfile.
   */
  public void setDockerFile(@NotNull String dockerFile) {
    this.dockerFile = Objects.requireNonNull(dockerFile);
  }

  /**
   * Returns the currently configured name for the Docker image built from this
   * project.
   * <p>
   * Default Value: {@code "demo-service"}
   *
   * @return Name of the built Docker image.
   */
  @NotNull
  public String getImageName() {
    return imageName;
  }

  /**
   * Configures the name for the Docker image built from this project.
   *
   * @param imageName Name for the build Docker image.
   */
  public void setImageName(@NotNull String imageName) {
    this.imageName = Objects.requireNonNull(imageName);
  }

  @Override
  public String toString() {
    return "DockerConfig{" +
      "arguments=" + getArguments() +
      ", environment=" + getEnvironment() +
      ", imageName='" + imageName + '\'' +
      ", context='" + context + '\'' +
      ", dockerFile='" + dockerFile + '\'' +
      '}';
  }
}
