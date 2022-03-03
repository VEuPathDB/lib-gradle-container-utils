package org.veupathdb.lib.gradle.container.tasks.fgputil

/**
 * FgpUtil Build Configuration
 * <p>
 * Configuration for building FgpUtil.
 */
class FgpUtilConfiguration {

  companion object {
    @JvmStatic
    val AccountDB = Target("fgputil-accountdb-1.0.0.jar")

    @JvmStatic
    val Cache = Target("fgputil-cache-1.0.0.jar")

    @JvmStatic
    val CLI = Target("fgputil-cli-1.0.0.jar")

    @JvmStatic
    val Client = Target("fgputil-client-1.0.0.jar")

    @JvmStatic
    val Core = Target("fgputil-core-1.0.0.jar")

    @JvmStatic
    val DB = Target("fgputil-db-1.0.0.jar")

    @JvmStatic
    val Events = Target("fgputil-events-1.0.0.jar")

    @JvmStatic
    val JSON = Target("fgputil-json-1.0.0.jar")

    @JvmStatic
    val Server = Target("fgputil-server-1.0.0.jar")

    @JvmStatic
    val Servlet = Target("fgputil-servlet-1.0.0.jar")

    @JvmStatic
    val Solr = Target("fgputil-solr-1.0.0.jar")

    @JvmStatic
    val Test = Target("fgputil-test-1.0.0.jar")

    @JvmStatic
    val Web = Target("fgputil-web-1.0.0.jar")

    @JvmStatic
    val XML = Target("fgputil-xml-1.0.0.jar")
  }

  @JvmInline
  value class Target(val value: String)

  var version = "latest"

  var targets = arrayOf(
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
  )
}
