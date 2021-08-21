package org.veupathdb.lib.gradle.container.tasks.fgputil;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.exec.git.Git;
import org.veupathdb.lib.gradle.container.exec.Maven;
import org.veupathdb.lib.gradle.container.exec.git.GitTarget;
import org.veupathdb.lib.gradle.container.tasks.base.build.VendorBuildAction;

import java.io.File;

/**
 * FgpUtil Installation
 * <p>
 * Downloads, compiles, and installs FgpUtil into a project's configured vendor
 * directory.
 *
 * @since 1.0.0
 */
public class InstallFgpUtil extends VendorBuildAction {

  public static final String TaskName = "install-fgputil";

  private static final String TargetName = "FgpUtil";
  private static final String LockFile   = "fgputil.lock";

  @Override
  @NotNull
  protected String getDependencyName() {
    return TargetName;
  }

  @Override
  @NotNull
  public String pluginDescription() {
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
    final var repo = git.clone(buildConfiguration().getUrl(), getDependencyRoot());
    final var vers = buildConfiguration().getTargetVersion();
    final var targ = GitTarget.of(vers);

    if (!targ.isDefault()) {
      log().info("Switching FgpUtil to {}", vers);

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
  protected void install() {
    log().open();

    log().info("Building FgpUtil");

    final var mvn = new Maven(log());
    final var dir = getBuildTargetDirectory();

    mvn.cleanInstall(dir);

    util().moveFilesTo(getDependencyRoot(), mvn.findOutputJars(dir));

    log().close();
  }

  @Override
  protected FgpUtilConfiguration buildConfiguration() {
    return getOptions().getFgpUtilConfig();
  }

  //
  //
  // Internal Methods
  //
  //

  /**
   * Predicate that tests whether the given input file is FgpUtil related.
   *
   * @param file File to test.
   *
   * @return Whether the file is FgpUtil related.
   *
   * @since 1.1.0
   */
  private boolean fileFilter(@NotNull final File file) {
    return log().map(file, file.isFile() && file.getName().startsWith("fgputil"));
  }

  /**
   * Deletes the given target file.
   *
   * @param file File to delete.
   *
   * @since 1.1.0
   */
  private void fileDelete(@NotNull final File file) {
    log().open(file);

    log().debug("Deleting file {}", file);

    if (!file.delete()) {
      log().fatal("Failed to delete file {}", file);
    }

    log().close();
  }

}
