package org.veupathdb.lib.gradle.container.config;


class ServiceConfiguration {

  var name = "demo-service";

  var version = "1.0.0";

  var group = "org.veupathdb.service";

  var projectPackage = "org.veupathdb.service.demo";

  var mainClassName = "Main";

  override fun toString() =
    "ProjectConfiguration{" +
      "name='" + name + '\'' +
      ", version='" + version + '\'' +
      ", group='" + group + '\'' +
      ", projectPackage='" + projectPackage + '\'' +
      ", mainClassName='" + mainClassName + '\'' +
      '}';
}
