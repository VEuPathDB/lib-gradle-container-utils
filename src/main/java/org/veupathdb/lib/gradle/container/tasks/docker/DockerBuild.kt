package org.veupathdb.lib.gradle.container.tasks.docker

import org.gradle.api.tasks.Internal
import org.veupathdb.lib.gradle.container.config.RedirectConfig
import org.veupathdb.lib.gradle.container.tasks.base.exec.ExecAction

import java.io.File
import java.io.IOException
import java.util.Arrays

/**
 * Docker Build
 * <p>
 * Executes {@code docker build} on this project's root Dockerfile.
 * <p>
 *
 * TODO: This should tag the built image as :latest, and then add an additional
 *       tag using the version from the project config.
 */
open class DockerBuild : ExecAction() {

  companion object {
    const val TaskName = "build-docker"

    const val TaskDescription = "Builds this project's docker image.  " +
      "WARNING: This task requires that your user is a member of the " +
      "\"docker\" group."

    //
    //
    // Error Messages
    //
    //

    private const val ErrMissingUsername = "Missing required GitHub username " +
      "property.  Please ensure that you have configured your global " +
      "gradle.properties or set the credentials on your local env."

    private const val ErrMissingToken = "Missing required GitHub token " +
      "property.  Please ensure that you have configured your global " +
      "gradle.properties or set the credentials on your local env."

    private const val ErrDockerAccess = "Docker test failed.  Please ensure " +
      "docker is installed and that your user is a member of the `docker` " +
      "group."

    //
    //
    // Property Keys
    //
    //

    private const val EnvGithubUsername = "GITHUB_USERNAME"

    private const val EnvGithubToken = "GITHUB_TOKEN"

    private const val PropGithubUsername = "gpr.user"

    private const val PropGithubToken = "gpr.key"

    //
    //
    // Command Components
    //
    //

    private const val Command = "docker"

    private const val ArgBuild = "build"

    private const val ArgPS = "ps"

    private const val FlagFile = "--file"

    private const val FlagNoCache = "--no-cache"

    private const val FlagImageName = "--tag"

    private const val FlagBuildArg = "--build-arg"

    //
    //
    // Command Relevant Files
    //
    //

    private const val Dockerfile = "Dockerfile"
  }

  //
  //
  // Override/Implementation Methods
  //
  //


  override fun fillIncrementalInputFiles(files: MutableCollection<File>) {
    super.fillIncrementalInputFiles(files)
    files.add(getDockerFile())
  }

  override fun execute() {
    log.open()

    log.info("Testing Docker access")
    testDockerAccess()

    super.execute()

    log.close()
  }

  @Internal
  override fun getWorkDirectory() = RootDir

  @Internal
  override fun getCommandName() = Command

  override val pluginDescription get() = TaskDescription

  @Internal
  override fun getStdOutRedirect() = RedirectConfig.toStdOut()

  @Internal
  override fun getStdErrRedirect() = RedirectConfig.toStdErr()

  override fun appendArguments(args: MutableList<String>) {
    log.open()

    // Append mandatory args/flags.
    args.addAll(Arrays.asList(
      ArgBuild,
      FlagNoCache,
      FlagFile,
      getDockerFile().getPath(),
      FlagImageName,
      makeDockerImageName(),
      FlagBuildArg + "=" + EnvGithubUsername + "=" + findGithubUsername(),
      FlagBuildArg + "=" + EnvGithubToken + "=" + findGitHubToken()
    ))

    // Append configured extra args/flags.
    args.addAll(execConfiguration.arguments)

    // This must go last.
    args.add(execConfiguration.context)

    log.close()
  }

  override val execConfiguration get() = options.docker

  //
  //
  // Internal Methods
  //
  //

  private fun getDockerFile() = File(execConfiguration.context, Dockerfile)

  private fun makeDockerImageName() = log.getter(execConfiguration.imageName)

  private fun findGithubUsername(): String {
    log.open()

    val username = getPropOrEnv(PropGithubUsername, EnvGithubUsername)

    if (username.isEmpty)
      log.fatal<Nothing>(ErrMissingUsername)
    else
      return log.close(username.get())
  }

  private fun findGitHubToken(): String {
    log.open()

    val token = getPropOrEnv(PropGithubToken, EnvGithubToken)

    if (token.isEmpty)
      log.fatal<Nothing>(ErrMissingToken)
    else
      return log.close(token.get())
  }

  /**
   * Tests that we can execute docker commands without sudo.
   */
  private fun testDockerAccess() {
    log.open()

    try {
      val proc = ProcessBuilder()
        .command(Command, ArgPS)
        .start()

      if (proc.waitFor() != 0) {
        log.fatal<String>(ErrDockerAccess)
      }
    } catch (e: IOException) {
      log.fatal(e, "Failed to test docker access.")
    } catch (e: InterruptedException) {
      log.fatal(e, "Failed to test docker access.")
    }

    log.close()
  }
}
