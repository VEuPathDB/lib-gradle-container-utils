package org.veupathdb.lib.gradle.container.tasks.docker;

import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.config.RedirectConfig;
import org.veupathdb.lib.gradle.container.tasks.base.exec.ExecAction;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Docker Build
 * <p>
 * Executes {@code docker build} on this project's root Dockerfile.
 * <p>
 *
 * TODO: This should tag the built image as :latest, and then add an additional
 *       tag using the version from the project config.
 */
public class DockerBuild extends ExecAction {
  public static final  String TaskName        = "build-docker";
  private static final String TaskDescription = "Builds this project's docker image.  WARNING: This task requires that "
    + "your user is a member of the \"docker\" group.";

  //
  //
  // Error Messages
  //
  //
  private static final String ErrMissingUsername = "Missing required GitHub username property.  Please ensure that you "
    + "have configured your global gradle.properties or set the credentials on your local env.";
  private static final String ErrMissingToken    = "Missing required GitHub token property.  Please ensure that you "
    + "have configured your global gradle.properties or set the credentials on your local env.";
  private static final String ErrDockerAccess    = "Docker test failed.  Please ensure docker is installed and that "
    + "your user is a member of the `docker` group.";

  //
  //
  // Property Keys
  //
  //
  private static final String EnvGithubUsername  = "GITHUB_USERNAME";
  private static final String EnvGithubToken     = "GITHUB_TOKEN";
  private static final String PropGithubUsername = "gpr.user";
  private static final String PropGithubToken    = "gpr.key";

  //
  //
  // Command Components
  //
  //
  private static final String Command       = "docker";
  private static final String ArgBuild      = "build";
  private static final String FlagFile      = "--file";
  private static final String FlagNoCache   = "--no-cache";
  private static final String FlagImageName = "--tag";
  private static final String FlagBuildArg  = "--build-arg";

  //
  //
  // Command Relevant Files
  //
  //
  private static final File   StdOutFile = new File("docker-stdout.log");
  private static final File   StdErrFile = new File("docker-stderr.log");
  private static final String Dockerfile = "Dockerfile";

  //
  //
  // Override/Implementation Methods
  //
  //


  @Override
  public void fillIncrementalInputFiles(@NotNull Collection<File> files) {
    super.fillIncrementalInputFiles(files);
    files.add(getDockerFile());
  }

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
    StdErrFile.delete();
    StdOutFile.delete();
  }

  @Internal
  @Override
  protected @NotNull File getWorkDirectory() {
    return RootDir;
  }

  @Internal
  @Override
  protected @NotNull String getCommandName() {
    return Command;
  }

  @Override
  public @NotNull String pluginDescription() {
    return TaskDescription;
  }

  @Internal
  @Override
  protected @NotNull Optional<RedirectConfig> getStdOutRedirect() {
    return Optional.of(RedirectConfig.toFile(StdOutFile));
  }

  @Internal
  @Override
  protected @NotNull Optional<RedirectConfig> getStdErrRedirect() {
    return Optional.of(RedirectConfig.toFile(StdErrFile));
  }

  @Override
  protected void appendArguments(@NotNull List<String> args) {
    log().open();

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
    ));

    // Append configured extra args/flags.
    args.addAll(execConfiguration().getArguments());

    // This must go last.
    args.add(execConfiguration().getContext());

    log().close();
  }

  @Override
  protected @NotNull DockerConfig execConfiguration() {
    return getOptions().getDockerConfig();
  }

  //
  //
  // Internal Methods
  //
  //

  @NotNull
  private File getDockerFile() {
    return new File(execConfiguration().getContext(), Dockerfile);
  }

  private @NotNull String makeDockerImageName() {
    log().open();
    return log().close(execConfiguration().getImageName());
  }

  private @NotNull String findGithubUsername() {
    log().open();

    var username = getPropOrEnv(PropGithubUsername, EnvGithubUsername);

    if (username.isEmpty())
      log().fatal(ErrMissingUsername);
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
      log().fatal(ErrMissingToken);
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
        log().fatal(ErrDockerAccess);
      }
    } catch (IOException | InterruptedException e) {
      log().fatal(e, "Failed to test docker access.");
    }

    log().close();
  }
}
