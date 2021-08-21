package org.veupathdb.lib.gradle.container.tasks.base.exec;

import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.config.RedirectConfig;
import org.veupathdb.lib.gradle.container.tasks.base.Action;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Exec Action
 *
 * Represents a task that executes an external command.
 *
 * @since 1.1.0
 */
public abstract class ExecAction extends Action {

  //
  //
  // Abstract Methods
  //
  //

  /**
   * Returns the working directory in which the target command will be executed.
   *
   * @return The working directory in which the target command will be executed.
   *
   * @since 1.1.0
   */
  @NotNull
  @Internal
  protected abstract File getWorkDirectory();

  /**
   * Returns the name of the command to execute.
   *
   * @return The name of the command to execute.
   *
   * @since 1.1.0
   */
  @NotNull
  @Internal
  protected abstract String getCommandName();

  @NotNull
  protected abstract ExecConfiguration execConfiguration();

  //
  //
  // Instance Methods
  //
  //

  /**
   * Returns an optional redirect configuration for the target command's stdout
   * stream.
   * <p>
   * If the returned option is empty, the command's stdout stream will be
   * discarded.
   *
   * @return An optional stdout redirect config.
   *
   * @since 1.1.0
   */
  @NotNull
  @Internal
  protected Optional<RedirectConfig> getStdOutRedirect() {
    return log().getter(Optional.empty());
  }

  /**
   * Returns an optional redirect configuration for the target command's stderr
   * stream.
   * <p>
   * If the returned option is empty, the command's stderr stream be gathered to
   * an internal buffer and logged in the event that the target command fails.
   *
   * @return An optional stderr redirect config.
   */
  @NotNull
  @Internal
  protected Optional<RedirectConfig> getStdErrRedirect() {
    return log().getter(Optional.empty());
  }

  /**
   * Extension point for adding arguments to the target command.
   *
   * @param args Argument list being built.
   *
   * @since 1.1.0
   */
  protected void appendArguments(@NotNull final List<String> args) {
    log().noop(args);
  }

  /**
   * Extension point for adding variables to the environment for the target
   * command.
   *
   * @param env Environment map being built.
   *
   * @since 1.1.0
   */
  protected void appendEnvironment(@NotNull final Map<String, String> env) {
    log().open();
    env.putAll(execConfiguration().getEnvironment());
    log().close();
  }

  @Override
  public void execute() {
    log().open();

    // Create a new process builder with this ExecAction's configured command.
    final var com = new ProcessBuilder(getCommandName());

    // Build the argument list for the target command.
    appendArguments(com.command());

    // Build the environment for the target command.
    appendEnvironment(com.environment());

    // Set the work directory for the target command.
    com.directory(getWorkDirectory());

    Process proc;
    int status;

    try {
      log().debug(
        "\n  Executing:\n    {}\n  In directory:\n    {}",
        () -> String.join(" ", com.command()),
        com::directory
      );
      logComStart(com);

      // Start the process for the command.
      proc = com.start();

      // Configure the pipe for the command's stdout.  If no custom
      // configuration is provided, pipe stdout to /dev/null
      var pipe = getStdOutRedirect().orElse(RedirectConfig.toDevNull());
      pipe.forStdOut(proc);

      // Prepare a buffer that may be used to collect the process' stderr if
      // no custom configuration is provided.
      ByteArrayOutputStream errBuf = null;

      // If a custom stderr configuration is provided, pipe the target command's
      // stderr to the configured location.
      if ((pipe = getStdErrRedirect().orElse(null)) != null)
        pipe.forStdErr(proc);
      // Else append it to the byte buffer to use when logging the error in the
      // event that the target command fails.
      else
        RedirectConfig.toStream(errBuf = new ByteArrayOutputStream()).forStdErr(proc);

      // Wait for the command to finish.
      status = proc.waitFor();

      // If the command failed, log the error.
      if (status != 0) {

        // If we aren't using a custom output pipe for stderr, then collect the
        // stderr output from the byte buffer and return it in the log and
        // exception.
        if (errBuf != null) {
          final var err = errBuf.toString();

          log().error("Command {} execution failed with status code {}: {}", getCommandName(), status, err);
          throw new RuntimeException("Command " + getCommandName() + " execution failed with status code " + status + ": " + err);
        }

        // If we are using a custom output pipe for stderr, then we don't have
        // the error output to log.  Instead, append a notification that stderr
        // was logged to a custom location.
        log().error("Command {} execution failed with status code {}.  StdErr written to custom buffer.", getCommandName(), status);
        throw new RuntimeException("Command " + getCommandName() + " execution failed with status code " + status);
      }
    } catch (IOException | InterruptedException e) {
      log().error("Command {} execution failed", this::getCommandName);
      throw new RuntimeException("Command " + getCommandName() + " execution failed", e);
    }

    // Execute any post-command tasks.
    // These are only executed if the command completes successfully.
    postExec();

    log().close();
  }

  /**
   * Post command execution hook.
   *
   * This method will only be called if the command completes successfully.
   *
   * @since 1.1.0
   */
  protected void postExec() {
    log().noop();
  }

  /**
   * Log the start of the target command.
   *
   * @param com Command definition.
   *
   * @since 1.1.0
   */
  private void logComStart(@NotNull final ProcessBuilder com) {
    log().info(() -> {
      final var args = com.command();

      return switch (args.size()) {
        case 1  -> String.format("Executing command `%s`\n", args.get(0));
        case 2  -> String.format("Executing command `%s %s`", args.get(0), args.get(1));
        case 3  -> String.format("Executing command `%s %s %s`\n", args.get(0), args.get(1), args.get(2));
        default -> String.format("Executing command `%s %s %s ...`\n", args.get(0), args.get(1), args.get(2));
      };
    });
  }
}
