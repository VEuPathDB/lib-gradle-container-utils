package org.veupathdb.lib.gradle.container.tasks;

import org.gradle.api.tasks.Internal;
import org.veupathdb.lib.gradle.container.tasks.base.VendorBuildAction;
import org.veupathdb.lib.gradle.container.exec.Git;
import org.veupathdb.lib.gradle.container.exec.Maven;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class InstallFgpUtil extends VendorBuildAction {

  public static final String TaskName = "fgputilInstall";

  private static final String TargetName = "FgpUtil";
  private static final String LockFile   = "fgputil.lock";
  private static final String URL        = "https://github.com/VEuPathDB/FgpUtil";

  @Override
  protected String getDependencyName() {
    return TargetName;
  }

  @Override
  protected String pluginDescription() {
    return "Download, build, and vendor FgpUtil";
  }

  @Override
  protected File getLockFile() {
    return new File(getDependencyRoot(), LockFile);
  }

  @Override
  protected File download() {
    Log.trace("InstallFgpUtil#download()");

    System.out.println("Cloning FgpUtil");

    final var git  = new Git(getProject());
    final var repo = git.clone(URL, getDependencyRoot());
    final var vers = getConfiguredVersion();
    final var targ = Git.Target.of(vers);

    if (!targ.isDefault()) {
      System.out.println("Switching FgpUtil to " + vers);

      git.checkout(repo, targ);
    }

    return repo;
  }

  @Override
  protected void clean() {
    Log.trace("InstallFgpUtil#clean()");

    System.out.println("Removing old FgpUtil jar files");

    //noinspection ConstantConditions
    for (final var file : getDependencyRoot().listFiles()) {
      Log.debug("Deleting file " + file);

      if (!file.delete()) {
        Log.error("Failed to delete file " + file);
        throw new RuntimeException("Failed to delete file " + file);
      }
    }
  }

  @Override
  @Internal
  protected String getConfiguredVersion() {
    final String tmp;
    return (tmp = getOptions().getFgpUtilVersion()) == null
      ? Git.Target.Default.name()
      : tmp;
  }

  @Override
  protected void install() {
    Log.trace("InstallFgpUtil#install()");

    System.out.println("Building FgpUtil");

    final var jars = new Maven(getProject()).cleanInstall(getBuildTargetDirectory());

    try {
      for (final var jar : jars) {
        Files.move(
          jar.toPath(),
          getDependencyRoot().toPath().resolve(jar.getName()),
          StandardCopyOption.REPLACE_EXISTING
        );
      }
    } catch (Exception e) {
      Log.error("Failed to move one or more jar files to the vendor directory");
      throw new RuntimeException("Failed to move one or more jar files to the vendor directory", e);
    }
  }
}
