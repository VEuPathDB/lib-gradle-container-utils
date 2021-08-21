package org.veupathdb.lib.gradle.container.exec.git;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.util.Logger;

import java.io.File;

/**
 * Git Wrapper
 * <p>
 * Provides methods for performing {@code git} actions.
 *
 * @since 1.0.0
 */
public class Git {
  // Git command parts
  private static final String
    Command  = "git",
    Checkout = "checkout",
    Clone    = "clone",
    FBranch  = "--branch",
    FDepth   = "--depth",
    FQuiet   = "--quiet";


  private final Logger Log;

  public Git(@NotNull final Logger log) {
    log.constructor(log);
    Log = log;
  }

  /**
   * Performs a full clone of the target repository in the given working
   * directory.
   * <p>
   * This method will clone the Git repository's default branch.
   *
   * @param url    URL of the repository to clone.
   * @param parent Workdir the git command will be run in.
   *
   * @return The directory containing the newly checked out git repository.
   *
   * @since 1.0.0
   */
  @NotNull
  public File clone(@NotNull final String url, @NotNull final File parent) {
    Log.open(url, parent);

    Log.debug("Performing a full clone of {}", url);

    try {
      final var proc = Runtime.getRuntime()
        .exec(new String[]{Command, Clone, FQuiet, url}, new String[0], parent);

      if (proc.waitFor() != 0) {
        throw new RuntimeException(new String(proc.getErrorStream().readAllBytes()).trim());
      }
    } catch (Exception e) {
      Log.fatal(e, "Failed to clone {} into dir {}", url, parent);
    }

    return Log.close(new File(
      parent,
      url.substring(url.lastIndexOf('/') + 1).replace(".git", "")
    ));
  }

  /**
   * Performs a shallow clone of the target repository in the given working
   * directory.
   * <p>
   * This method will clone the Git repository's default branch.
   *
   * @param url    URL of the repository to clone.
   * @param parent Workdir the git command will be run in.
   *
   * @return The directory containing the newly checked out git repository.
   *
   * @since 1.0.0
   */
  @NotNull
  public File shallowClone(@NotNull final String url, @NotNull final File parent) {
    Log.open(url, parent);

    Log.debug("Performing a shallow clone of {}", url);

    try {
      final var proc = Runtime.getRuntime()
        .exec(new String[]{Command, Clone, FQuiet, FDepth, "1", url}, new String[0], parent);

      if (proc.waitFor() != 0) {
        throw new RuntimeException(new String(proc.getErrorStream().readAllBytes()).trim());
      }
    } catch (Exception e) {
      Log.fatal(e, "Failed to clone {} into dir {}", url, parent);
    }

    return Log.close(new File(
      parent,
      url.substring(url.lastIndexOf('/') + 1).replace(".git", "")
    ));
  }

  /**
   * Performs a full clone of the target repository at the specified target
   * branch in the given working directory.
   *
   * @param url    URL of the repository to clone.
   * @param parent Workdir the git command will be run in.
   * @param branch Target Git branch to clone the repo at.
   *
   * @return The directory containing the newly checked out git repository.
   *
   * @since 1.1.0
   */
  @NotNull
  public File clone(
    @NotNull final String url,
    @NotNull final File parent,
    @NotNull final String branch
  ) {
    Log.open(url, parent, branch);

    Log.debug("Performing a full clone of {} at branch {}", url, branch);

    try {
      final var proc = Runtime.getRuntime()
        .exec(
          new String[]{Command, Clone, FBranch, branch, FQuiet, url},
          new String[0],
          parent
        );

      if (proc.waitFor() != 0)
        throw new RuntimeException(new String(proc.getErrorStream().readAllBytes()).trim());
    } catch (Exception e) {
      Log.fatal(e, "Failed to clone {} at branch {} into dir {}", url, branch, parent);
    }

    return Log.close(new File(
      parent,
      url.substring(url.lastIndexOf('/') + 1).replace(".git", "")
    ));
  }

  /**
   * Performs a shallow clone of the target repository at the specified target
   * branch in the given working directory.
   *
   * @param url    URL of the repository to clone.
   * @param parent Workdir the git command will be run in.
   * @param branch Target Git branch to clone the repo at.
   *
   * @return The directory containing the newly checked out git repository.
   *
   * @since 1.1.0
   */
  @NotNull
  public File shallowClone(
    @NotNull final String url,
    @NotNull final File parent,
    @NotNull final String branch
  ) {
    Log.open(url, parent, branch);

    Log.debug("Performing a shallow clone of {} at branch {}", url, branch);

    try {
      final var proc = Runtime.getRuntime()
        .exec(
          new String[]{Command, Clone, FDepth, "1", FBranch, branch, FQuiet, url},
          new String[0],
          parent
        );

      if (proc.waitFor() != 0)
        throw new RuntimeException(new String(proc.getErrorStream().readAllBytes()).trim());
    } catch (Exception e) {
      Log.fatal(e, "Failed to clone {} at branch {} into dir {}", url, branch, parent);
    }

    return Log.close(new File(
      parent,
      url.substring(url.lastIndexOf('/') + 1).replace(".git", "")
    ));
  }

  /**
   * Performs a {@code git checkout} in the given directory {@code repo}.
   *
   * @param repo   Target directory in which the checkout will be performed.
   * @param target Git Target specifying the tag, branch, or commit hash to
   *               checkout.
   *
   * @since 1.0.0
   */
  public void checkout(@NotNull final File repo, @NotNull final GitTarget target) {
    Log.open(repo, target);

    Log.debug("Checking out target {} in local repo {}", target, repo);

    try {
      final var proc = Runtime.getRuntime()
        .exec(new String[]{Command, Checkout, target.name()}, new String[0], repo);

      if (proc.waitFor() != 0) {
        throw new RuntimeException(new String(proc.getErrorStream().readAllBytes()));
      }
    } catch (Exception e) {
      Log.fatal(e, "Failed to checkout {} in repo {}", target, repo);
    }

    Log.close();
  }
}
