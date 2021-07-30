package org.veupathdb.lib.gradle.container.tasks;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

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

  public Git(final Project project) {
    Log = project.getLogger();
  }

  File clone(final String url, final File parent) {
    try {
      final var proc = Runtime.getRuntime().exec(new String[]{Command, Clone, FDepth, "1", FQuiet, url}, new String[0], parent);

      if (proc.waitFor() != 0) {
        throw new RuntimeException(new String(proc.getErrorStream().readAllBytes()));
      }
    } catch (Exception e) {
      Log.error("Failed to clone " + url + " into dir " + parent);
      throw new RuntimeException("Failed to clone " + url + " into dir " + parent + url, e);
    }

    return new File(parent, url.substring(url.lastIndexOf('/') + 1));
  }

  File clone(final String url, final File parent, final String branch) {
    try {
      final var proc = Runtime.getRuntime()
        .exec(
          new String[]{Command, Clone, FDepth, "1", FBranch, branch, FQuiet, url},
          new String[0],
          parent
        );

      if (proc.waitFor() != 0)
        throw new RuntimeException(new String(proc.getErrorStream().readAllBytes()));
    } catch (Exception e) {
      Log.error("Failed to clone " + url + " at branch " + branch + " into dir " + parent);
      throw new RuntimeException("Failed to clone " + url + " at branch " + branch + " into dir " + parent, e);
    }

    return new File(parent, url.substring(url.lastIndexOf('/') + 1));
  }

  void checkout(final File repo, final Target target) {
    try {
      final var proc = Runtime.getRuntime()
        .exec(new String[]{Command, Checkout, target.name()}, new String[0], repo);

      if (proc.waitFor() != 0) {
        throw new RuntimeException(new String(proc.getErrorStream().readAllBytes()));
      }
    } catch (Exception e) {
      Log.error("Failed to checkout " + target + " in repo " + repo);
      throw new RuntimeException("Failed to checkout " + target + " in repo " + repo, e);
    }
  }

  private static boolean isDefault(final String branchName) {
    if (branchName == null)
      throw new NullPointerException("Branch name cannot be null");

    for (final var branch : DefaultBranches)
      if (branch.equals(branchName))
        return true;

    return false;

  }

  public static class GitExtension {
    private Target version;

    public Target getVersion() {
      return version == null ? Target.Default : version;
    }

    public void setVersion(String version) {
      this.version = Target.of(version);
    }

    public boolean isDefault() {
      return version == null || version == Target.Default;
    }
  }

  public interface Target {
    Target Default = () -> "main";

    static Target of(final String branchName) {
      if (branchName == null)
        return Default;

      if (Git.isDefault(branchName))
        return Default;

      return () -> branchName;
    }

    String name();

    default boolean isDefault() {
      return Git.isDefault(name());
    }

    default boolean is(final Target other) {
      return this == other || this.name().equals(other.name());
    }
  }
}
