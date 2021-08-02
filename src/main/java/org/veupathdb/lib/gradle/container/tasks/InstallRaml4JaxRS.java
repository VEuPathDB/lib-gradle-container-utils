package org.veupathdb.lib.gradle.container.tasks;

import org.veupathdb.lib.gradle.container.exec.Git;
import org.veupathdb.lib.gradle.container.exec.Maven;
import org.veupathdb.lib.gradle.container.tasks.base.BinBuildAction;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class InstallRaml4JaxRS extends BinBuildAction {

  public static final String TaskName = "ramlGenInstall";

  static final String  LockFile       = "raml4jaxrs.lock";
  static final String  DefaultVersion = "3.0.7";
  static final String  GitURL         = "https://github.com/mulesoft-labs/raml-for-jax-rs.git";
  static final Pattern VersionMatch   = Pattern.compile("\\d+\\.\\d+\\.\\d+-SNAPSHOT");
  static final String  OutputFile     = "raml-to-jaxrs.jar";

  @Override
  protected String getDependencyName() {
    return "Raml for JaxRS";
  }

  @Override
  protected File getLockFile() {
    return new File(getDependencyRoot(), LockFile);
  }

  @Override
  protected String pluginDescription() {
    return "Builds and installs the Raml for JaxRS generator.";
  }

  @Override
  protected String getConfiguredVersion() {
    final String tmp;
    return (tmp = getOptions().getRamlForJaxRSVersion()) == null ? DefaultVersion : tmp;
  }

  @Override
  protected void clean() {

  }

  @Override
  protected File download() {
    Log.trace("InstallRaml4JaxRS#download()");

    System.out.println("Cloning " + getDependencyName());

    return new Git(getProject()).shallowClone(GitURL, getDependencyRoot(), getConfiguredVersion());
  }

  @Override
  protected void install() {
    Log.trace("InstallRaml4JaxRS#install()");

    correctPoms(findPoms());

    System.out.println("Compiling " + getDependencyName());

    final var jars = new Maven(getProject())
      .cleanInstall(new File(getBuildTargetDirectory(), "raml-to-jaxrs/raml-to-jaxrs-cli"));

    try {
      for (final var jar : jars) {
        // Locate the target jar and move it to the bin directory.
        if (jar.getName().endsWith("dependencies.jar")) {
          Log.debug("Moving file {} to {}", jar, getDependencyRoot());

          Files.move(
            jar.toPath(),
            getBinRoot().toPath().resolve(OutputFile),
            StandardCopyOption.REPLACE_EXISTING
          );

          break;
        }
      }
    } catch (Exception e) {
      Log.error("Failed to move one or more jar files to the bin directory");
      throw new RuntimeException("Failed to move one or more jar files to the bin directory", e);
    }
  }

  private List<File> findPoms() {
    Log.trace("InstallRaml4JaxRS#getPoms(File)");

    // Version 3.0.7 of raml-for-jax-rs contains 46 pom files.  Oversize this a
    // bit for different versions.
    final var poms  = new ArrayList<File>(64);
    final var queue = new LinkedList<File>();

    queue.push(getBuildTargetDirectory());

    while (queue.peek() != null) {
      final var dir = queue.poll();

      Log.debug("Gathering pom files from directory {}", dir);

      //noinspection ConstantConditions
      for (final var child : dir.listFiles()) {
        if (child.isDirectory()) {
          if (!child.getName().startsWith(".") && !child.getName().equals("src"))
            queue.push(child);
        } else if (child.getName().equals("pom.xml")) {
          Log.debug("Located pom file {}", child);
          poms.add(child);
        }
      }
    }

    return poms;
  }

  private void correctPoms(final List<File> poms) {
    Log.trace("InstallRaml4JaxRS#correctPoms(List)");

    System.out.println("Patching " + getDependencyName() + " pom files");

    try {
      for (final var pom : poms) {
        Log.debug("Patching {}", pom);

        final var pp = pom.toPath();

        Files.writeString(
          pp,
          VersionMatch.matcher(Files.readString(pp)).replaceAll(getConfiguredVersion()),
          StandardOpenOption.TRUNCATE_EXISTING
        );
      }
    } catch (Exception e) {
      Log.error("Failed to correct pom files");
      throw new RuntimeException("Failed to correct pom files", e);
    }
  }
}
