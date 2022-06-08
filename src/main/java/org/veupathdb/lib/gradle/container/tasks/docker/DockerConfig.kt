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
  var imageName:  String? = null,
  var context:    String = ".",
  var dockerFile: String = "Dockerfile",
) : ExecConfiguration()
