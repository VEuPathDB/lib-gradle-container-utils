package org.veupathdb.lib.gradle.container.tasks.base.exec

import org.gradle.api.tasks.Internal
import org.veupathdb.lib.gradle.container.config.RedirectConfig
import org.veupathdb.lib.gradle.container.tasks.base.Action

import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Exec Action
 *
 * Represents a task that executes an external command.
 *
 * @since 1.1.0
 */
abstract class ExecAction : Action() {

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
  @Internal
  protected abstract fun getWorkDirectory(): File

  /**
   * Returns the name of the command to execute.
   *
   * @return The name of the command to execute.
   *
   * @since 1.1.0
   */
  @Internal
  protected abstract fun getCommandName(): String

  @get:Internal
  protected abstract val execConfiguration: ExecConfiguration

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
  @Internal
  protected open fun getStdOutRedirect(): RedirectConfig? {
    return log.getter(null)
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
  @Internal
  protected open fun getStdErrRedirect(): RedirectConfig? {
    return log.getter(null)
  }

  /**
   * Extension point for adding arguments to the target command.
   *
   * @param args Argument list being built.
   *
   * @since 1.1.0
   */
  protected open fun appendArguments(args: MutableList<String>) {
    log.noop(args)
  }

  /**
   * Extension point for adding variables to the environment for the target
   * command.
   *
   * @param env Environment map being built.
   *
   * @since 1.1.0
   */
  protected open fun appendEnvironment(env: MutableMap<String, String>) {
    log.open()
    env.putAll(execConfiguration.environment)
    log.close()
  }

  override fun execute() {
    log.open()

    // Create a new process builder with this ExecAction's configured command.
    val com = ProcessBuilder(getCommandName())

    // Build the argument list for the target command.
    appendArguments(com.command())

    // Build the environment for the target command.
    appendEnvironment(com.environment())

    // Set the work directory for the target command.
    com.directory(getWorkDirectory())

    val proc: Process
    val status: Int

    try {
      log.debug(
        "\n  Executing:\n    {}\n  In directory:\n    {}",
        { com.command().joinToString(" ") },
        com::directory
      )
      logComStart(com)

      // Start the process for the command.
      proc = com.start()

      // Configure the pipe for the command's stdout.  If no custom
      // configuration is provided, pipe stdout to /dev/null
      var pipe: RedirectConfig? = getStdOutRedirect() ?: RedirectConfig.toDevNull()
      pipe!!.forStdOut(proc)

      // Prepare a buffer that may be used to collect the process' stderr if
      // no custom configuration is provided.
      var errBuf: ByteArrayOutputStream? = null

      // If a custom stderr configuration is provided, pipe the target command's
      // stderr to the configured location.
      pipe = getStdErrRedirect()
      if (pipe != null)
        pipe.forStdErr(proc)
      // Else append it to the byte buffer to use when logging the error in the
      // event that the target command fails.
      else {
        errBuf = ByteArrayOutputStream()
        RedirectConfig.toStream(errBuf).forStdErr(proc)
      }

      // Wait for the command to finish.
      status = proc.waitFor()

      // If the command failed, log the error.
      if (status != 0) {

        // If we aren't using a custom output pipe for stderr, then collect the
        // stderr output from the byte buffer and return it in the log and
        // exception.
        if (errBuf != null) {
          val err = errBuf.toString()

          log.error("Command {} execution failed with status code {}: {}", getCommandName(), status, err)
          throw RuntimeException("Command " + getCommandName() + " execution failed with status code " + status + ": " + err)
        }

        // If we are using a custom output pipe for stderr, then we don't have
        // the error output to log.  Instead, append a notification that stderr
        // was logged to a custom location.
        log.error("Command {} execution failed with status code {}.  StdErr written to custom buffer.", getCommandName(), status)
        throw RuntimeException("Command " + getCommandName() + " execution failed with status code " + status)
      }
    } catch (e: Exception) {
      log.error("Command {} execution failed", this::getCommandName)
      throw RuntimeException("Command " + getCommandName() + " execution failed", e)
    }

    // Execute any post-command tasks.
    // These are only executed if the command completes successfully.
    postExec()

    log.close()
  }

  /**
   * Post command execution hook.
   *
   * This method will only be called if the command completes successfully.
   *
   * @since 1.1.0
   */
  protected open fun postExec() {
    log.noop()
  }

  /**
   * Log the start of the target command.
   *
   * @param com Command definition.
   *
   * @since 1.1.0
   */
  private fun logComStart(com: ProcessBuilder) {
    log.info {
      val args = com.command()

      return@info when (args.size) {
        1    -> String.format("Executing command `%s`\n", args[0])
        2    -> String.format("Executing command `%s %s`", args[0], args[1])
        3    -> String.format("Executing command `%s %s %s`\n",
          args[0],
          args[1],
          args[2])
        else -> String.format("Executing command `%s %s %s ...`\n",
          args[0],
          args[1],
          args[2])
      }
    }
  }
}
