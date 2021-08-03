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
    return null;
  }

  protected void appendArguments(@NotNull final List<String> args) {
    // Override me to add args
  }

  protected void appendEnvironment(@NotNull final Map<String, String> env) {
    // Override me to add extra environment vars
  }

  @Override
  protected void execute() {
    Log.trace("ExecAction#execute()");

    final var com = new ProcessBuilder(getCommandName());

    appendArguments(com.command());
    appendEnvironment(com.environment());

    final var outFile = getStdOutRedirect();
    if (outFile == null)
      com.redirectOutput(ProcessBuilder.Redirect.DISCARD);
    else
      com.redirectOutput(outFile);

    com.directory(getWorkDirectory());

    Process proc = null;
    int status;

    try {
      if (Log.isDebugEnabled()) {
        Log.debug(
          "Executing\n    {}\nIn directory {}",
          String.join(" ", com.command()),
          com.directory()
        );
      }
      logComStart(com);

      proc = com.start();

      status = proc.waitFor();
      if (status != 0) {
        final var err = new String(proc.getErrorStream().readAllBytes());
        Log.error("Command " + getCommandName() + " execution failed with status code " + status + ": " + err);
        throw new RuntimeException("Command " + getCommandName() + " execution failed with status code " + status + ": " + err);
      }
    } catch (IOException | InterruptedException e) {
      Log.error("Command " + getCommandName() + " execution failed");
      throw new RuntimeException("Command " + getCommandName() + " execution failed", e);
    }

    postExec();
  }

  protected void postExec() {}

  private void logComStart(@NotNull final ProcessBuilder com) {
    final var args = com.command();

    switch (args.size()) {
      case 1  -> System.out.println("Executing command " + args.get(0));
      case 2  -> System.out.println("Executing command " + args.get(0) + " " + args.get(1));
      case 3  -> System.out.printf("Executing command %s %s %s\n", args.get(0), args.get(1), args.get(2));
      default -> System.out.printf("Executing command %s %s %s ...\n", args.get(0), args.get(1), args.get(2));
    }
  }
}
