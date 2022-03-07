package org.veupathdb.lib.gradle.container.tasks.jaxrs

import org.veupathdb.lib.gradle.container.exec.Maven
import org.veupathdb.lib.gradle.container.exec.git.Git
import org.veupathdb.lib.gradle.container.tasks.base.build.BinBuildAction

import java.io.File
import java.util.Stack

/**
 * RAML 4 Jax-RS Generator Installation
 *
 * @since 1.1.0
 */
open class InstallRaml4JaxRS : BinBuildAction() {

  companion object {
    private const val LockFile = "raml4jaxrs.lock"

    @JvmStatic
    private val VersionMatch = Regex("\\d+\\.\\d+\\.\\d+-SNAPSHOT")

    const val TaskName = "install-raml-4-jax-rs"

    const val OutputFile = "raml-to-jaxrs.jar"
  }

  override fun getDependencyName(): String {
    return "Raml for JaxRS"
  }

  override fun getLockFile(): File {
    return log.getter(File(getDependencyRoot(), LockFile))
  }

  override val pluginDescription
    get() = "Builds and installs the Raml for JaxRS generator."

  override fun clean() {}

  override fun download(): File {
    log.open()
    log.info("Cloning {}", this::getDependencyName)

    return log.close(Git(log).shallowClone(
      buildConfiguration().url,
      getDependencyRoot(),
      buildConfiguration().targetVersion
    ))
  }

  override fun install() {
    log.open()

    correctPoms(findPoms())

    log.info("Compiling {}", this::getDependencyName)

    val mvn = Maven(log)
    val dir = File(getBuildTargetDirectory(), "raml-to-jaxrs/raml-to-jaxrs-cli")

    mvn.cleanInstall(dir)
    mvn.findOutputJars(dir)
      .filter { it.name.endsWith("dependencies.jar") }
      .peek { log.debug("Installing jar %s", it) }
      .forEach { util.moveFile(it, File(getBinRoot(), OutputFile)) }

    log.close()
  }

  override fun buildConfiguration(): Raml4JaxRSBuildConfig {
    return options.raml4jaxrs
  }

  /**
   * Locates all the pom.xml files in the cloned raml-4-jax-rs Git repo.
   *
   * @return A list of all the pom.xml files in the cloned raml-4-jax-rs Git
   * repo.
   *
   * @since 1.1.0
   */
  private fun findPoms(): List<File> {
    log.open()

    // Version 3.0.7 of raml-for-jax-rs contains 46 pom files
    val poms = Stack<File>()
    val dirs = Stack<File>()

    dirs.push(getBuildTargetDirectory())

    while (!dirs.empty()) {
      val dir = dirs.pop()

      log.debug("Gathering pom files from directory {}", dir)

      //noinspection ConstantConditions
      for (child in dir.listFiles()!!) {

        if (child.isDirectory) {

          if (!child.name.startsWith(".") && !child.name.equals("src"))
            dirs.push(child)

        } else if (child.name.equals("pom.xml")) {

          log.debug("Located pom file {}", child)
          poms.add(child)

        }
      }
    }

    return log.close(poms)
  }

  /**
   * Monkey-patches the pom files in the given list to fix a build error from
   * bad version values in the cloned pom files.
   *
   * @param poms Pom files to patch.
   *
   * @since 1.1.0
   */
  private fun correctPoms(poms: List<File>) {
    log.open(poms)

    log.info("Patching {} pom files", this::getDependencyName)

    for (pom in poms) {
      log.debug("Patching {}", pom)

      util.overwriteFile(
        pom,
        VersionMatch.replace(util.readFile(pom), buildConfiguration().targetVersion)
      )
    }

    log.close()
  }
}
