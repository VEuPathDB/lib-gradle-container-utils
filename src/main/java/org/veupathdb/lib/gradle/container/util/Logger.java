package org.veupathdb.lib.gradle.container.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Plugin Tree Logger.
 * <p>
 * Provides methods for logging messages at various levels of severity to the
 * console.
 *
 * @since 1.1.0
 */
// TODO: allow configuring threshold for what goes to stdout and what goes to stderr.
public class Logger {
  /**
   * Disable logging of all message severities less than Fatal.
   *
   * @since 1.1.0
   */
  public static final byte LogLevelNone = 0;

  /**
   * Disable logging of all message severities less than Error.
   *
   * @since 1.1.0
   */
  public static final byte LogLevelError = 1;

  /**
   * Disable logging of all message severities less than Warn.
   *
   * @since 1.1.0
   */
  public static final byte LogLevelWarn = 2;

  /**
   * Disable logging of all message severities less than Info.
   *
   * @since 1.1.0
   */
  public static final byte LogLevelInfo = 3;

  /**
   * Disable logging of all message severities less than Debug.
   *
   * @since 1.1.0
   */
  public static final byte LogLevelDebug = 4;

  /**
   * Enable logging of all messages, and tree logging.
   *
   * @since 1.1.0
   */
  public static final byte LogLevelTrace = 5;

  public static final byte LogLevelFatal = 0;

  /**
   * Pattern for logging no-arg method call starts.
   *
   * @since 1.1.0
   */
  private static final String OpenNoArg = "{}#{}()";

  /**
   * Pattern for logging single argument method call starts.
   *
   * @since 1.1.0
   */
  private static final String OpenOneArg = "{}#{}({})";

  /**
   * Pattern for logging double argument method call starts.
   *
   * @since 1.1.0
   */
  private static final String OpenTwoArg = "{}#{}({}, {})";

  /**
   * Pattern for logging triple argument method call starts.
   *
   * @since 1.1.0
   */
  private static final String OpenThreeArg = "{}#{}({}, {}, {})";

  /**
   * Pattern for logging single argument constructors.
   *
   * @since 1.1.0
   */
  private static final String ConstructorOneArg = "{}#new({})";

  /**
   * Pattern for logging double argument constructors.
   *
   * @since 1.1.0
   */
  private static final String ConstructorTwoArg = "{}#new({}, {})";

  /**
   * Pattern for logging zero argument no-op methods.
   *
   * @since 1.1.0
   */
  private static final String NoOpNoArg = "{}#{}(): no-op";

  /**
   * Pattern for logging single argument no-op methods.
   *
   * @since 1.1.0
   */
  private static final String NoOpOneArg = "{}#{}({}): no-op";

  /**
   * Pattern for logging mapping methods.
   *
   * @since 1.1.0
   */
  private static final String MapPattern = "{}#{}({}): {}";

  private static final boolean isTTY = System.getenv("DOCKER") == null;

  /**
   * Currently configured logging level.
   *
   * @since 1.1.0
   */
  private final byte level;

  /**
   * Project path.
   * <p>
   * Used for trimming file paths down to relative paths for log readability.
   *
   * @since 1.1.0
   */
  private final String projPath;

  /**
   * Standard logging writer.
   * <p>
   * Messages below the error threshold will be appended to this writer.
   *
   * @since 1.1.0
   */
  private final BufferedWriter writer;

  /**
   * Timestamp for when this logger instance was created.
   * <p>
   * Used for tracking task execution times in milliseconds.
   *
   * @since 1.1.0
   */
  private final long start;

  /**
   * Call stack size counter.
   * <p>
   * Used to track call stack depth for tree logging.
   *
   * @since 1.1.0
   */
  private int call;

