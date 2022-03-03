package org.veupathdb.lib.gradle.container.exec.git

import org.veupathdb.lib.gradle.container.util.Logger

import java.io.File

/**
 * Git Wrapper
 * <p>
 * Provides methods for performing {@code git} actions.
 *
 * @since 1.0.0
 */
class Git(val Log: Logger) {

  companion object {
    // Git command parts
    const val Command  = "git"
    const val Checkout = "checkout"
    const val Clone    = "clone"
    const val FBranch  = "--branch"
    const val FDepth   = "--depth"
    const val FQuiet   = "--quiet"
  }

  init {
    Log.constructor(Log)
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
  fun clone(url: String, parent: File): File {
    Log.open(url, parent)

    Log.debug("Performing a full clone of {}", url)

    try {
      val proc = Runtime.getRuntime()
        .exec(arrayOf(Command, Clone, FQuiet, url), emptyArray(), parent)

      if (proc.waitFor() != 0) {
        throw RuntimeException(String(proc.errorStream.readAllBytes()).trim())
      }
    } catch (e: Exception) {
      Log.fatal(e, "Failed to clone {} into dir {}", url, parent)
    }

    return Log.close(File(
      parent,
      url.substring(url.lastIndexOf('/') + 1).replace(".git", "")
    ))
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
  fun shallowClone(url: String, parent: File): File {
    Log.open(url, parent)

    Log.debug("Performing a shallow clone of {}", url)

    try {
      val proc = Runtime.getRuntime()
        .exec(arrayOf(Command, Clone, FQuiet, FDepth, "1", url), emptyArray(), parent)

      if (proc.waitFor() != 0) {
        throw RuntimeException(String(proc.errorStream.readAllBytes()).trim())
      }
    } catch (e: Exception) {
      Log.fatal(e, "Failed to clone {} into dir {}", url, parent)
    }

    return Log.close(File(
      parent,
      url.substring(url.lastIndexOf('/') + 1).replace(".git", "")
    ))
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
  fun clone(url: String, parent: File, branch: String): File {
    Log.open(url, parent, branch)

    Log.debug("Performing a full clone of {} at branch {}", url, branch)

    try {
      val proc = Runtime.getRuntime()
        .exec(
          arrayOf(Command, Clone, FBranch, branch, FQuiet, url),
          emptyArray(),
          parent
        )

      if (proc.waitFor() != 0)
        throw RuntimeException(String(proc.errorStream.readAllBytes()).trim())
    } catch (e: Exception) {
      Log.fatal(e, "Failed to clone {} at branch {} into dir {}", url, branch, parent)
    }

    return Log.close(File(
      parent,
      url.substring(url.lastIndexOf('/') + 1).replace(".git", "")
    ))
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
  fun shallowClone(url: String, parent: File, branch: String): File {
    Log.open(url, parent, branch)

    Log.debug("Performing a shallow clone of {} at branch {}", url, branch)

    try {
      val proc = Runtime.getRuntime()
        .exec(
          arrayOf(Command, Clone, FDepth, "1", FBranch, branch, FQuiet, url),
          emptyArray(),
          parent
        )

      if (proc.waitFor() != 0)
        throw RuntimeException(String(proc.getErrorStream().readAllBytes()).trim())
    } catch (e: Exception) {
      Log.fatal(e, "Failed to clone {} at branch {} into dir {}", url, branch, parent)
    }

    return Log.close(File(
      parent,
      url.substring(url.lastIndexOf('/') + 1).replace(".git", "")
    ))
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
  fun checkout(repo: File, target: GitTarget) {
    Log.open(repo, target)

    Log.debug("Checking out target {} in local repo {}", target, repo)

    try {
      val proc = Runtime.getRuntime()
        .exec(arrayOf(Command, Checkout, target.name()), emptyArray(), repo)

      if (proc.waitFor() != 0) {
        throw RuntimeException(String(proc.getErrorStream().readAllBytes()))
      }
    } catch (e: Exception) {
      Log.fatal(e, "Failed to checkout {} in repo {}", target, repo)
    }

    Log.close()
  }
}
