@file:Suppress("DuplicatedCode")

package org.veupathdb.lib.gradle.container.util

import java.io.*
import java.lang.reflect.Array
import java.nio.file.Path

private operator fun (() -> Logger.Level).compareTo(lvl: Logger.Level) =
  this().compareTo(lvl)

/**
 * Plugin Tree Logger.
 * <p>
 * Provides methods for logging messages at various levels of severity to the
 * console.
 *
 * @since 1.1.0
 */
// TODO: allow configuring threshold for what goes to stdout and what goes to stderr.
@Suppress("unused")
class Logger(level: () -> Level, rootDir: File) {

  /**
   * @since 3.0.0
   */
  @JvmInline
  value class Level(val value: Int) {
    companion object {
      val None = Level(0)
      val Fatal = Level(0)
      val Error = Level(1)
      val Warn = Level(2)
      val Info = Level(3)
      val Debug = Level(4)
      val Trace = Level(5)
    }

    operator fun compareTo(lvl: Level) = compareTo(lvl.value)

    operator fun compareTo(lvl: Int) =
      when {
        value < lvl -> -1
        value > lvl -> 1
        else        -> 0
      }
  }

  companion object {

    /**
     * Pattern for logging no-arg method call starts.
     *
     * @since 1.1.0
     */
    const val OpenNoArg = "{}#{}()"

    /**
     * Pattern for logging single argument method call starts.
     *
     * @since 1.1.0
     */
    const val OpenOneArg = "{}#{}({})"

    /**
     * Pattern for logging double argument method call starts.
     *
     * @since 1.1.0
     */
    const val OpenTwoArg = "{}#{}({}, {})"

    /**
     * Pattern for logging triple argument method call starts.
     *
     * @since 1.1.0
     */
    const val OpenThreeArg = "{}#{}({}, {}, {})"

    /**
     * Pattern for logging single argument constructors.
     *
     * @since 1.1.0
     */
    const val ConstructorOneArg = "{}#new({})"

    /**
     * Pattern for logging double argument constructors.
     *
     * @since 1.1.0
     */
    const val ConstructorTwoArg = "{}#new({}, {})"

    /**
     * Pattern for logging zero argument no-op methods.
     *
     * @since 1.1.0
     */
    const val NoOpNoArg = "{}#{}(): no-op"

    /**
     * Pattern for logging single argument no-op methods.
     *
     * @since 1.1.0
     */
    const val NoOpOneArg = "{}#{}({}): no-op"

    /**
     * Pattern for logging mapping methods.
     *
     * @since 1.1.0
     */
    const val MapPattern = "{}#{}({}): {}"

    @JvmStatic
    val isTTY = System.getenv("DOCKER") == null

    @JvmStatic
    private val ColorPrefixes = arrayOf(
      "\u001B[31m".toCharArray(), // Fatal
      "\u001B[91m".toCharArray(), // Error
      "\u001B[93m".toCharArray(), // Warning
      "\u001B[37m".toCharArray(), // Info
      "\u001B[94m".toCharArray(), // Debug
      "\u001B[90m".toCharArray(), // Trace
    )

    @JvmStatic
    private val ColorReset = "\u001B[39m".toCharArray()

    /**
     * Log level prefix strings.
     *
     * @since 1.1.0
     */
    @JvmStatic
    private val LevelPrefixes = arrayOf(
      "[FATAL] ".toCharArray(),
      "[ERROR] ".toCharArray(),
      " [WARN] ".toCharArray(),
      " [INFO] ".toCharArray(),
      "[DEBUG] ".toCharArray(),
      "[TRACE] ".toCharArray(),
    )

    /**
     * Injection point string.
     *
     * @since 1.1.0
     */
    private const val Inject = "{}"

    /**
     * Length of the injection point string.
     *
     * @since 1.1.0
     */
    private const val ISize = 2

    /**
     * Tree continuation prefix.
     * <p>
     * Used for generating tree prefixes greater than the number of cached
     * prefixes in {@link #padding}.
     *
     * @since 1.1.0
     */
    private const val bar = "  | "

    /**
     * Tree call prefix.
     * <p>
     * Used for generating tree prefixes greater than the number of cached
     * prefixes in {@link #padding}.
     *
     * @since 1.1.0
     */
    private const val com = "  |- "

    /**
     * Cached pre-built tree logging prefix strings for call stacks up to a depth
     * of 15.
     *
     * @since 1.1.0
     */
    @JvmStatic
    private val padding = arrayOf(
      "",
      " |- ",
      " |   |- ",
      " |   |   |- ",
      " |   |   |   |- ",
      " |   |   |   |   |- ",
      " |   |   |   |   |   |- ",
      " |   |   |   |   |   |   |- ",
      " |   |   |   |   |   |   |   |- ",
      " |   |   |   |   |   |   |   |   |- ",
      " |   |   |   |   |   |   |   |   |   |- ",
      " |   |   |   |   |   |   |   |   |   |   |- ",
      " |   |   |   |   |   |   |   |   |   |   |   |- ",
      " |   |   |   |   |   |   |   |   |   |   |   |   |- ",
      " |   |   |   |   |   |   |   |   |   |   |   |   |   |- ",
    )

    /**
     * Static char array representing the timestamp prefix.
     *
     *
     * This value will be copied and modified as necessary to represent the number
     * of milliseconds that have passed since the logger was created.
     *
     *
     * Max supported execution time of 9,999,999ms or 4.444444 hours.
     *
     * @since 1.1.0
     */
    private val timeBuffer =
      charArrayOf('[', '0', '0', '0', '0', '0', '0', '0', ']', ' ')
  }

