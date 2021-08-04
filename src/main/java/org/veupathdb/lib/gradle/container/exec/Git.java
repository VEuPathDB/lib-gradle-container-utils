package org.veupathdb.lib.gradle.container.exec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.veupathdb.lib.gradle.container.util.Logger;

import java.io.File;

public class Git {
  private static final String[] DefaultBranches = {"master", "main"};

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
   *
   * @param url    URL of the repository to clone.
   * @param parent Workdir the git command will be run in.
   *
   * @return The directory containing the newly checked out git repository.
   */
  @NotNull
  public File clone(@NotNull final String url, @NotNull final File parent) {
    Log.open(url, parent);

    Log.debug("Performing a full clone of {}", url);

    try {
      final var proc = Runtime.getRuntime().exec(new String[]{Command, Clone, FQuiet, url}, new String[0], parent);

      if (proc.waitFor() != 0) {
        throw new RuntimeException(new String(proc.getErrorStream().readAllBytes()).trim());
      }
    } catch (Exception e) {
      Log.error("Failed to clone {} into dir {}", url, parent);
      throw new RuntimeException("Failed to clone " + url + " into dir " + parent + url, e);
    }

    return Log.close(new File(
      parent,
      url.substring(url.lastIndexOf('/') + 1).replace(".git", "")
    ));
  }

  /**
   * Performs a shallow clone of the target repository in the given working
   * directory.
   *
   * @param url    URL of the repository to clone.
   * @param parent Workdir the git command will be run in.
   *
   * @return The directory containing the newly checked out git repository.
   */
  @NotNull
  public File shallowClone(@NotNull final String url, @NotNull final File parent) {
    Log.open(url, parent);

    Log.debug("Performing a shallow clone of {}", url);

    try {
      final var proc = Runtime.getRuntime().exec(new String[]{Command, Clone, FQuiet, FDepth, "1", url}, new String[0], parent);

      if (proc.waitFor() != 0) {
        throw new RuntimeException(new String(proc.getErrorStream().readAllBytes()).trim());
      }
    } catch (Exception e) {
      Log.error("Failed to clone {} into dir {}", url, parent);
      throw new RuntimeException("Failed to clone " + url + " into dir " + parent + url, e);
    }

    return Log.close(new File(
      parent,
      url.substring(url.lastIndexOf('/') + 1).replace(".git", "")
    ));
  }

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
      Log.error("Failed to clone {} at branch {} into dir {}", url, branch, parent);
      throw new RuntimeException("Failed to clone " + url + " at branch " + branch + " into dir " + parent, e);
    }

    return Log.close(new File(
      parent,
      url.substring(url.lastIndexOf('/') + 1).replace(".git", "")
    ));
  }

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
      Log.error("Failed to clone {} at branch {} into dir {}", url, branch, parent);
      throw new RuntimeException("Failed to clone " + url + " at branch " + branch + " into dir " + parent, e);
    }

    return Log.close(new File(
      parent,
      url.substring(url.lastIndexOf('/') + 1).replace(".git", "")
    ));
  }

  public void checkout(@NotNull final File repo, @NotNull final Target target) {
    Log.open(repo, target);

    Log.debug("Checking out target {} in local repo {}", target, repo);

    try {
      final var proc = Runtime.getRuntime()
        .exec(new String[]{Command, Checkout, target.name()}, new String[0], repo);

      if (proc.waitFor() != 0) {
        throw new RuntimeException(new String(proc.getErrorStream().readAllBytes()));
      }
    } catch (Exception e) {
      Log.error("Failed to checkout {} in repo {}", target, repo);
      throw new RuntimeException("Failed to checkout " + target + " in repo " + repo, e);
    }

    Log.close();
  }

  private static boolean isDefault(@NotNull final String branchName) {
    for (final var branch : DefaultBranches)
      if (branch.equals(branchName))
        return true;

    return false;
  }

  public interface Target {
    Target Default = new TargetImpl("main");

    @NotNull
    static Target of(@Nullable final String branchName) {
      if (branchName == null)
        return Default;

      if (Git.isDefault(branchName))
        return Default;

      return new TargetImpl(branchName);
    }

    @NotNull
    String name();

    default boolean isDefault() {
      return Git.isDefault(name());
    }

    default boolean is(@NotNull final Target other) {
      return this == other || this.name().equals(other.name());
    }
  }

  private static final class TargetImpl implements Target {
    @NotNull
    private final String name;

    private TargetImpl(@NotNull final String name) {
      this.name = name;
    }

    @Override
    @NotNull
    public String name() {
      return name;
    }

    @Override
    @NotNull
    public String toString() {
      return "Target{name=" + name + "}";
    }
  }
}
