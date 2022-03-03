package org.veupathdb.lib.gradle.container.tasks.docker;

import org.veupathdb.lib.gradle.container.tasks.base.exec.ExecConfiguration;

/**
 * Docker Build Configuration
 * <p>
 * Configuration for building the root docker container of a project.
 *
 * @since 2.0.0
 */
@SuppressWarnings("unused")
class DockerConfig(
  imageName: String = "demo-service",
  context: String = ".",
  dockerFile: String = "Dockerfile",
) : ExecConfiguration()