  /**
   * Currently configured logging level.
   *
   * @since 1.1.0
   */
  val logLevel: () -> Level = level

  /**
   * Project path.
   * <p>
   * Used for trimming file paths down to relative paths for log readability.
   *
   * @since 1.1.0
   */
  private val projPath: String = rootDir.path

  /**
   * Standard logging writer.
   * <p>
   * Messages below the error threshold will be appended to this writer.
   *
   * @since 1.1.0
   */
  private val writer: BufferedWriter = BufferedWriter(PrintWriter(System.out))

  /**
   * Timestamp for when this logger instance was created.
   * <p>
   * Used for tracking task execution times in milliseconds.
   *
   * @since 1.1.0
   */
  private val start: Long = System.currentTimeMillis()

  /**
   * Call stack size counter.
   * <p>
   * Used to track call stack depth for tree logging.
   *
   * @since 1.1.0
   */
  private var call: Int = 0

  init {
    this.constructor(level, rootDir)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  val isTrace get() = this.logLevel >= Level.Trace

  val isDebug get() = this.logLevel >= Level.Debug

  val isInfo get() = this.logLevel >= Level.Info

  val isWarn get() = this.logLevel >= Level.Warn

  val isError get() = this.logLevel >= Level.Error

  val isDisabled get() = this.logLevel() == Level.None

  ////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Increments the call stack count and, if trace logging is enabled, prints
   * the class and method name of the caller.
   *
   * @since 1.1.0
   */
  fun open() {
    if (this.logLevel >= Level.Trace) {
      val stack = Exception().stackTrace[1]
      log(
        Level.Trace,
        OpenNoArg,
        stack.className.substring(stack.className.lastIndexOf('.') + 1),
        stack.methodName
      )
    }
    call++
  }

  /**
   * Increments the call stack count and, if trace logging is enabled, prints
   * the class name, method name, and first argument of the caller.
   *
   * @param arg1 First argument of the calling method.
   *
   * @since 1.1.0
   */
  fun open(arg1: Any?) {
    if (this.logLevel >= Level.Trace) {
      val stack = Exception().stackTrace[1]
      log(
        Level.Trace,
        OpenOneArg,
        stack.className.substring(stack.className.lastIndexOf('.') + 1),
        stack.methodName,
        arg1
      )
    }
    call++
  }

  /**
   * Increments the call stack count and, if trace logging is enabled, prints
   * the class name, method name, and arguments of the caller.
   *
   * @param arg1 First argument of the calling method.
   * @param arg2 Second argument of the calling method.
   *
   * @since 1.1.0
   */
  fun open(arg1: Any?, arg2: Any?) {
    if (this.logLevel >= Level.Trace) {
      val stack = Exception().stackTrace[1]
      log(
        Level.Trace,
        OpenTwoArg,
        stack.className.substring(stack.className.lastIndexOf('.') + 1),
        stack.methodName,
        arg1,
        arg2
      )
    }
    call++
  }

  /**
   * Increments the call stack count and, if trace logging is enabled, prints
   * the class name, method name, and arguments of the caller.
   *
   * @param arg1 First argument of the calling method.
   *
   * @param arg2 Second argument of the calling method.
   *
   * @param arg3 Third argument of the calling method.
   *
   * @since 1.1.0
   */
  fun open(arg1: Any?, arg2: Any?, arg3: Any?) {
    if (this.logLevel >= Level.Trace) {
      val stack = Exception().stackTrace[1]
      log(
        Level.Trace,
        OpenThreeArg,
        stack.className.substring(stack.className.lastIndexOf('.') + 1),
        stack.methodName,
        arg1,
        arg2,
        arg3
      )
    }
    call++
  }

  /**
   * If {@link #Level.Trace} is enabled, prints a void return then decrements
   * the tree log level.
   *
   * @since 1.1.0
   */
  fun close() {
    log(Level.Trace, "return: void")
    call--
  }

  /**
   * If {@link #Level.Trace} is enabled, prints the given return value then
   * decrements the tree log level.
   *
   * @param value Passthrough value.
   *
   * @param T Type of the passthrough value.
   *
   * @return The given passthrough value.
   *
   * @since 1.1.0
   */
  fun <T> close(value: T): T {
    log(Level.Trace, "return: {}", value)
    call--
    return value
  }

  /**
   * If {@link #Level.Trace} is enabled, prints a getter method signature
   * including the given return value, returning the passed value.
   *
   * Example:
   * ```
   * class Bar {
   *   public String getFoo() {
   *     return log.getter("Hello world");
   *   }
   * }
   *
   * ////////
   *
   * new Bar().getFoo(); // Prints: Bar#getFoo(): Hello world
   * ```
   *
   * This method prints the log at the current tree log level and does not
   * increment it.
   *
   * @param value Passthrough value that will be returned.
   *
   * @param T Type of the passthrough value that will be returned.
   *
   * @return The given passthrough value.
   *
   * @since 1.1.0
   */
  fun <T> getter(value: T): T {
    if (this.logLevel >= Level.Trace) {
      val stack = Exception().stackTrace[1]
      log(
        Level.Trace,
        "{}#{}(): {}",
        stack.className.substring(stack.className.lastIndexOf('.') + 1),
        stack.methodName,
        value
      )
    }

    return value
  }

  /**
   * If {@link #Level.Trace} is enabled, prints a constructor method
   * signature including the given argument value.
   * <p>
   * Example:
   * <pre>{@code
   * class Bar {
   *   public Bar(String val) {
   *     log.constructor(val);
   *   }
   * }
   *
   * ////////
   *
   * new Bar("test"); // Prints: Bar#new(test)
   * }</pre>
   * <p>
   * This method prints the log at the current tree log level and does not
   * increment it.
   *
   * @param arg1 Argument passed to the calling constructor.
   *
   * @since 1.1.0
   */
  fun constructor(arg1: Any?) {
    if (this.logLevel >= Level.Trace) {
      val stack = Exception().stackTrace[1]
      log(
        Level.Trace,
        ConstructorOneArg,
        stack.className.substring(stack.className.lastIndexOf('.') + 1),
        arg1
      )
    }
  }

  /**
   * If {@link #Level.Trace} is enabled, prints a constructor method
   * signature including the given argument values.
   * <p>
   * Example:
   * <pre>{@code
   * class Bar {
   *   public Bar(String arg1, int arg2) {
   *     log.constructor(arg1, arg2);
   *   }
   * }
   *
   * ////////
   *
   * new Bar("test", 69); // Prints: Bar#new(test, 69)
   * }</pre>
   * <p>
   * This method prints the log at the current tree log level and does not
   * increment it.
   *
   * @param arg1 First argument passed to the calling constructor.
   * @param arg2 Second argument passed to the calling constructor.
   *
   * @since 1.1.0
   */
  fun constructor(arg1: Any?, arg2: Any?) {
    if (this.logLevel >= Level.Trace) {
      val stack = Exception().stackTrace[1]
      log(
        Level.Trace,
        ConstructorTwoArg,
        stack.className.substring(stack.className.lastIndexOf('.') + 1),
        arg1,
        arg2
      )
    }
  }

  /**
   * If {@link #Level.Trace} is enabled, prints the caller's function
   * signature.
   * <p>
   * Example:
   * <pre>{@code
   * class Bar {
   *   fun foo() {
   *     log.noop();
   *   }
   * }
   *
   * ////////
   *
   * new Bar().foo(); // Prints: Bar#foo(): no-op
   * }</pre>
   * <p>
   * This method prints the log at the current tree log level and does not
   * increment it.
   *
   * @since 1.1.0
   */
  fun noop() {
    if (this.logLevel >= Level.Trace) {
      val stack = Exception().stackTrace[1]
      log(
        Level.Trace,
        NoOpNoArg,
        stack.className.substring(stack.className.lastIndexOf('.') + 1),
        stack.methodName
      )
    }
  }

  /**
   * If {@link #Level.Trace} is enabled, prints the caller's function
   * signature including the given argument.
   * <p>
   * Example:
   * <pre>{@code
   * class Bar {
   *   fun foo(String hello) {
   *     log.noop(hello);
   *   }
   * }
   *
   * ////////
   *
   * new Bar().foo("gravy"); // Prints: Bar#foo(gravy): no-op
   * }</pre>
   * <p>
   * This method prints the log at the current tree log level and does not
   * increment it.
   *
   * @param arg1 Argument value to render in the noop trace log line.
   *
   * @since 1.1.0
   */
  fun noop(arg1: Any?) {
    if (this.logLevel >= Level.Trace) {
      val stack = Exception().stackTrace[1]
      log(
        Level.Trace,
        NoOpOneArg,
        stack.className.substring(stack.className.lastIndexOf('.') + 1),
        stack.methodName,
        arg1
      )
    }
  }

  /**
   * If {@link #Level.Trace} is enabled, prints the caller's function
   * signature including the given argument and return value.
   * <p>
   * Intended for use in "mapping" functions (one input, one output).
   * <p>
   * Example:
   * <pre>{@code
   * class Bar {
   *   public int foo(String hello) {
   *     return log.map(hello, Integer.parseInt(hello));
   *   }
   * }
   *
   * ////////
   *
   * new Bar().foo("666"); // Prints: Bar#foo(666): 666
   * }</pre>
   * <p>
   * This method prints the log at the current tree log level and does not
   * increment it.
   *
   * @param input The input argument to the calling function.
   *
   * @param output The return value from the calling function.
   *
   * @param T The type of the return value from the calling function.
   *
   * @return Returns the argument {@code out};
   *
   * @since 1.1.0
   */
  fun <T> map(input: Any?, output: T): T {
    if (this.logLevel >= Level.Trace) {
      val stack = Exception().stackTrace[1]
      log(
        Level.Trace,
        MapPattern,
        stack.className.substring(stack.className.lastIndexOf('.') + 1),
        stack.methodName,
        input,
        output
      )
    }

    return output
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * If {@link #Level.Error} is enabled, prints the given value to stdout.
   *
   * @param value Value to print.
   *
   * @since 1.1.0
   */
  fun error(value: Any?) {
    log(Level.Error, value)
  }

  /**
   * If {@link #Level.Error} is enabled, expands the template string using the
   * return value of the given {@code Supplier} and prints the result to stdout.
   *
   * @param fmt Template string.
   *
   * @param fn Method that will be called to retrieve the value to inject into
   * the template string.  This method will only be called if [Level.Error] is
   * enabled.
   *
   * @since 1.1.0
   */
  fun error(fmt: String, fn: () -> Any?) {
    log(Level.Error, fmt, fn)
  }

  /**
   * If {@link #Level.Error} is enabled, expands the template string using the
   * given argument and prints the result to stdout.
   *
   * @param fmt  Template string.
   * @param val1 Value to inject into the template string.
   *
   * @since 1.1.0
   */
  fun error(fmt: String, val1: Any?) {
    log(Level.Error, fmt, val1)
  }

  /**
   * If {@link #Level.Error} is enabled, expands the template string using the
   * given arguments and prints the result to stdout.
   *
   * @param fmt  Template string.
   * @param val1 First value to inject into the template string.
   * @param val2 Second value to inject into the template string.
   *
   * @since 1.1.0
   */
  fun error(fmt: String, val1: Any?, val2: Any?) {
    this.log(Level.Error, fmt, val1, val2)
  }

  /**
   * If {@link #Level.Error} is enabled, expands the template string using the
   * given arguments and prints the result to stdout.
   *
   * @param fmt  Template string.
   * @param val1 First value to inject into the template string.
   * @param val2 Second value to inject into the template string.
   * @param val3 Third value to inject into the template string.
   *
   * @since 1.1.0
   */
  fun error(
    fmt: String,
    val1: Any?,
    val2: Any?,
    val3: Any?,
  ) = log(Level.Error, fmt, val1, val2, val3)

  ////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * If {@link #Level.Info} is enabled, prints the given value to stdout.
   *
   * @param value Value to print.
   *
   * @since 1.1.0
   */
  fun info(value: Any?) = log(Level.Info, value)

  /**
   * If {@link #Level.Info} is enabled, prints the output of the given
   * {@code Supplier} to stdout.
   *
   * @param messageSupplier Method that will be called to retrieve the value
   *                        to print.  This method will only be called if
   *                        {@link #Level.Info}  is enabled.
   *
   * @throws NullPointerException if {@code messageSupplier} is {@code null}
   * @since 1.1.0
   */
  fun info(messageSupplier: () -> String) = log(Level.Info, messageSupplier)

  /**
   * If [Level.Info] is enabled, expands the template string using the return
   * value of the given supplier and prints the result to stdout.
   *
   * @param fmt Template string.
   *
   * @param fn1 Method that will be called to retrieve the value to inject into
   * the template string.  This method will only be called if [Level.Info] is
   * enabled.
   *
   * @since 1.1.0
   */
  fun info(fmt: String, fn1: () -> Any?) = log(Level.Info, fmt, fn1)

  /**
   * If {@link #Level.Info} is enabled, expands the template string using the
   * given argument and prints the result to stdout.
   *
   * @param fmt  Template string.
   * @param val1 Value to inject into the template string.
   *
   * @since 1.1.0
   */
  fun info(fmt: String, val1: Any?) = log(Level.Info, fmt, val1)

  ////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * If {@link #Level.Debug} is enabled, prints the given value to stdout.
   *
   * @param value Value to print.
   *
   * @since 1.1.0
   */
  fun debug(value: Any?) = log(Level.Debug, value)

  /**
   * If [Level.Debug] is enabled, expands the template string using the given
   * argument and prints the result to stdout.
   *
   * @param fmt  Template string.
   * @param val1 Value to inject into the template string.
   *
   * @since 1.1.0
   */
  fun debug(fmt: String, val1: Any?) = log(Level.Debug, fmt, val1)

  /**
   * If {@link #Level.Debug} is enabled, expands the template string using the
   * return value of the given {@code Supplier}s and prints the result to
   * stdout.
   *
   * @param fmt Template string.
   *
   * @param fn1 Method that will be called to retrieve the first value to inject
   * into the template string.  This method will only be called if
   * {@link #Level.Debug} is enabled.
   *
   * @param fn2 Method that will be called to retrieve the second value to
   * inject into the template string.  This method will only be
   * called if {@link #Level.Debug} is enabled.
   *
   * @since 1.1.0
   */
  fun debug(fmt: String, fn1: () -> Any?, fn2: () -> Any?) =
    log(Level.Debug, fmt, fn1, fn2)

  /**
   * If {@link #Level.Debug} is enabled, expands the template string using the
   * given arguments and prints the result to stdout.
   *
   * @param fmt  Template string.
   * @param val1 First value to inject into the template string.
   * @param val2 Second value to inject into the template string.
   *
   * @since 1.1.0
   */
  fun debug(fmt: String, val1: Any?, val2: Any?) =
    log(Level.Debug, fmt, val1, val2)

  //
  //
  // Fatal Level Logging
  //
  //

  //
  // Without an exception
  //

  fun <T>  fatal(value: Any?): T {
    val buf = StringWriter (64)

    write(BufferedWriter (buf), Level.Fatal, value)
    writeNoPrefix(writer, buf.toString())

    throw RuntimeException(buf.toString())
  }

  /**
   * Formats and logs the given message and argument, then throws a runtime
   * exception with that log text.
   *
   * @param fmt Log format string.
   *
   * @param value Argument to inject into the log format string.
   *
   * @param T Type of the "returned" value to enable using this method
   * execution as a return value.
   *
   * @return Nothing.  This method will always throw.
   *
   * @throws RuntimeException Thrown after the log is written.
   * @since 2.0.0
   */
  fun <T> fatal(fmt: String, value: Any?): T {
    val buf = StringWriter(128)

    write(BufferedWriter(buf), Level.Fatal, fmt, value)
    writeNoPrefix(writer, buf.toString())

    throw RuntimeException(buf.toString())
  }

  //
  // With an exception
  //

  fun <T> fatal(value: Throwable): T {
    write(writer, Level.Fatal, value.message)

    throw RuntimeException(value)
  }

  fun fatal(err: Throwable, value: Any?) {
    val buf = StringWriter(64)

    write(BufferedWriter(buf), Level.Fatal, value)
    writeNoPrefix(writer, buf.toString())

    throw RuntimeException(buf.toString(), err)
  }

  fun <T> fatal(err: Throwable, fmt: String, value: Any?): T {
    val buf = StringWriter(64)

    write(BufferedWriter(buf), Level.Fatal, fmt, value)
    writeNoPrefix(writer, buf.toString())

    throw RuntimeException(buf.toString(), err)
  }

  fun fatal(err: Throwable, fmt: String, val1: Any?, val2: Any?) {
    val buf = StringWriter(64)

    write(BufferedWriter(buf), Level.Fatal, fmt, val1, val2)
    writeNoPrefix(writer, buf.toString())

    throw RuntimeException(buf.toString(), err)
  }

  fun fatal(
    err: Throwable,
    fmt: String,
    val1: Any?,
    val2: Any?,
    val3: Any?,
  ) {
    val buf = StringWriter (64)

    write(BufferedWriter(buf), Level.Fatal, fmt, val1, val2, val3)
    writeNoPrefix(writer, buf.toString())

    throw RuntimeException(buf.toString(), err)
  }

  //
  //
  // Base Logging Methods
  //
  //

  /**
   * If the current configured log level is greater than or equal to the given
   * {@code level}, prints the output of the given {@code Supplier} to stdout.
   *
   * @param fn Method that will be called to retrieve the value to print.
   *           This method will not be called if logging at the specified level
   *           is not enabled.
   *
   * @throws NullPointerException if {@code messageSupplier} is {@code null}
   * @since 1.1.0
   */
  fun log(level: Level, fn: () -> Any?) {
    if (this.logLevel >= level) {
      write(level, fn())
    }
  }

  /**
   * If the current configured log level is greater than or equal to the given
   * {@code level}, prints the given value to stdout.
   *
   * @param value Value to print.
   *
   * @since 1.1.0
   */
  fun log(level: Level, value: Any?) {
    if (this.logLevel >= level) {
      write(level, value)
    }
  }

  /**
   * If the current configured log level is greater than or equal to the given
   * {@code level}, injects the output of the given {@code Supplier} into the
   * given format string and logs it.
   *
   * @param level Level to log the given message at.
   *
   * @param fmt Format string.
   *
   * @param fn1 Supplier for a value to inject into the format string.
   *
   * This function will not be called if logging at the specified level is not
   * enabled.
   *
   * @since 1.1.0
   */
  fun log(level: Level, fmt: String, fn1: () -> Any?) {
    if (this.logLevel >= level) {
      write(level, fmt, fn1())
    }
  }

  /**
   * If the current configured log level is greater than or equal to the given
   * {@code level}, injects the output of the given {@code Supplier}s into the
   * given format string and logs it.
   *
   * @param level Level to log the given message at.
   *
   * @param fmt Format string.
   *
   * @param fn1 Supplier for the first value to inject into the format string.
   *
   * This function will not be called if logging at the specified level is not
   * enabled.
   *
   * @param fn2 Supplier for the second value to inject into the format string.
   *
   * This function will not be called if logging at the specified level is not
   * enabled.
   *
   * @since 1.1.0
   */
  fun log(level: Level, fmt: String, fn1: () -> Any?, fn2: () -> Any?) {
    if (this.logLevel >= level) {
      write(level, fmt, fn1(), fn2())
    }
  }

  /**
   * If the current configured log level is greater than or equal to the given
   * {@code level}, expands the template string using the given argument and
   * prints the result to stdout.
   *
   * @param level Log level.
   * @param fmt   Template string.
   * @param val1  Value to inject into the template string.
   *
   * @since 1.1.0
   */
  fun log(level: Level, fmt: String, val1: Any?) {
    if (this.logLevel >= level) {
      write(level, fmt, val1)
    }
  }

  /**
   * If the current configured log level is greater than or equal to the given
   * {@code level}, expands the template string using the given arguments and
   * prints the result to stdout.
   *
   * @param level Log level.
   * @param fmt   Template string.
   * @param val1  First value to inject into the template string.
   * @param val2  Second value to inject into the template string.
   *
   * @since 1.1.0
   */
  fun log(
    level: Level,
    fmt: String,
    val1: Any?,
    val2: Any?,
  ) {
    if (this.logLevel >= level) {
      write(level, fmt, val1, val2)
    }
  }

  /**
   * If the current configured log level is greater than or equal to the given
   * {@code level}, expands the template string using the given arguments and
   * prints the result to stdout.
   *
   * @param level Log level.
   * @param fmt   Template string.
   * @param val1  First value to inject into the template string.
   * @param val2  Second value to inject into the template string.
   * @param val3  Third value to inject into the template string.
   *
   * @since 1.1.0
   */
  fun log(
    level: Level,
    fmt: String,
    val1: Any?,
    val2: Any?,
    val3: Any?,
  ) {
    if (this.logLevel >= level) {
      write(level, fmt, val1, val2, val3)
    }
  }

  /**
   * If the current configured log level is greater than or equal to the given
   * {@code level}, expands the template string using the given arguments and
   * prints the result to stdout.
   *
   * @param level Log level.
   * @param fmt   Template string.
   * @param val1  First value to inject into the template string.
   * @param val2  Second value to inject into the template string.
   * @param val3  Third value to inject into the template string.
   * @param val4  Fourth value to inject into the template string.
   *
   * @since 1.1.0
   */
  fun log(
    level: Level,
    fmt: String,
    val1: Any?,
    val2: Any?,
    val3: Any?,
    val4: Any?,
  ) {
    if (this.logLevel >= level) {
      write(level, fmt, val1, val2, val3, val4)
    }
  }

  /**
   * If the current configured log level is greater than or equal to the given
   * {@code level}, expands the template string using the given arguments and
   * prints the result to stdout.
   *
   * @param level Log level.
   * @param fmt   Template string.
   * @param val1  First value to inject into the template string.
   * @param val2  Second value to inject into the template string.
   * @param val3  Third value to inject into the template string.
   * @param val4  Fourth value to inject into the template string.
   * @param val5  Fifth value to inject into the template string.
   *
   * @since 1.1.0
   */
  fun log(
    level: Level,
    fmt: String,
    val1: Any?,
    val2: Any?,
    val3: Any?,
    val4: Any?,
    val5: Any?,
  ) {
    if (this.logLevel >= level) {
      write(level, fmt, val1, val2, val3, val4, val5)
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  override fun toString() = "Logger{level=$logLevel}"

  ////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Render the given value as a string.
   * <p>
   * Special cases:
   * <table>
   *   <tr>
   *     <td>Null input</td>
   *     <td>Returns {@code "null"}</td>
   *   </tr>
   *   <tr>
   *     <td>String input</td>
   *     <td>Returns the input value wrapped in quotes.</td>
   *   </tr>
   *   <tr>
   *     <td>File input</td>
   *     <td>If the file is contained in this project, returns the path relative
   *     to the project root.  If the file is not contained in this project,
   *     returns the full path to the file.</td>
   *   </tr>
   *   <tr>
   *     <td>Path input</td>
   *     <td>If the path is contained in this project, returns the path relative
   *     to the project root.  If the path is not contained in this project,
   *     returns the full path.</td>
   *   </tr>
   *   <tr>
   *     <td>Array input</td>
   *     <td>Returns the array printed in a format similar to that of
   *     {@link Arrays#toString()}</td>
   *   </tr>
   * </table>
   * <p>
   * All other input values will be printed via {@link Object#toString()}
   *
   * @param value Value to stringify.
   *
   * @return Stringified value.
   *
   * @since 1.1.0
   */
  internal fun stringify(value: Any?): String {
    val tmp: String

    if (value == null)
      return "null"

    if (value is String)
      return value

    val cls = value.javaClass

    if (cls.isPrimitive)
      return value.toString()

    if (value is File) {
      tmp = value.path
      return if (tmp.startsWith(projPath))
        "." + tmp.substring(projPath.length)
      else
        tmp
    }

    if (value is Path) {
      tmp = value.toString()
      return if (tmp.startsWith(projPath))
        "." + tmp.substring(projPath.length)
      else
        tmp
    }

    if (value.javaClass.isArray) {
      return arrayToString(value)
    }

    return value.toString()
  }

  /**
   * Generates tree-logging prefix padding for the current call stack depth.
   *
   * @return Tree-logging prefix padding;
   *
   * @since 1.1.0
   */
  private fun pad(): String {
    return if (call < 15) {
      padding[call]
    } else {
      bar.repeat(call - 15) + padding[14] + com
    }
  }

  /**
   * Returns the prefix text form of the given log level.
   *
   * @param level Level for which the prefix should be returned.
   *
   * @return Prefix for the given log level.
   *
   * @since 1.1.0
   */
  internal fun levelPrefix(level: Level): CharArray {
    return LevelPrefixes[level.value]
  }

  /**
   * Creates an execution time log prefix.
   *
   * @return A new execution time log prefix.
   *
   * @since 1.1.0
   */
  internal fun timePrefix(): CharArray {
    val line = CharArray(timeBuffer.size)
    System.arraycopy(timeBuffer, 0, line, 0, line.size)

    fillTime(line, System.currentTimeMillis() - start)

    return line
  }

  /**
   * Writes the given duration value {@code time} to the given char array.
   *
   * @param buf  Char array to write the duration value to.
   * @param time Duration in milliseconds.
   *
   * @since 1.1.0
   */
  internal fun fillTime(buf: CharArray, time: Long) {
    var pos = buf.size - 3
    var rem = time

    while (rem > 0) {
      buf[pos--] = (rem % 10 + 48).toInt().toChar()
      rem /= 10
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Writes the given value to the configured writer with a timestamp and log
   * level prefix.
   *
   * @param level Log level.
   * @param value Value to write.
   *
   * @since 1.1.0
   */
  internal fun write(level: Level, value: Any?) {
    write(writer, level, value)
  }

  internal fun write(writer: BufferedWriter, level: Level, value: Any?) {
    try {
      writePrefix(level)

      writer.write(value.toString())
      writer.newLine()
      writer.flush()
    } catch (e: IOException) {
      backupWrite(Level.Fatal, "Failed to write to stdout")
      throw RuntimeException("Failed to write to stdout", e)
    }
  }

  internal fun writeNoPrefix(writer: BufferedWriter, value: Any?) {
    try {
      writer.write(value.toString())
      writer.newLine()
      writer.flush()
    } catch (e: IOException) {
      backupWrite(Level.Fatal, "Failed to write to stdout.")
      throw RuntimeException("Failed to write to stdout.", e)
    }
  }

  /**
   * Injects the given value into the given format string and writes it to the
   * configured writer with a timestamp and log level prefix.
   *
   * @param level Log level.
   * @param fmt   Format string.
   * @param value   Value to inject into the give format string.
   *
   * @since 1.1.0
   */
  internal fun write(level: Level, fmt: String, value: Any?) =
    write(writer, level, fmt, value)

  internal fun write(
    writer: BufferedWriter,
    level: Level,
    fmt: String,
    value: Any?,
  ) {
    try {
      writePrefix(level)

      val pos = inject(0, 0, 1, fmt, value)

      if (pos == -1) {
        writer.write(fmt)
      } else {
        writer.write(fmt.substring(pos))
      }

      writer.newLine()
      writer.flush()
    } catch (e: IOException) {
      fatal(e, "Failed to write to stdout")
    }
  }

  /**
   * Injects the given values into the given format string and writes it to the
   * configured writer with a timestamp and log level prefix.
   *
   * @param level Log level.
   * @param fmt   Format string.
   * @param val1  First value to inject into the give format string.
   * @param val2  Second to inject into the give format string.
   *
   * @since 1.1.0
   */
  internal fun write(
    level: Level,
    fmt: String,
    val1: Any?,
    val2: Any?,
  ) {
    write(writer, level, fmt, val1, val2)
  }

  internal fun write(
    writer: BufferedWriter,
    level: Level,
    fmt: String,
    val1: Any?,
    val2: Any?,
  ) {
    val count = 2
    var ind = 0
    var pos = 0
    var prev = 0

    try {
      writePrefix(level)

      pos = inject(pos, ind++, count, fmt, val1)

      if (pos > -1) {
        prev = pos
        pos = inject(pos, ind, count, fmt, val2)
      }

      if (pos == -1) {
        writer.write(fmt.substring(prev))
      } else {
        writer.write(fmt.substring(pos))
      }

      writer.newLine()
      writer.flush()
    } catch (e: IOException) {
      backupWrite(Level.Error, "Failed to write to stdout")
      throw RuntimeException("Failed to write to stdout", e)
    }
  }

  /**
   * Injects the given values into the given format string and writes it to the
   * configured writer with a timestamp and log level prefix.
   *
   * @param level Log level.
   *
   * @param fmt Format string.
   *
   * @param val1 First value to inject into the give format string.
   *
   * @param val2 Second to inject into the give format string.
   *
   * @param val3 Third to inject into the give format string.
   *
   * @since 1.1.0
   */
  internal fun write(
    level: Level,
    fmt: String,
    val1: Any?,
    val2: Any?,
    val3: Any?,
  ) {
    val count = 3
    var ind = 0
    var pos = 0
    var prev = 0

    try {
      writePrefix(level)

      pos = inject(pos, ind++, count, fmt, val1)

      if (pos > -1) {
        prev = pos
        pos = inject(pos, ind++, count, fmt, val2)
      }

      if (pos > -1) {
        prev = pos
        pos = inject(pos, ind, count, fmt, val3)
      }

      if (pos == -1) {
        writer.write(fmt.substring(prev))
      } else {
        writer.write(fmt.substring(pos))
      }

      writer.newLine()
      writer.flush()
    } catch (e: IOException) {
      backupWrite(Level.Error, "Failed to write to stdout")
      throw RuntimeException("Failed to write to stdout", e)
    }
  }

  internal fun write(
    writer: BufferedWriter,
    level: Level,
    fmt: String,
    val1: Any?,
    val2: Any?,
    val3: Any?,
  ) {
    val count = 3
    var ind = 0
    var pos = 0
    var prev = 0

    try {
      writePrefix(level)

      pos = inject(pos, ind++, count, fmt, val1)

      if (pos > -1) {
        prev = pos
        pos = inject(pos, ind++, count, fmt, val2)
      }

      if (pos > -1) {
        prev = pos
        pos = inject(pos, ind, count, fmt, val3)
      }

      if (pos == -1) {
        writer.write(fmt.substring(prev))
      } else {
        writer.write(fmt.substring(pos))
      }

      writer.newLine()
      writer.flush()
    } catch (e: IOException) {
      backupWrite(Level.Error, "Failed to write to stdout")
      throw RuntimeException("Failed to write to stdout", e)
    }
  }

  /**
   * Injects the given values into the given format string and writes it to the
   * configured writer with a timestamp and log level prefix.
   *
   * @param level Log level.
   *
   * @param fmt Format string.
   *
   * @param val1 First value to inject into the give format string.
   *
   * @param val2 Second to inject into the give format string.
   *
   * @param val3 Third to inject into the give format string.
   *
   * @param val4 Fourth to inject into the give format string.
   *
   * @since 1.1.0
   */
  @SuppressWarnings("DuplicatedCode")
  internal fun write(
    level: Level,
    fmt: String,
    val1: Any?,
    val2: Any?,
    val3: Any?,
    val4: Any?,
  ) {
    val count = 4
    var ind = 0
    var pos = 0
    var prev = 0

    try {
      writePrefix(level)

      pos = inject(pos, ind++, count, fmt, val1)

      if (pos > -1) {
        prev = pos
        pos = inject(pos, ind++, count, fmt, val2)
      }

      if (pos > -1) {
        prev = pos
        pos = inject(pos, ind++, count, fmt, val3)
      }

      if (pos > -1) {
        prev = pos
        pos = inject(pos, ind, count, fmt, val4)
      }

      if (pos == -1) {
        writer.write(fmt.substring(prev))
      } else {
        writer.write(fmt.substring(pos))
      }

      writer.newLine()
      writer.flush()
    } catch (e: IOException) {
      backupWrite(Level.Error, "Failed to write to stdout")
      throw RuntimeException("Failed to write to stdout", e)
    }
  }

  /**
   * Injects the given values into the given format string and writes it to the
   * configured writer with a timestamp and log level prefix.
   *
   * @param level Log level.
   *
   * @param fmt Format string.
   *
   * @param val1 First value to inject into the give format string.
   *
   * @param val2 Second to inject into the give format string.
   *
   * @param val3 Third to inject into the give format string.
   *
   * @param val4 Fourth to inject into the give format string.
   *
   * @param val5 Fifth to inject into the give format string.
   *
   * @since 1.1.0
   */
  @SuppressWarnings("DuplicatedCode")
  internal fun write(
    level: Level,
    fmt: String,
    val1: Any?,
    val2: Any?,
    val3: Any?,
    val4: Any?,
    val5: Any?,
  ) {
    val count = 5
    var ind = 0
    var pos = 0
    var prev = 0

    try {
      writePrefix(level)
      backupWrite(Level.Error, fmt)

      pos = inject(pos, ind++, count, fmt, val1)

      if (pos > -1) {
        prev = pos
        pos = inject(pos, ind++, count, fmt, val2)
      }

      if (pos > -1) {
        prev = pos
        pos = inject(pos, ind++, count, fmt, val3)
      }

      if (pos > -1) {
        prev = pos
        pos = inject(pos, ind++, count, fmt, val4)
      }

      if (pos > -1) {
        prev = pos
        pos = inject(pos, ind, count, fmt, val5)
      }

      if (pos == -1) {
        writer.write(fmt.substring(prev))
      } else {
        writer.write(fmt.substring(pos))
      }

      writer.newLine()
      writer.flush()
    } catch (e: IOException) {
      backupWrite(Level.Error, "Failed to write to stdout")
      throw RuntimeException("Failed to write to stdout", e)
    }
  }

  /**
   * Finds an injection point and injects the given value ([value]) into the
   * given format string ([fmt]) and writes the output to the configured
   * writer.
   *
   * If no injection point was found, the remainder of the string, from
   * [start] to `fmt.length` will be written to the configured writer.
   *
   * Injection point scanning starts at character [start] in the format string.
   *
   * @param start Starting place for injection point scanning.
   *
   * @param index Current injection point index (starting from `0`).
   *
   * @param count Total number of parameters that are to be injected into the
   * format string if at least that many injection points exist in the format
   * string.
   *
   * @param fmt Format string.
   *
   * @param value Value to inject into the format string.
   *
   * @return The current position in the format string.  This value is used as
   * the [start] value for the next injection.
   *
   * @since 1.1.0
   */
  internal fun inject(
    start: Int,
    index: Int,
    count: Int,
    fmt: String,
    value: Any?,
  ) = inject(writer, start, index, count, fmt, value)

  /**
   * Finds an injection point and injects the given value ([value]) into the
   * given format string [fmt] and writes the output to the given writer.
   *
   * If no injection point was found, the remainder of the string, from
   * [start] to `fmt.length` will be written to the given
   * writer.
   *
   * Injection point scanning starts at character [start] in the format string.
   *
   * @param writer Writer that the result will be written to.
   *
   * @param start Starting place for injection point scanning.
   *
   * @param index Current injection point index (starting from `0`).
   *
   * @param count Total number of parameters that are to be injected into the
   * format string if at least that many injection points exist in the format
   * string.
   *
   * @param fmt Format string.
   *
   * @param value Value to inject into the format string.
   *
   * @return The current position in the format string.  This value is used as
   * the [start] value for the next injection.
   */
  internal fun inject(
    writer: BufferedWriter,
    start: Int,
    index: Int,
    count: Int,
    fmt: String,
    value: Any?,
  ): Int {
    val pos = fmt.indexOf(Inject, start)

    if (pos < 0) {
      backupWrite(
        Level.Warn,
        "Logger: $count parameters provided but there are " + (
          if (index == 0)
            "no placeholders in format string."
          else
            "only " + index + 1 + " placeholders in format string"
          )
      )
      return -1
    }

    writer.write(fmt.substring(start, pos))
    writer.write(stringify(value))

    return pos + ISize
  }

  /**
   * Generates a string representation of the given array.
   *
   * @param arr Array to stringify.
   *
   * @return The stringified array.
   *
   * @since 1.1.0
   */
  internal fun arrayToString(arr: Any): String {
    val len = Array.getLength(arr)

    if (len == 0) {
      return "[]"
    }

    val out = StringBuilder(10 * len)

    out.append('[')
    out.append(Array.get(arr, 0))
    for (i in 1 until len) {
      out.append(", ").append(Array.get(arr, i))
    }
    out.append(']')

    return out.toString()
  }

  /**
   * Writes log message prefix(es) to the configured writer.
   *
   * @param level Level used to print the log level prefix.
   *
   * @throws IOException Thrown if writing to the configured writer fails.
   *
   * @since 1.1.0
   */
  internal fun writePrefix(level: Level) = writePrefix(writer, level)

  internal fun writePrefix(writer: Writer, level: Level) {
    writer.write(timePrefix())

    if (isTTY) {
      writer.write(ColorPrefixes[level.value])
      writer.write(levelPrefix(level))
      writer.write(ColorReset)
    } else {
      writer.write(levelPrefix(level))
    }

    if (logLevel >= Level.Trace)
      writer.write(pad())
  }

  /**
   * Writes messages out using `print*`, rather than the default
   * writer.
   *
   * Intended to be used as a fallback when unable to safely use one of the
   * `write(...)` methods.
   *
   * @param level Log level.  If the currently configured log level is less than
   * this value, this method is a no-op.
   *
   * @param value Value to write out.
   *
   * @since 1.1.0
   */
  internal fun backupWrite(level: Level, value: Any?) {
    print(timePrefix())
    print(levelPrefix(level))

    if (level >= Level.Trace)
      print(pad())

    println(value)
  }
}
