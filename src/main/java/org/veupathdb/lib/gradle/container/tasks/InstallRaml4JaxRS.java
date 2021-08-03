package org.veupathdb.lib.gradle.container.tasks;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.exec.Git;
import org.veupathdb.lib.gradle.container.exec.Maven;
import org.veupathdb.lib.gradle.container.tasks.base.BinBuildAction;

import java.io.File;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

public class InstallRaml4JaxRS extends BinBuildAction {

  public static final String TaskName = "ramlGenInstall";

  static final String  LockFile     = "raml4jaxrs.lock";
  static final String  GitURL       = "https://github.com/mulesoft-labs/raml-for-jax-rs.git";
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
    return Log.getter(new File(getDependencyRoot(), LockFile));
  }

  @Override
  @NotNull
  protected String pluginDescription() {
    return "Builds and installs the Raml for JaxRS generator.";
  }

  @Override
  @NotNull
  protected String getConfiguredVersion() {
    return Log.getter(Options.getRamlForJaxRSVersion());
  }

  @Override
  protected void clean() {

  }

  @Override
  @NotNull
  protected File download() {
    Log.open();
    Log.info("Cloning " + getDependencyName());

    return Log.close(new Git(Log).shallowClone(GitURL, getDependencyRoot(), getConfiguredVersion()));
  }

  @Override
  protected void install() {
    Log.open();

    correctPoms(findPoms());

    Log.info("Compiling " + getDependencyName());

    final var mvn = new Maven(Log);
    final var dir = new File(getBuildTargetDirectory(), "raml-to-jaxrs/raml-to-jaxrs-cli");

    mvn.cleanInstall(dir);
    mvn.findOutputJars(dir)
      .filter(f -> f.getName().endsWith("dependencies.jar"))
      .peek(f -> Log.debug("Installing jar %s", f))
      .forEach(f -> Util.moveFile(f, new File(getBinRoot(), OutputFile)));

    Log.close();
  }

  @NotNull
  private List<File> findPoms() {
    Log.open();

    // Version 3.0.7 of raml-for-jax-rs contains 46 pom files
    final var poms = new Stack<File>();
    final var dirs = new Stack<File>();

    dirs.push(getBuildTargetDirectory());

    while (!dirs.empty()) {
      final var dir = dirs.pop();

      Log.debug("Gathering pom files from directory %s", dir);

      //noinspection ConstantConditions
      for (final var child : dir.listFiles()) {
        if (child.isDirectory()) {
          if (!child.getName().startsWith(".") && !child.getName().equals("src"))
            dirs.push(child);
        } else if (child.getName().equals("pom.xml")) {
          Log.debug("Located pom file %s", child);
          poms.add(child);
        }
      }
    }

    return Log.close(poms);
  }

  private void correctPoms(@NotNull final List<File> poms) {
    Log.open(poms);

    Log.info("Patching %s pom files", getDependencyName());

    for (final var pom : poms) {
      Log.debug("Patching %s", pom);

      Util.overwriteFile(
        pom,
        VersionMatch.matcher(Util.readFile(pom))
          .replaceAll(getConfiguredVersion())
      );
    }
  }
}
