package org.veupathdb.lib.gradle.container.tasks.jaxrs;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.exec.git.Git;
import org.veupathdb.lib.gradle.container.exec.Maven;
import org.veupathdb.lib.gradle.container.tasks.base.build.BinBuildAction;

import java.io.File;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * RAML 4 Jax-RS Generator Installation
 *
 * @since 1.1.0
 */
public class InstallRaml4JaxRS extends BinBuildAction {

  public static final String TaskName = "install-raml-4-jax-rs";

  static final String  LockFile     = "raml4jaxrs.lock";
  static final Pattern VersionMatch = Pattern.compile("\\d+\\.\\d+\\.\\d+-SNAPSHOT");
  static final String  OutputFile   = "raml-to-jaxrs.jar";

  @Override
  @NotNull
  protected String getDependencyName() {
    return "Raml for JaxRS";
  }

  @Override
  @NotNull
  protected File getLockFile() {
    return log().getter(new File(getDependencyRoot(), LockFile));
  }

  @Override
  @NotNull
  public String pluginDescription() {
    return "Builds and installs the Raml for JaxRS generator.";
  }

  @Override
  protected void clean() {

  }

  @Override
  @NotNull
  protected File download() {
    log().open();
    log().info("Cloning {}", this::getDependencyName);

    return log().close(new Git(log()).shallowClone(
      buildConfiguration().getUrl(),
      getDependencyRoot(),
      buildConfiguration().getTargetVersion()
    ));
  }

  @Override
  protected void install() {
    log().open();

    correctPoms(findPoms());

    log().info("Compiling {}", this::getDependencyName);

    final var mvn = new Maven(log());
    final var dir = new File(getBuildTargetDirectory(), "raml-to-jaxrs/raml-to-jaxrs-cli");

    mvn.cleanInstall(dir);
    mvn.findOutputJars(dir)
      .filter(f -> f.getName().endsWith("dependencies.jar"))
      .peek(f -> log().debug("Installing jar %s", f))
      .forEach(f -> util().moveFile(f, new File(getBinRoot(), OutputFile)));

    log().close();
  }

  @Override
  protected Raml4JaxRSBuildConfig buildConfiguration() {
    return getOptions().getRaml4JaxRSConfig();
  }

  /**
   * Locates all the pom.xml files in the cloned raml-4-jax-rs Git repo.
   *
   * @return A list of all the pom.xml files in the cloned raml-4-jax-rs Git
   * repo.
   *
   * @since 1.1.0
   */
  @NotNull
  private List<File> findPoms() {
    log().open();

    // Version 3.0.7 of raml-for-jax-rs contains 46 pom files
    final var poms = new Stack<File>();
    final var dirs = new Stack<File>();

    dirs.push(getBuildTargetDirectory());

    while (!dirs.empty()) {
      final var dir = dirs.pop();

      log().debug("Gathering pom files from directory {}", dir);

      //noinspection ConstantConditions
      for (final var child : dir.listFiles()) {

        if (child.isDirectory()) {

          if (!child.getName().startsWith(".") && !child.getName().equals("src"))
            dirs.push(child);

        } else if (child.getName().equals("pom.xml")) {

          log().debug("Located pom file {}", child);
          poms.add(child);

        }
      }
    }

    return log().close(poms);
  }

  /**
   * Monkey-patches the pom files in the given list to fix a build error from
   * bad version values in the cloned pom files.
   *
   * @param poms Pom files to patch.
   *
   * @since 1.1.0
   */
  private void correctPoms(@NotNull final List<File> poms) {
    log().open(poms);

    log().info("Patching {} pom files", this::getDependencyName);

    for (final var pom : poms) {
      log().debug("Patching {}", pom);

      util().overwriteFile(
        pom,
        VersionMatch.matcher(util().readFile(pom))
          .replaceAll(buildConfiguration().getTargetVersion())
      );
    }

    log().close();
  }
}
