package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.tasks.Internal;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class ExecAction extends Action {

  @Internal
  protected abstract File getWorkDirectory();

  @Internal
  protected abstract String getCommandName();

  @Internal
  protected File getStdOutRedirect() {
    return null;
  }

  protected void appendArguments(final List<String> args) {
    // Override me to add args
  }

  protected void appendEnvironment(final Map<String, String> env) {
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
      Log.debug("Executing command {} in directory {}", com.command(), com.directory());

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
}
