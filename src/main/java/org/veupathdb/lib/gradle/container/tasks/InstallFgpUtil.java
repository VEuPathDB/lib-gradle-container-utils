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
    return log().getter(new File(getDependencyRoot(), LockFile));
  }

  @Override
  @NotNull
  protected File download() {
    log().open();

    log().info("Cloning FgpUtil");

    final var git  = new Git(log());
    final var repo = git.clone(URL, getDependencyRoot());
    final var vers = getConfiguredVersion();
    final var targ = Git.Target.of(vers);

    if (!targ.isDefault()) {
      log().info("Switching FgpUtil to " + vers);

      git.checkout(repo, targ);
    }

    return log().close(repo);
  }

  @Override
  protected void clean() {
    log().open();

    log().info("Removing old FgpUtil jar files");

    util().listChildren(getDependencyRoot())
      .filter(this::fileFilter)
      .forEach(this::fileDelete);

    log().close();
  }

  @Override
  @Internal
  @NotNull
  protected String getConfiguredVersion() {
    return log().getter(getOptions().getFgpUtilVersion());
  }

  @Override
  protected void install() {
    log().open();

    log().info("Building FgpUtil");

    final var mvn = new Maven(log());
    final var dir = getBuildTargetDirectory();

    mvn.cleanInstall(dir);

    util().moveFilesTo(getDependencyRoot(), mvn.findOutputJars(dir));

    log().close();
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  private boolean fileFilter(@NotNull final File file) {
    return log().map(file, file.isFile() && file.getName().startsWith("fgputil"));
  }

  private void fileDelete(@NotNull final File file) {
    log().open(file);

    log().debug("Deleting file %s", file);

    if (!file.delete()) {
      log().error("Failed to delete file " + file);
      throw new RuntimeException("Failed to delete file " + file);
    }

    log().close();
  }
}
