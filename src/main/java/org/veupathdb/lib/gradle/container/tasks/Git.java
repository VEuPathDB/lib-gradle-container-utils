package org.veupathdb.lib.gradle.container.tasks;

import java.io.File;

public class Git {
  private static final String[] DefaultBranches = {"master", "main"};

  // Git command parts
  private static final String
    Git      = "git",
    Checkout = "checkout",
    Clone    = "clone",
    FBranch  = "--branch",
    FDepth   = "--depth",
    FQuiet   = "--quiet";

  public static class GitExtension {
    private String version;

    public String getVersion() {
      return version;
    }

    public void setVersion(String version) {
      this.version = version;
    }

    public boolean isDefault() {
      if (version == null)
        return true;

      for (final var branch : DefaultBranches)
        if (branch.equals(version))
          return true;

      return false;
    }
  }

  static File clone(final String url, final File parent) {
    try {
      final var proc = Runtime.getRuntime().exec(new String[]{Git, Clone, FDepth, "1", FQuiet, url}, new String[0], parent);
      if (proc.waitFor() != 0) {
        throw new RuntimeException(new String(proc.getErrorStream().readAllBytes()));
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to clone repo " + url, e);
    }

    return new File(parent, url.substring(url.lastIndexOf('/') + 1));
  }

  static File clone(final String url, final File parent, final String branch) {
    try {
      final var proc = Runtime.getRuntime()
        .exec(
          new String[]{Git, Clone, FDepth, "1", FBranch, branch, FQuiet, url},
          new String[0],
          parent
        );

      if (proc.waitFor() != 0)
        throw new RuntimeException(new String(proc.getErrorStream().readAllBytes()));
    } catch (Exception e) {
      throw new RuntimeException("Failed to clone repo " + url, e);
    }

    return new File(parent, url.substring(url.lastIndexOf('/') + 1));
  }

  static void checkout(final File repo, final String target) {
    try {
      Runtime.getRuntime().exec(new String[]{Git, Checkout, target}, new String[0], repo);
    } catch (Exception e) {
      throw new RuntimeException("Failed to checkout " + target + " in repo " + repo, e);
    }
  }
}
