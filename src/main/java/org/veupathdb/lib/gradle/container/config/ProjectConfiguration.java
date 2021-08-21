package org.veupathdb.lib.gradle.container.config;

import org.jetbrains.annotations.NotNull;

public class ProjectConfiguration {

  @NotNull
  private String name = "demo-service";

  @NotNull
  private String version = "1.0.0";

  @NotNull
  private String group = "org.veupathdb.service";

  @NotNull
  private String projectPackage = "org.veupathdb.service.demo";

  @NotNull
  private String mainClassName = "Main";

  @NotNull
  public String getName() {
    return name;
  }

  public void setName(@NotNull String name) {
    this.name = name;
  }

  @NotNull
  public String getVersion() {
    return version;
  }

  public void setVersion(@NotNull String version) {
    this.version = version;
  }

  @NotNull
  public String getGroup() {
    return group;
  }

  public void setGroup(@NotNull String group) {
    this.group = group;
  }

  @NotNull
  public String getProjectPackage() {
    return projectPackage;
  }

  public void setProjectPackage(@NotNull String projectPackage) {
    this.projectPackage = projectPackage;
  }

  @NotNull
  public String getMainClassName() {
    return mainClassName;
  }

  public void setMainClassName(@NotNull String mainClassName) {
    this.mainClassName = mainClassName;
  }

  @Override
  public String toString() {
    return "ProjectConfiguration{" +
      "name='" + name + '\'' +
      ", version='" + version + '\'' +
      ", group='" + group + '\'' +
      ", projectPackage='" + projectPackage + '\'' +
      ", mainClassName='" + mainClassName + '\'' +
      '}';
  }
}
