package org.veupathdb.lib.gradle.container.tasks;

import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.exec.Git;
import org.veupathdb.lib.gradle.container.exec.Maven;
import org.veupathdb.lib.gradle.container.tasks.base.VendorBuildAction;

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
    return Log.getter(new File(getDependencyRoot(), LockFile));
  }

  @Override
  @NotNull
  protected File download() {
    Log.open();

    Log.info("Cloning FgpUtil");

    final var git  = new Git(Log);
    final var repo = git.clone(URL, getDependencyRoot());
    final var vers = getConfiguredVersion();
    final var targ = Git.Target.of(vers);

    if (!targ.isDefault()) {
      Log.info("Switching FgpUtil to " + vers);

      git.checkout(repo, targ);
    }

    return Log.close(repo);
  }

  @Override
  protected void clean() {
    Log.open();

    Log.info("Removing old FgpUtil jar files");

    Util.listChildren(getDependencyRoot())
      .filter(this::fileFilter)
      .forEach(this::fileDelete);

    Log.close();
  }

  @Override
  @Internal
  @NotNull
  protected String getConfiguredVersion() {
    return Log.getter(Options.getFgpUtilVersion());
  }

  @Override
  protected void install() {
    Log.open();

    Log.info("Building FgpUtil");

    final var mvn = new Maven(Log);
    final var dir = getBuildTargetDirectory();

    mvn.cleanInstall(dir);

    Util.moveFilesTo(getDependencyRoot(), mvn.findOutputJars(dir));

    Log.close();
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  private boolean fileFilter(@NotNull final File file) {
    return Log.map(file, file.isFile() && file.getName().startsWith("fgputil"));
  }

  private void fileDelete(@NotNull final File file) {
    Log.open(file);

    Log.debug("Deleting file %s", file);

    if (!file.delete()) {
      Log.error("Failed to delete file " + file);
      throw new RuntimeException("Failed to delete file " + file);
    }

    Log.close();
  }
}
