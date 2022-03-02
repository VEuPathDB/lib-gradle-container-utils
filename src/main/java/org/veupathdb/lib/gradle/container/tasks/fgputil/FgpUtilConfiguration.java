package org.veupathdb.lib.gradle.container.tasks.fgputil;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.tasks.base.build.BuildConfiguration;

import java.util.List;

/**
 * FgpUtil Build Configuration
 * <p>
 * Configuration for building FgpUtil.
 */
public class FgpUtilConfiguration {

  public static final String
    AccountDB = "fgputil-accountdb-1.0.0.jar",
    Cache = "fgputil-cache-1.0.0.jar",
    CLI = "fgputil-cli-1.0.0.jar",
    Client = "fgputil-client-1.0.0.jar",
    Core = "fgputil-core-1.0.0.jar",
    DB = "fgputil-db-1.0.0.jar",
    Events = "fgputil-events-1.0.0.jar",
    JSON = "fgputil-json-1.0.0.jar",
    Server = "fgputil-server-1.0.0.jar",
    Servlet = "fgputil-servlet-1.0.0.jar",
    Solr = "fgputil-solr-1.0.0.jar",
    Test = "fgputil-test-1.0.0.jar",
    Web = "fgputil-web-1.0.0.jar",
    XML = "fgputil-xml-1.0.0.jar"
  ;

  private String version = "latest";

  private String[] targets = {
    AccountDB,
    Cache,
    CLI,
    Client,
    Core,
    DB,
    Events,
    JSON,
    Server,
    Servlet,
    Solr,
    Test,
    Web,
    XML,
  };


  public @NotNull String getVersion() {
    return version;
  }

  public void setVersion(@NotNull String version) {
    this.version = version;
  }

  public @NotNull String[] getTargets() {
    return targets;
  }

  public void setTargets(@NotNull String[] targets) {
    this.targets = targets;
  }


}
