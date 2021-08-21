package org.veupathdb.lib.gradle.container.tasks.jaxrs;

import org.veupathdb.lib.gradle.container.tasks.base.build.BuildConfiguration;

/**
 * RAML 4 Jax-RS Build Configuration
 * <p>
 * Configuration for building RAML 4 Jax-RS.
 */
public class Raml4JaxRSBuildConfig extends BuildConfiguration {
  public Raml4JaxRSBuildConfig() {
    super("https://github.com/mulesoft-labs/raml-for-jax-rs.git", "3.0.7");
  }

  @Override
  public String toString() {
    return "Raml4JaxRSBuildConfig{" +
      "targetVersion='" + getTargetVersion() + '\'' +
      ", url='" + getUrl() + '\'' +
      '}';
  }
}