  public Logger(final byte level, final File rootDir) {
    this.start    = System.currentTimeMillis();
    this.level    = level;
    this.projPath = rootDir.getPath();
    this.writer   = new BufferedWriter(new PrintWriter(System.out));

    constructor(level, rootDir);
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  public byte getLogLevel() {
    return level;
  }

  public boolean isTrace() {
    return level >= LogLevelTrace;
  }

  public boolean debugEnabled() {
    return level >= LogLevelDebug;
  }

  public boolean isInfo() {
    return level >= LogLevelInfo;
  }

  public boolean isWarn() {
    return level >= LogLevelWarn;
  }

  public boolean isError() {
    return level >= LogLevelError;
  }

  public boolean isDisabled() {
    return level == LogLevelNone;
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Increments the call stack count and, if trace logging is enabled, prints
   * the class and method name of the caller.
   *
   * @since 1.1.0
   */
  public void open() {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(
        LogLevelTrace,
        OpenNoArg,
        stack.getClassName().substring(stack.getClassName().lastIndexOf('.') + 1),
        stack.getMethodName()
      );
    }
    call++;
  }

  /**
   * Increments the call stack count and, if trace logging is enabled, prints
   * the class name, method name, and first argument of the caller.
   *
   * @param arg1 First argument of the calling method.
   *
   * @since 1.1.0
   */
  public void open(@Nullable final Object arg1) {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(
        LogLevelTrace,
        OpenOneArg,
        stack.getClassName().substring(stack.getClassName().lastIndexOf('.') + 1),
        stack.getMethodName(),
        arg1
      );
    }
    call++;
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
  public void open(@Nullable final Object arg1, @Nullable final Object arg2) {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(
        LogLevelTrace,
        OpenTwoArg,
        stack.getClassName().substring(stack.getClassName().lastIndexOf('.') + 1),
        stack.getMethodName(),
        arg1,
        arg2
      );
    }
    call++;
  }

