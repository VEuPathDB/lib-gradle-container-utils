package org.veupathdb.lib.gradle.container.tasks.fgputil

/**
 * FgpUtil Build Configuration
 * <p>
 * Configuration for building FgpUtil.
 */
class FgpUtilConfiguration {

    val AccountDB = Target("fgputil-accountdb-1.0.0.jar")

    val Cache = Target("fgputil-cache-1.0.0.jar")

    val CLI = Target("fgputil-cli-1.0.0.jar")

    val Client = Target("fgputil-client-1.0.0.jar")

    val Core = Target("fgputil-core-1.0.0.jar")

    val DB = Target("fgputil-db-1.0.0.jar")

    val Events = Target("fgputil-events-1.0.0.jar")

    val JSON = Target("fgputil-json-1.0.0.jar")

    val Server = Target("fgputil-server-1.0.0.jar")

    val Servlet = Target("fgputil-servlet-1.0.0.jar")

    val Solr = Target("fgputil-solr-1.0.0.jar")

    val Test = Target("fgputil-test-1.0.0.jar")

    val Web = Target("fgputil-web-1.0.0.jar")

    val XML = Target("fgputil-xml-1.0.0.jar")

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
