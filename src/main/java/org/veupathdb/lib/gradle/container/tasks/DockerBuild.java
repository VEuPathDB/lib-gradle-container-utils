package org.veupathdb.lib.gradle.container.tasks;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.config.RedirectConfig;
import org.veupathdb.lib.gradle.container.tasks.base.ExecAction;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DockerBuild extends ExecAction {
  public static final String TaskName = "build-docker";

  private static final String EnvGithubUsername  = "GITHUB_USERNAME";
  private static final String EnvGithubToken     = "GITHUB_TOKEN";
  private static final String PropGithubUsername = "gpr.user";
  private static final String PropGithubToken    = "gpr.key";

  private static final String Command       = "docker";
  private static final String ArgBuild      = "build";
  private static final String FlagNoCache   = "--no-cache";
  private static final String FlagImageName = "-t";
  private static final String FlagBuildArg  = "--build-arg";

  private static final String TaskDescription = "Builds this project's docker image.  WARNING: This task requires that "
    + "your user is a member of the \"docker\" group.";

  private static final File stdOutFile = new File("docker-stdout.log");
  private static final File stdErrFile = new File("docker-stderr.log");

  @Override
  public void execute() {
    log().open();

    log().info("Testing Docker access");
    testDockerAccess();

    super.execute();

    log().close();
  }

  @Override
  protected void postExec() {
    stdErrFile.delete();
    stdOutFile.delete();
  }

  @Override
  protected @NotNull File getWorkDirectory() {
    return RootDir;
  }

  @Override
  protected @NotNull String getCommandName() {
    return Command;
  }

  @Override
  public @NotNull String pluginDescription() {
    return TaskDescription;
  }

  @Override
  protected @NotNull Optional<RedirectConfig> getStdOutRedirect() {
    return Optional.of(RedirectConfig.toFile(stdOutFile));
  }

  @Override
  protected @NotNull Optional<RedirectConfig> getStdErrRedirect() {
    return Optional.of(RedirectConfig.toFile(stdErrFile));
  }

  @Override
  protected void appendArguments(@NotNull List<String> args) {
    log().open();

    args.addAll(Arrays.asList(
      ArgBuild,
      FlagNoCache,
      FlagImageName,
      makeDockerImageName(),
      FlagBuildArg + "=" + EnvGithubUsername + "=" + findGithubUsername(),
      FlagBuildArg + "=" + EnvGithubToken + "=" + findGitHubToken(),
      getOptions().getDockerContext()
    ));

    log().close();
  }

  private @NotNull String makeDockerImageName() {
    log().open();
    return log().close(serviceProperties().containerName());
  }

  private @NotNull String findGithubUsername() {
    log().open();

    var username = getPropOrEnv(PropGithubUsername, EnvGithubUsername);

    if (username.isEmpty())
      log().fatal("Missing required GitHub username property.  Please ensure that you have configured your global gradle.properties or set the credentials on your local env.");
    else
      return log().close(username.get());

    // This is never reached, but required since log().fatal() doesn't count as
    // throwing an exception.
    return "";
  }

  private @NotNull String findGitHubToken() {
    log().open();

    var token = getPropOrEnv(PropGithubToken, EnvGithubToken);

    if (token.isEmpty())
      log().fatal("Missing required GitHub token property.  Please ensure that you have configured your global gradle.properties or set the credentials on your local env.");
    else
      return log().close(token.get());

    // This is never reached, but required since log().fatal() doesn't count as
    // throwing an exception.
    return "";
  }

  /**
   * Tests that we can execute docker commands without sudo.
   */
  private void testDockerAccess() {
    log().open();

    try {
      final var proc = new ProcessBuilder()
        .command("docker", "ps")
        .start();

      if (proc.waitFor() != 0) {
        log().fatal("Docker test failed.  Please ensure docker is installed and that your user is a member of the `docker` group.");
      }
    } catch (IOException | InterruptedException e) {
      log().fatal(e, "Failed to test docker access.");
    }

    log().close();
  }
}