  /**
   * Increments the call stack count and, if trace logging is enabled, prints
   * the class name, method name, and arguments of the caller.
   *
   * @param arg1 First argument of the calling method.
   * @param arg2 Second argument of the calling method.
   * @param arg3 Third argument of the calling method.
   *
   * @since 1.1.0
   */
  public void open(
    @Nullable final Object arg1,
    @Nullable final Object arg2,
    @Nullable final Object arg3
  ) {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(
        LogLevelTrace,
        OpenThreeArg,
        stack.getClassName().substring(stack.getClassName().lastIndexOf('.') + 1),
        stack.getMethodName(),
        arg1,
        arg2,
        arg3
      );
    }
    call++;
  }

  /**
   * If {@link #LogLevelTrace} is enabled, prints a void return then decrements
   * the tree log level.
   *
   * @since 1.1.0
   */
  public void close() {
    log(LogLevelTrace, "return: void");
    call--;
  }

  /**
   * If {@link #LogLevelTrace} is enabled, prints the given return value then
   * decrements the tree log level.
   *
   * @param val Passthrough value.
   * @param <T> Type of the passthrough value.
   *
   * @return The given passthrough value.
   *
   * @since 1.1.0
   */
  @Contract("null -> null")
  public <T> T close(final T val) {
    log(LogLevelTrace, "return: {}", val);
    call--;
    return val;
  }

  /**
   * If {@link #LogLevelTrace} is enabled, prints a getter method signature
   * including the given return value, returning the passed value.
   * <p>
   * Example:
   * <pre>{@code
   * class Bar {
   *   public String getFoo() {
   *     return log.getter("Hello world");
   *   }
   * }
   *
   * ////////
   *
   * new Bar().getFoo(); // Prints: Bar#getFoo(): Hello world
   * }</pre>
   * <p>
   * This method prints the log at the current tree log level and does not
   * increment it.
   *
   * @param val Passthrough value that will be returned.
   * @param <T> Type of the passthrough value that will be returned.
   *
   * @return The given passthrough value.
   *
   * @since 1.1.0
   */
  @Contract("null -> null")
  public <T> T getter(final T val) {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(
        LogLevelTrace,
        "{}#{}(): {}",
        stack.getClassName().substring(stack.getClassName().lastIndexOf('.') + 1),
        stack.getMethodName(),
        val
      );
    }

    return val;
  }

  /**
   * If {@link #LogLevelTrace} is enabled, prints a constructor method
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
  public void constructor(@Nullable final Object arg1) {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(
        LogLevelTrace,
        ConstructorOneArg,
        stack.getClassName().substring(stack.getClassName().lastIndexOf('.') + 1),
        arg1
      );
    }
  }

  /**
   * If {@link #LogLevelTrace} is enabled, prints a constructor method
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
  public void constructor(@Nullable final Object arg1, @Nullable final Object arg2) {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(
        LogLevelTrace,
        ConstructorTwoArg,
        stack.getClassName().substring(stack.getClassName().lastIndexOf('.') + 1),
        arg1,
        arg2
      );
    }
  }

  /**
   * If {@link #LogLevelTrace} is enabled, prints the caller's function
   * signature.
   * <p>
   * Example:
   * <pre>{@code
   * class Bar {
   *   public void foo() {
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
  public void noop() {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(
        LogLevelTrace,
        NoOpNoArg,
        stack.getClassName().substring(stack.getClassName().lastIndexOf('.') + 1),
        stack.getMethodName()
      );
    }
  }

  /**
   * If {@link #LogLevelTrace} is enabled, prints the caller's function
   * signature including the given argument.
   * <p>
   * Example:
   * <pre>{@code
   * class Bar {
   *   public void foo(String hello) {
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
  public void noop(@Nullable final Object arg1) {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(
        LogLevelTrace,
        NoOpOneArg,
        stack.getClassName().substring(stack.getClassName().lastIndexOf('.') + 1),
        stack.getMethodName(),
        arg1
      );
    }
  }

  /**
   * If {@link #LogLevelTrace} is enabled, prints the caller's function
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
   * @param in  The input argument to the calling function.
   * @param out The return value from the calling function.
   * @param <T> The type of the return value from the calling function.
   *
   * @return Returns the argument {@code out};
   *
   * @since 1.1.0
   */
  @Contract("_, null -> null")
  public <T> T map(@Nullable final Object in, @Nullable final T out) {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(
        LogLevelTrace,
        MapPattern,
        stack.getClassName().substring(stack.getClassName().lastIndexOf('.') + 1),
        stack.getMethodName(),
        in,
        out
      );
    }

    return out;
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * If {@link #LogLevelError} is enabled, prints the given value to stdout.
   *
   * @param val Value to print.
   *
   * @since 1.1.0
   */
  public void error(@Nullable final Object val) {
    log(LogLevelError, val);
  }

  /**
   * If {@link #LogLevelError} is enabled, expands the template string using the
   * return value of the given {@code Supplier} and prints the result to stdout.
   *
   * @param fmt Template string.
   * @param fn  Method that will be called to retrieve the value to inject
   *            into the template string.  This method will only be called if
   *            {@link #LogLevelError} is enabled.
   *
   * @since 1.1.0
   */
  public void error(@NotNull final String fmt, @NotNull final Supplier<?> fn) {
    log(LogLevelError, fmt, fn);
  }

  /**
   * If {@link #LogLevelError} is enabled, expands the template string using the
   * given argument and prints the result to stdout.
   *
   * @param fmt  Template string.
   * @param val1 Value to inject into the template string.
   *
   * @since 1.1.0
   */
  public void error(@NotNull final String fmt, @Nullable final Object val1) {
    log(LogLevelError, fmt, val1);
  }

  /**
   * If {@link #LogLevelError} is enabled, expands the template string using the
   * given arguments and prints the result to stdout.
   *
   * @param fmt  Template string.
   * @param val1 First value to inject into the template string.
   * @param val2 Second value to inject into the template string.
   *
   * @since 1.1.0
   */
  public void error(
    @NotNull final String fmt,
    @Nullable final Object val1,
    @Nullable final Object val2
  ) {
    log(LogLevelError, fmt, val1, val2);
  }

  /**
   * If {@link #LogLevelError} is enabled, expands the template string using the
   * given arguments and prints the result to stdout.
   *
   * @param fmt  Template string.
   * @param val1 First value to inject into the template string.
   * @param val2 Second value to inject into the template string.
   * @param val3 Third value to inject into the template string.
   *
   * @since 1.1.0
   */
  public void error(
    @NotNull final String fmt,
    @Nullable final Object val1,
    @Nullable final Object val2,
    @Nullable final Object val3
  ) {
    log(LogLevelError, fmt, val1, val2, val3);
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * If {@link #LogLevelInfo} is enabled, prints the given value to stdout.
   *
   * @param val Value to print.
   *
   * @since 1.1.0
   */
  public void info(@Nullable final Object val) {
    log(LogLevelInfo, val);
  }

  /**
   * If {@link #LogLevelInfo} is enabled, prints the output of the given
   * {@code Supplier} to stdout.
   *
   * @param messageSupplier Method that will be called to retrieve the value
   *                        to print.  This method will only be called if
   *                        {@link #LogLevelInfo}  is enabled.
   *
   * @throws NullPointerException if {@code messageSupplier} is {@code null}
   * @since 1.1.0
   */
  public void info(@NotNull final Supplier<?> messageSupplier) {
    log(LogLevelInfo, messageSupplier);
  }

  /**
   * If {@link #LogLevelInfo} is enabled, expands the template string using the
   * return value of the given {@code Supplier} and prints the result to stdout.
   *
   * @param fmt Template string.
   * @param fn1 Method that will be called to retrieve the value to inject
   *            into the template string.  This method will only be called if
   *            {@link #LogLevelInfo} is enabled.
   *
   * @since 1.1.0
   */
  public void info(@NotNull final String fmt, @NotNull Supplier<?> fn1) {
    log(LogLevelInfo, fmt, fn1);
  }

  /**
   * If {@link #LogLevelInfo} is enabled, expands the template string using the
   * given argument and prints the result to stdout.
   *
   * @param fmt  Template string.
   * @param val1 Value to inject into the template string.
   *
   * @since 1.1.0
   */
  public void info(@NotNull final String fmt, @Nullable Object val1) {
    log(LogLevelInfo, fmt, val1);
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * If {@link #LogLevelDebug} is enabled, prints the given value to stdout.
   *
   * @param val Value to print.
   *
   * @since 1.1.0
   */
  public void debug(@Nullable final Object val) {
    log(LogLevelDebug, val);
  }

  /**
   * If {@link #LogLevelDebug} is enabled, expands the template string using the
   * given argument and prints the result to stdout.
   *
   * @param fmt  Template string.
   * @param val1 Value to inject into the template string.
   *
   * @since 1.1.0
   */
  public void debug(@NotNull final String fmt, @Nullable Object val1) {
    log(LogLevelDebug, fmt, val1);
  }

  /**
   * If {@link #LogLevelDebug} is enabled, expands the template string using the
   * return value of the given {@code Supplier}s and prints the result to
   * stdout.
   *
   * @param fmt Template string.
   * @param fn1 Method that will be called to retrieve the first value to inject
   *            into the template string.  This method will only be called if
   *            {@link #LogLevelDebug} is enabled.
   * @param fn2 Method that will be called to retrieve the second value to
   *            inject into the template string.  This method will only be
   *            called if {@link #LogLevelDebug} is enabled.
   *
   * @since 1.1.0
   */
  public void debug(@NotNull final String fmt, @NotNull Supplier<?> fn1, @NotNull Supplier<?> fn2) {
    log(LogLevelDebug, fmt, fn1, fn2);
  }

  /**
   * If {@link #LogLevelDebug} is enabled, expands the template string using the
   * given arguments and prints the result to stdout.
   *
   * @param fmt  Template string.
   * @param val1 First value to inject into the template string.
   * @param val2 Second value to inject into the template string.
   *
   * @since 1.1.0
   */
  public void debug(@NotNull final String fmt, @Nullable Object val1, @Nullable Object val2) {
    log(LogLevelDebug, fmt, val1, val2);
  }

  //
  //
  // Fatal Level Logging
  //
  //

  //
  // Without an exception
  //

  @NotNull
  @Contract(value = "_ -> fail", pure = true)
  public <T> T fatal(@Nullable final Object val) {
    final var buf = new StringWriter(64);

    write(new BufferedWriter(buf), LogLevelFatal, val);
    write(writer, buf.toString());

    throw new RuntimeException(buf.toString());
  }

  /**
   * Formats and logs the given message and argument, then throws a runtime
   * exception with that log text.
   *
   * @param fmt Log format string.
   * @param val Argument to inject into the log format string.
   * @param <T> Type of the "returned" value to enable using this method
   *            execution as a return value.
   *
   * @return Nothing.  This method will always throw.
   *
   * @throws RuntimeException Thrown after the log is written.
   * @since 2.0.0
   */
  @NotNull
  @Contract(value = "_, _ -> fail", pure = true)
  public <T> T fatal(@NotNull final String fmt, @Nullable final Object val) {
    final var buf = new StringWriter(128);

    write(new BufferedWriter(buf), LogLevelFatal, fmt, val);
    write(writer, buf.toString());

    throw new RuntimeException(buf.toString());
  }

  //
  // With an exception
  //

  public <T> T fatal(@NotNull final Throwable val) {
    write(writer, LogLevelFatal, val.getMessage());

    throw new RuntimeException(val);
  }

  public void fatal(@NotNull final Throwable err, @Nullable final Object val) {
    final var buf = new StringWriter(64);

    write(new BufferedWriter(buf), LogLevelFatal, val);
    write(writer, buf.toString());

    throw new RuntimeException(buf.toString(), err);
  }

  public <T> T fatal(@NotNull final Throwable err, @NotNull final String fmt, @Nullable final Object val) {
    final var buf = new StringWriter(64);

    write(new BufferedWriter(buf), LogLevelFatal, fmt, val);
    write(writer, buf.toString());

    throw new RuntimeException(buf.toString(), err);
  }

  public void fatal(
    @NotNull final Throwable err,
    @NotNull final String fmt,
    @Nullable final Object val1,
    @Nullable final Object val2
  ) {
    final var buf = new StringWriter(64);

    write(new BufferedWriter(buf), LogLevelFatal, fmt, val1, val2);
    write(writer, buf.toString());

    throw new RuntimeException(buf.toString(), err);
  }

  public void fatal(
    @NotNull final Throwable err,
    @NotNull final String fmt,
    @Nullable final Object val1,
    @Nullable final Object val2,
    @Nullable final Object val3
  ) {
    final var buf = new StringWriter(64);

    write(new BufferedWriter(buf), LogLevelFatal, fmt, val1, val2);
    write(writer, buf.toString());

    throw new RuntimeException(buf.toString(), err);
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
  public void log(final byte level, @NotNull final Supplier<?> fn) {
    if (this.level >= level) {
      write(level, fn.get());
    }
  }

  /**
   * If the current configured log level is greater than or equal to the given
   * {@code level}, prints the given value to stdout.
   *
   * @param val Value to print.
   *
   * @since 1.1.0
   */
  public void log(final byte level, @Nullable final Object val) {
    if (this.level >= level) {
      write(level, val);
    }
  }

  /**
   * If the current configured log level is greater than or equal to the given
   * {@code level}, injects the output of the given {@code Supplier} into the
   * given format string and logs it.
   *
   * @param level Level to log the given message at.
   * @param fmt   Format string.
   * @param fn1   {@code Supplier} for a value to inject into the format string.
   *              <p>
   *              This function will not be called if logging at the specified
   *              level is not enabled.
   *
   * @since 1.1.0
   */
  public void log(final byte level, @NotNull final String fmt, @NotNull final Supplier<?> fn1) {
    if (this.level >= level) {
      write(level, fmt, fn1.get());
    }
  }

  /**
   * If the current configured log level is greater than or equal to the given
   * {@code level}, injects the output of the given {@code Supplier}s into the
   * given format string and logs it.
   *
   * @param level Level to log the given message at.
   * @param fmt   Format string.
   * @param fn1   {@code Supplier} for the first value to inject into the format
   *              string.
   *              <p>
   *              This function will not be called if logging at the specified
   *              level is not enabled.
   * @param fn2   {@code Supplier} for the second value to inject into the
   *              format string.
   *              <p>
   *              This function will not be called if logging at the specified
   *              level is not enabled.
   *
   * @since 1.1.0
   */
  public void log(
    final byte level,
    @NotNull final String fmt,
    @NotNull final Supplier<?> fn1,
    @NotNull final Supplier<?> fn2
  ) {
    if (this.level >= level) {
      write(level, fmt, fn1.get(), fn2.get());
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
  public void log(final byte level, @NotNull final String fmt, @Nullable final Object val1) {
    if (this.level >= level) {
      write(level, fmt, val1);
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
  public void log(
    final byte level,
    @NotNull final String fmt,
    @Nullable final Object val1,
    @Nullable final Object val2
  ) {
    if (this.level >= level) {
      write(level, fmt, val1, val2);
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
  public void log(
    final byte level,
    @NotNull final String fmt,
    @Nullable final Object val1,
    @Nullable final Object val2,
    @Nullable final Object val3
  ) {
    if (this.level >= level) {
      write(level, fmt, val1, val2, val3);
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
  public void log(
    final byte level,
    @NotNull final String fmt,
    @Nullable final Object val1,
    @Nullable final Object val2,
    @Nullable final Object val3,
    @Nullable final Object val4
  ) {
    if (this.level >= level) {
      write(level, fmt, val1, val2, val3, val4);
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
  public void log(
    final byte level,
    @NotNull final String fmt,
    @Nullable final Object val1,
    @Nullable final Object val2,
    @Nullable final Object val3,
    @Nullable final Object val4,
    @Nullable final Object val5
  ) {
    if (this.level >= level) {
      write(level, fmt, val1, val2, val3, val4, val5);
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public String toString() {
    return "Logger{" +
      "level=" + level +
      '}';
  }

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
   * @param val Value to stringify.
   *
   * @return Stringified value.
   *
   * @since 1.1.0
   */
  @NotNull
  String stringify(@Nullable final Object val) {
    final String tmp;

    if (val == null)
      return "null";

    if (val instanceof String)
      return (String) val;

    final var cls = val.getClass();

    if (cls.isPrimitive())
      return String.valueOf(val);

    if (val instanceof File)
      if ((tmp = ((File) val).getPath()).startsWith(projPath))
        return "." + tmp.substring(projPath.length());
      else
        return tmp;

    if (val instanceof Path)
      if ((tmp = ((Path) val).toString()).startsWith(projPath))
        return "." + tmp.substring(projPath.length());
      else
        return tmp;

    if (val.getClass().isArray()) {
      return arrayToString(val);
    }

    return val.toString();
  }

  /**
   * Generates tree-logging prefix padding for the current call stack depth.
   *
   * @return Tree-logging prefix padding;
   *
   * @since 1.1.0
   */
  @NotNull
  private String pad() {
    if (call < 15) {
      return padding[call];
    } else {
      return bar.repeat(call - 15) + padding[14] + com;
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
  char[] levelPrefix(final byte level) {
    return LevelPrefixes[level];
  }

  /**
   * Static char array representing the timestamp prefix.
   * <p>
   * This value will be copied and modified as necessary to represent the number
   * of milliseconds that have passed since the logger was created.
   * <p>
   * Max supported execution time of 9,999,999ms or 4.444444 hours.
   *
   * @since 1.1.0
   */
  private static final char[] timeBuffer = {'[', '0', '0', '0', '0', '0', '0', '0', ']', ' '};

  /**
   * Creates an execution time log prefix.
   *
   * @return A new execution time log prefix.
   *
   * @since 1.1.0
   */
  char[] timePrefix() {
    final var line = new char[timeBuffer.length];
    System.arraycopy(timeBuffer, 0, line, 0, line.length);

    fillTime(line, System.currentTimeMillis() - start);

    return line;
  }

  /**
   * Writes the given duration value {@code time} to the given char array.
   *
   * @param buf  Char array to write the duration value to.
   * @param time Duration in milliseconds.
   *
   * @since 1.1.0
   */
  @Contract(mutates = "param1")
  void fillTime(final char[] buf, final long time) {
    var pos = buf.length - 3;
    var rem = time;

    while (rem > 0) {
      buf[pos--] = (char) (rem % 10 + '0');
      rem /= 10;
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Writes the given value to the configured writer with a timestamp and log
   * level prefix.
   *
   * @param level Log level.
   * @param val   Value to write.
   *
   * @since 1.1.0
   */
  void write(
    @Range(from = LogLevelNone, to = LogLevelTrace) final byte level,
    @Nullable final Object val
  ) {
    write(writer, level, val);
  }

  void write(@NotNull final BufferedWriter writer, final byte level, @Nullable final Object val) {
    try {
      writePrefix(level);

      writer.write(String.valueOf(val));
      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      backupWrite(LogLevelFatal, "Failed to write to stdout");
      throw new RuntimeException("Failed to write to stdout", e);
    }
  }

  void write(@NotNull final BufferedWriter writer, @Nullable final Object val) {
    try {
      writer.write(String.valueOf(val));
      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      backupWrite(LogLevelFatal, "Failed to write to stdout.");
      throw new RuntimeException("Failed to write to stdout.", e);
    }
  }

  /**
   * Injects the given value into the given format string and writes it to the
   * configured writer with a timestamp and log level prefix.
   *
   * @param level Log level.
   * @param fmt   Format string.
   * @param val   Value to inject into the give format string.
   *
   * @since 1.1.0
   */
  void write(
    @Range(from = LogLevelNone, to = LogLevelTrace) final byte level,
    @NotNull final String fmt,
    @Nullable final Object val
  ) {
    write(writer, level, fmt, val);
  }

  void write(
    @NotNull final BufferedWriter writer,
    @Range(from = 0, to = 5) final byte level,
    @NotNull final String fmt,
    @Nullable final Object val
  ) {
    try {
      writePrefix(level);

      final var pos = inject(0, 0, 1, fmt, val);

      if (pos == -1) {
        writer.write(fmt);
      } else {
        writer.write(fmt.substring(pos));
      }

      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      fatal(e, "Failed to write to stdout");
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
  void write(
    final byte level,
    @NotNull final String fmt,
    @Nullable final Object val1,
    @Nullable final Object val2
  ) {
    write(writer, level, fmt, val1, val2);
  }

  @SuppressWarnings("DuplicatedCode")
  void write(
    @NotNull final BufferedWriter writer,
    final byte level,
    @NotNull final String fmt,
    @Nullable final Object val1,
    @Nullable final Object val2
  ) {
    final var count = 2;
    var       ind   = 0;
    var       pos   = 0;
    var       prev  = 0;

    try {
      writePrefix(level);

      pos = inject(pos, ind++, count, fmt, val1);

      if (pos > -1) {
        prev = pos;
        pos  = inject(pos, ind, count, fmt, val2);
      }

      if (pos == -1) {
        writer.write(fmt.substring(prev));
      } else {
        writer.write(fmt.substring(pos));
      }

      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      backupWrite(LogLevelError, "Failed to write to stdout");
      throw new RuntimeException("Failed to write to stdout", e);
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
   * @param val3  Third to inject into the give format string.
   *
   * @since 1.1.0
   */
  @SuppressWarnings("DuplicatedCode")
  void write(
    final byte level,
    @NotNull final String fmt,
    @Nullable final Object val1,
    @Nullable final Object val2,
    @Nullable final Object val3
  ) {
    final var count = 3;
    var       ind   = 0;
    var       pos   = 0;
    var       prev  = 0;

    try {
      writePrefix(level);

      pos = inject(pos, ind++, count, fmt, val1);

      if (pos > -1) {
        prev = pos;
        pos  = inject(pos, ind++, count, fmt, val2);
      }

      if (pos > -1) {
        prev = pos;
        pos  = inject(pos, ind, count, fmt, val3);
      }

      if (pos == -1) {
        writer.write(fmt.substring(prev));
      } else {
        writer.write(fmt.substring(pos));
      }

      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      backupWrite(LogLevelError, "Failed to write to stdout");
      throw new RuntimeException("Failed to write to stdout", e);
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
   * @param val3  Third to inject into the give format string.
   * @param val4  Fourth to inject into the give format string.
   *
   * @since 1.1.0
   */
  @SuppressWarnings("DuplicatedCode")
  void write(
    final byte level,
    @NotNull final String fmt,
    @Nullable final Object val1,
    @Nullable final Object val2,
    @Nullable final Object val3,
    @Nullable final Object val4
  ) {
    final var count = 4;
    var       ind   = 0;
    var       pos   = 0;
    var       prev  = 0;

    try {
      writePrefix(level);

      pos = inject(pos, ind++, count, fmt, val1);

      if (pos > -1) {
        prev = pos;
        pos  = inject(pos, ind++, count, fmt, val2);
      }

      if (pos > -1) {
        prev = pos;
        pos  = inject(pos, ind++, count, fmt, val3);
      }

      if (pos > -1) {
        prev = pos;
        pos  = inject(pos, ind, count, fmt, val4);
      }

      if (pos == -1) {
        writer.write(fmt.substring(prev));
      } else {
        writer.write(fmt.substring(pos));
      }

      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      backupWrite(LogLevelError, "Failed to write to stdout");
      throw new RuntimeException("Failed to write to stdout", e);
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
   * @param val3  Third to inject into the give format string.
   * @param val4  Fourth to inject into the give format string.
   * @param val5  Fifth to inject into the give format string.
   *
   * @since 1.1.0
   */
  @SuppressWarnings("DuplicatedCode")
  void write(
    final byte level,
    @NotNull final String fmt,
    @Nullable final Object val1,
    @Nullable final Object val2,
    @Nullable final Object val3,
    @Nullable final Object val4,
    @Nullable final Object val5
  ) {
    final var count = 5;
    var       ind   = 0;
    var       pos   = 0;
    var       prev  = 0;

    try {
      writePrefix(level);
      backupWrite(LogLevelError, fmt);

      pos = inject(pos, ind++, count, fmt, val1);

      if (pos > -1) {
        prev = pos;
        pos  = inject(pos, ind++, count, fmt, val2);
      }

      if (pos > -1) {
        prev = pos;
        pos  = inject(pos, ind++, count, fmt, val3);
      }

      if (pos > -1) {
        prev = pos;
        pos  = inject(pos, ind++, count, fmt, val4);
      }

      if (pos > -1) {
        prev = pos;
        pos  = inject(pos, ind, count, fmt, val5);
      }

      if (pos == -1) {
        writer.write(fmt.substring(prev));
      } else {
        writer.write(fmt.substring(pos));
      }

      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      backupWrite(LogLevelError, "Failed to write to stdout");
      throw new RuntimeException("Failed to write to stdout", e);
    }
  }

  /**
   * Finds an injection point and injects the given value ({@code val}) into the
   * given format string ({@code fmt}) and writes the output to the configured
   * writer.
   * <p>
   * If no injection point was found, the remainder of the string, from
   * {@code start} to {@code fmt.length()} will be written to the configured
   * writer.
   * <p>
   * Injection point scanning starts at character {@code start} in the format
   * string.
   *
   * @param start  Starting place for injection point scanning.
   * @param index  Current injection point index (starting from {@code 0)).
   * @param count  Total number of parameters that are to be injected into the
   *               format string (if at least that many injection points exist
   *               in the format string.
   * @param fmt    Format string.
   * @param val    Value to inject into the format string.
   *
   * @return The current position in the format string.  This value is used as
   * the {@code start} value for the next injection.
   *
   * @throws IOException If an error occurs while attempting to write to the
   *                     configured writer.
   *
   * @since 1.1.0
   */
  int inject(
    final int start,
    final int index,
    final int count,
    @NotNull final String fmt,
    @Nullable final Object val
  ) throws IOException {
    return inject(writer, start, index, count, fmt, val);
  }

  /**
   * Finds an injection point and injects the given value ({@code val}) into the
   * given format string ({@code fmt}) and writes the output to the given
   * writer.
   * <p>
   * If no injection point was found, the remainder of the string, from
   * {@code start} to {@code fmt.length()} will be written to the given
   * writer.
   * <p>
   * Injection point scanning starts at character {@code start} in the format
   * string.
   *
   * @param writer Writer that the result will be written to.
   * @param start  Starting place for injection point scanning.
   * @param index  Current injection point index (starting from {@code 0)).
   * @param count  Total number of parameters that are to be injected into the
   *               format string (if at least that many injection points exist
   *               in the format string.
   * @param fmt    Format string.
   * @param val    Value to inject into the format string.
   *
   * @return The current position in the format string.  This value is used as
   * the {@code start} value for the next injection.
   *
   * @throws IOException If an error occurs while attempting to write to the
   *                     given writer.
   */
  int inject(
    @NotNull final BufferedWriter writer,
    final int start,
    final int index,
    final int count,
    @NotNull final String fmt,
    @Nullable final Object val
  ) throws IOException {
    final var pos = fmt.indexOf(Inject, start);

    if (pos < 0) {
      backupWrite(LogLevelWarn, "Logger: " + count + " parameters provided but there are " + (
        index == 0
          ? "no placeholders in format string."
          : "only " + index + 1 + " placeholders in format string"
      ));
      return -1;
    }

    writer.write(fmt.substring(start, pos));
    writer.write(stringify(val));

    return pos + ISize;
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
  String arrayToString(@NotNull final Object arr) {
    final var len = Array.getLength(arr);

    if (len == 0) {
      return "[]";
    }

    final var out = new StringBuilder(10 * len);

    out.append('[');
    out.append(Array.get(arr, 0));
    for (var i = 1; i < len; i++) {
      out.append(", ").append(Array.get(arr, i));
    }
    out.append(']');

    return out.toString();
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
  void writePrefix(final byte level) throws IOException {
    writePrefix(writer, level);
  }

  void writePrefix(
    @NotNull final Writer writer,
    final byte level
  ) throws IOException {
    writer.write(timePrefix());

    if (isTTY) {
      writer.write(ColorPrefixes[level]);
      writer.write(levelPrefix(level));
      writer.write(ColorReset);
    } else {
      writer.write(levelPrefix(level));
    }

    if (this.level >= LogLevelTrace)
      writer.write(pad());
  }

  /**
   * Writes messages out using System.out.print*, rather than the default
   * writer.
   * <p>
   * Intended to be used as a fallback when unable to safely use one of the
   * {@code write(...)} methods.
   *
   * @param level Log level.  If the currently configured log level is less than
   *              this value, this method is a no-op.
   * @param val   Value to write out.
   *
   * @since 1.1.0
   */
  void backupWrite(final byte level, @Nullable final Object val) {
    System.out.print(timePrefix());
    System.out.print(levelPrefix(level));

    if (this.level >= LogLevelTrace)
      System.out.print(pad());

    System.out.println(val);
  }

  private static final char[][] ColorPrefixes = {
    "\u001B[31m".toCharArray(), // Fatal
    "\u001B[91m".toCharArray(), // Error
    "\u001B[93m".toCharArray(), // Warning
    "\u001B[37m".toCharArray(), // Info
    "\u001B[94m".toCharArray(), // Debug
    "\u001B[90m".toCharArray(), // Trace
  };

  private static final char[] ColorReset = "\u001B[39m".toCharArray();

  /**
   * Log level prefix strings.
   *
   * @since 1.1.0
   */
  private static final char[][] LevelPrefixes = {
    "[FATAL] ".toCharArray(),
    "[ERROR] ".toCharArray(),
    " [WARN] ".toCharArray(),
    " [INFO] ".toCharArray(),
    "[DEBUG] ".toCharArray(),
    "[TRACE] ".toCharArray(),
  };

  /**
   * Injection point string.
   *
   * @since 1.1.0
   */
  private static final String Inject = "{}";

  /**
   * Length of the injection point string.
   *
   * @since 1.1.0
   */
  private static final int    ISize  = Inject.length();

  /**
   * Tree continuation prefix.
   * <p>
   * Used for generating tree prefixes greater than the number of cached
   * prefixes in {@link #padding}.
   *
   * @since 1.1.0
   */
  private static final String   bar     = "  | ";

  /**
   * Tree call prefix.
   * <p>
   * Used for generating tree prefixes greater than the number of cached
   * prefixes in {@link #padding}.
   *
   * @since 1.1.0
   */
  private static final String   com     = "  |- ";

  /**
   * Cached pre-built tree logging prefix strings for call stacks up to a depth
   * of 15.
   *
   * @since 1.1.0
   */
  private static final String[] padding = {
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
  };
}
