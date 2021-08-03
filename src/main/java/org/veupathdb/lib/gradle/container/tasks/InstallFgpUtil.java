package org.veupathdb.lib.gradle.container.tasks;

import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.tasks.base.VendorBuildAction;
import org.veupathdb.lib.gradle.container.exec.Git;
import org.veupathdb.lib.gradle.container.exec.Maven;

import java.io.File;

public class InstallFgpUtil extends VendorBuildAction {

  public static final String TaskName = "fgputilInstall";

  private static final String TargetName = "FgpUtil";
  private static final String LockFile   = "fgputil.lock";
  private static final String URL        = "https://github.com/VEuPathDB/FgpUtil";

  @Override
  @NotNull
  protected String getDependencyName() {
    return TargetName;
  }

  @Override
  @NotNull
  protected String pluginDescription() {
    return "Download, build, and vendor FgpUtil";
  }

  @Override
  @NotNull
  protected File getLockFile() {
    return new File(getDependencyRoot(), LockFile);
  }

  @Override
  @NotNull
  protected File download() {
    Log.trace("InstallFgpUtil#download()");

    Log.info("Cloning FgpUtil");

    final var git  = new Git(getProject());
    final var repo = git.clone(URL, getDependencyRoot());
    final var vers = getConfiguredVersion();
    final var targ = Git.Target.of(vers);

    if (!targ.isDefault()) {
      Log.info("Switching FgpUtil to " + vers);

      git.checkout(repo, targ);
    }

    return repo;
  }

  @Override
  protected void clean() {
    Log.trace("InstallFgpUtil#clean()");

    Log.info("Removing old FgpUtil jar files");

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
  @NotNull
  protected String getConfiguredVersion() {
    return Options.getFgpUtilVersion();
  }

  @Override
  protected void install() {
    Log.trace("InstallFgpUtil#install()");
    Log.info("Building FgpUtil");

    Util.moveFilesTo(getDependencyRoot(), new Maven(Log).cleanInstall(getBuildTargetDirectory()));
  }
}
