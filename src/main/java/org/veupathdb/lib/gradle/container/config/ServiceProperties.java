package org.veupathdb.lib.gradle.container.config;

import java.util.Properties;

public class ServiceProperties {
  private static final String KeyProjectName       = "project.name";
  private static final String KeyProjectVersion    = "project.version";
  private static final String KeyProjectGroup      = "project.group";
  private static final String KeyContainerName     = "container.name";
  private static final String KeyAppPackageRoot    = "app.package.root";
  private static final String KeyAppPackageService = "app.package.service";
  private static final String KeyAppMainClass      = "app.main-class";

  private final Properties props;

  public ServiceProperties(Properties props) {
    this.props = props;
  }

  public String projectName() {
    return (String) props.get(KeyProjectName);
  }

  public String projectVersion() {
    return (String) props.get(KeyProjectVersion);
  }

  public String projectGroup() {
    return (String) props.get(KeyProjectGroup);
  }

  public String containerName() {
    return (String) props.get(KeyContainerName);
  }

  public String appPackageRoot() {
    return (String) props.get(KeyAppPackageRoot);
  }

  public String appPackageService() {
    return (String) props.get(KeyAppPackageService);
  }

  public String appMainClass() {
    return (String) props.get(KeyAppMainClass);
  }
}
