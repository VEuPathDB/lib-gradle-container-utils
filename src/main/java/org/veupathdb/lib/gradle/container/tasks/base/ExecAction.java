package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class ExecAction extends Action {

  @Internal
  @NotNull
  protected abstract File getWorkDirectory();

  @Internal
  @NotNull
  protected abstract String getCommandName();

  @Internal
  @Nullable
  protected File getStdOutRedirect() {
    //noinspection ConstantConditions
    return log().getter(null);
  }

  protected void appendArguments(@NotNull final List<String> args) {
    log().noop(args);
  }

  protected void appendEnvironment(@NotNull final Map<String, String> env) {
    log().noop(env);
  }

  @Override
  protected void execute() {
    log().open();

    final var com = new ProcessBuilder(getCommandName());

    appendArguments(com.command());
    appendEnvironment(com.environment());

    final var outFile = getStdOutRedirect();
    if (outFile == null)
      com.redirectOutput(ProcessBuilder.Redirect.DISCARD);
    else
      com.redirectOutput(outFile);

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

      proc = com.start();

      status = proc.waitFor();
      if (status != 0) {
        final var err = new String(proc.getErrorStream().readAllBytes());
        log().error("Command {} execution failed with status code {}: {}", getCommandName(), status, err);
        throw new RuntimeException("Command " + getCommandName() + " execution failed with status code " + status + ": " + err);
      }
    } catch (IOException | InterruptedException e) {
      log().error("Command {} execution failed", this::getCommandName);
      throw new RuntimeException("Command " + getCommandName() + " execution failed", e);
    }

    postExec();

    log().close();
  }

  protected void postExec() {
    log().noop();
  }

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
