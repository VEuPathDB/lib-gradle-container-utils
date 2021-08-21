package org.veupathdb.lib.gradle.container.tasks.raml;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.tasks.base.exec.ExecConfiguration;

import java.util.Objects;

public class GenRamlConfig extends ExecConfiguration {
  @NotNull
  private String repoDocsDir = "docs";

  @NotNull
  private String resourceDocsDir = "src/main/resources";

  @NotNull
  private String apiDocFileName = "api.html";

  @NotNull
  public String getRepoDocsDir() {
    return repoDocsDir;
  }

  public void setRepoDocsDir(@NotNull String repoDocsDir) {
    this.repoDocsDir = Objects.requireNonNull(repoDocsDir);
  }

  @NotNull
  public String getResourceDocsDir() {
    return resourceDocsDir;
  }

  public void setResourceDocsDir(@NotNull String resourceDocsDir) {
    this.resourceDocsDir = Objects.requireNonNull(resourceDocsDir);
  }

  @NotNull
  public String getApiDocFileName() {
    return apiDocFileName;
  }

  public void setApiDocFileName(@NotNull String apiDocFileName) {
    this.apiDocFileName = Objects.requireNonNull(apiDocFileName);
  }

  @Override
  public String toString() {
    return "GenRamlConfig{" +
      "arguments=" + getArguments() +
      ", environment=" + getEnvironment() +
      ", repoDocsDir='" + repoDocsDir + '\'' +
      ", resourceDocsDir='" + resourceDocsDir + '\'' +
      ", apiDocFileName='" + apiDocFileName + '\'' +
      '}';
  }
}
