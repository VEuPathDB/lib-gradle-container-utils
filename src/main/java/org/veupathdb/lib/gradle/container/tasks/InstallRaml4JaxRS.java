package org.veupathdb.lib.gradle.container.tasks;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.exec.Git;
import org.veupathdb.lib.gradle.container.exec.Maven;
import org.veupathdb.lib.gradle.container.tasks.base.BinBuildAction;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
    return new File(getDependencyRoot(), LockFile);
  }

  @Override
  @NotNull
  protected String pluginDescription() {
    return "Builds and installs the Raml for JaxRS generator.";
  }

  @Override
  @NotNull
  protected String getConfiguredVersion() {
    return Options.getRamlForJaxRSVersion();
  }

  @Override
  protected void clean() {

  }

  @Override
  @NotNull
  protected File download() {
    Log.trace("InstallRaml4JaxRS#download()");
    Log.info("Cloning " + getDependencyName());

    return new Git(getProject()).shallowClone(GitURL, getDependencyRoot(), getConfiguredVersion());
  }

  @Override
  protected void install() {
    Log.trace("InstallRaml4JaxRS#install()");

    correctPoms(findPoms());

    Log.info("Compiling " + getDependencyName());

    final var jars = new Maven(Log)
      .cleanInstall(new File(getBuildTargetDirectory(), "raml-to-jaxrs/raml-to-jaxrs-cli"));

    for (final var jar : jars) {
      // Locate the target jar and move it to the bin directory.
      if (jar.getName().endsWith("dependencies.jar")) {
        Util.moveFile(jar, new File(getBinRoot(), OutputFile));
        break;
      }
    }
  }

  @NotNull
  private List<File> findPoms() {
    Log.trace("InstallRaml4JaxRS#findPoms()");

    // Version 3.0.7 of raml-for-jax-rs contains 46 pom files.  Oversize this a
    // bit for different versions.
    final var poms  = new ArrayList<File>(64);
    final var queue = new LinkedList<File>();

    queue.push(getBuildTargetDirectory());

    while (queue.peek() != null) {
      final var dir = queue.poll();

      Log.debug("Gathering pom files from directory %s", dir);

      //noinspection ConstantConditions
      for (final var child : dir.listFiles()) {
        if (child.isDirectory()) {
          if (!child.getName().startsWith(".") && !child.getName().equals("src"))
            queue.push(child);
        } else if (child.getName().equals("pom.xml")) {
          Log.debug("Located pom file %s", child);
          poms.add(child);
        }
      }
    }

    return poms;
  }

  private void correctPoms(@NotNull final List<File> poms) {
    Log.trace("InstallRaml4JaxRS#correctPoms(poms = %s)", poms);

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
