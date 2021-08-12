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

// TODO: allow configuring threshold for what goes to stdout and what goes to stderr.
public class Logger {
  public static final byte LogLevelNone  = 0;
  public static final byte LogLevelError = 1;
  public static final byte LogLevelWarn  = 2;
  public static final byte LogLevelInfo  = 3;
  public static final byte LogLevelDebug = 4;
  public static final byte LogLevelTrace = 5;
  public static final byte LogLevelFatal = 0;

  private static final String OpenNoArg         = "{}#{}()";
  private static final String OpenOneArg        = "{}#{}({})";
  private static final String OpenTwoArg        = "{}#{}({}, {})";
  private static final String OpenThreeArg      = "{}#{}({}, {}, {})";
  private static final String ConstructorOneArg = "{}#new({})";
  private static final String ConstructorTwoArg = "{}#new({}, {})";
  private static final String NoOpNoArg         = "{}#{}(): no-op";
  private static final String NoOpOneArg        = "{}#{}({}): no-op";
  private static final String MapPattern        = "{}#{}({}): {}";

  private final byte           level;
  private final String         projPath;
  private final BufferedWriter writer;
  private final long           start;

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
   * @return The given passthrough value.
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
   * @return Returns the argument {@code out};
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
   * @throws NullPointerException if {@code messageSupplier} is {@code null}
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
   */
  public void info(@NotNull final String fmt, @Nullable Object val1) {
    log(LogLevelInfo, fmt, val1);
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * If {@link #LogLevelDebug} is enabled, prints the given value to stdout.
   *
   * @param val Value to print.
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
   */
  public void debug(@NotNull final String fmt, @Nullable Object val1, @Nullable Object val2) {
    log(LogLevelDebug, fmt, val1, val2);
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  public <T> T fatal(@NotNull final Throwable val) {
    write(writer, LogLevelFatal, val.getMessage());

    throw new RuntimeException(val);
  }

  public <T> T fatal(@Nullable final Object val) {
    final var buf = new StringWriter(64);

    write(new BufferedWriter(buf), LogLevelFatal, val);
    write(writer, LogLevelFatal, buf.toString());

    throw new RuntimeException(buf.toString());
  }

  public void fatal(@NotNull final Throwable err, @Nullable final Object val) {
    final var buf = new StringWriter(64);

    write(new BufferedWriter(buf), LogLevelFatal, val);
    write(writer, LogLevelFatal, buf.toString());

    throw new RuntimeException(buf.toString(), err);
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * If the current configured log level is greater than or equal to the given
   * {@code level}, prints the output of the given {@code Supplier} to stdout.
   *
   * @param fn Method that will be called to retrieve the value to print.
   *           This method will not be called if logging at the specified level
   *           is not enabled.
   * @throws NullPointerException if {@code messageSupplier} is {@code null}
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
   */
  public void log(final byte level, @Nullable final Object val) {
    if (this.level >= level) {
      write(level, val);
    }
  }

  public void log(final byte level, @NotNull final String fmt, @NotNull final Supplier<?> fn1) {
    if (this.level >= level) {
      write(level, fmt, fn1.get());
    }
  }

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
   * @return Stringified value.
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

  @NotNull
  private String pad() {
    if (call < 15) {
      return padding[call];
    } else {
      return bar.repeat(call - 15) + padding[14] + com;
    }
  }

  char[] levelPrefix(final byte level) {
    return LevelPrefixes[level];
  }

  // Max execution time of 9,999,999ms or 4.444444 hours.
  private static final char[] timeBuffer = {'[', '0', '0', '0', '0', '0', '0', '0', ']', ' '};

  char[] timePrefix() {
    final var line = new char[timeBuffer.length];
    System.arraycopy(timeBuffer, 0, line, 0, line.length);

    fillTime(line, System.currentTimeMillis() - start);

    return line;
  }

  void fillTime(final char[] buf, final long time) {
    var pos = buf.length - 3;
    var rem = time;

    while (rem > 0) {
      buf[pos--] = (char) (rem % 10 + '0');
      rem /= 10;
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  void write(final byte level, @Nullable final Object val) {
    write(writer, level, val);
  }

  void write(@NotNull final BufferedWriter writer, final byte level, @Nullable final Object val) {
    try {
      writePrefix(level);

      writer.write(String.valueOf(val));
      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      backupWrite(LogLevelError, "Failed to write to stdout");
      throw new RuntimeException("Failed to write to stdout", e);
    }
  }

  void write(
    @Range(from = 0, to = 5) final byte   level,
    @NotNull                 final String fmt,
    @Nullable                final Object val
  ) {
    write(writer, level, fmt, val);
  }

  void write(
    @NotNull                 final BufferedWriter writer,
    @Range(from = 0, to = 5) final byte           level,
    @NotNull                 final String         fmt,
    @Nullable                final Object         val
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
        pos = inject(pos, ind, count, fmt, val2);
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
        pos = inject(pos, ind++, count, fmt, val2);
      }

      if (pos > -1) {
        prev = pos;
        pos = inject(pos, ind, count, fmt, val3);
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
        pos = inject(pos, ind++, count, fmt, val2);
      }

      if (pos > -1) {
        prev = pos;
        pos = inject(pos, ind++, count, fmt, val3);
      }

      if (pos > -1) {
        prev = pos;
        pos = inject(pos, ind, count, fmt, val4);
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
        pos = inject(pos, ind++, count, fmt, val2);
      }

      if (pos > -1) {
        prev = pos;
        pos = inject(pos, ind++, count, fmt, val3);
      }

      if (pos > -1) {
        prev = pos;
        pos = inject(pos, ind++, count, fmt, val4);
      }

      if (pos > -1) {
        prev = pos;
        pos = inject(pos, ind, count, fmt, val5);
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

  int inject(
    final int start,
    final int index,
    final int count,
    @NotNull final String fmt,
    @Nullable final Object val
  ) throws IOException {
    return inject(writer, start, index, count, fmt, val);
  }

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

  void writePrefix(final byte level) throws IOException {
    writePrefix(writer, level);
  }

  void writePrefix(
    @NotNull final Writer writer,
    final byte level
  ) throws IOException {
    writer.write(timePrefix());
    writer.write(levelPrefix(level));

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
   */
  void backupWrite(final byte level, @Nullable final Object val) {
    System.out.print(timePrefix());
    System.out.print(levelPrefix(level));

    if (this.level >= LogLevelTrace)
      System.out.print(pad());

    System.out.println(val);
  }

  private static final char[][] LevelPrefixes = {
    "[FATAL] ".toCharArray(),
    "[ERROR] ".toCharArray(),
    " [WARN] ".toCharArray(),
    " [INFO] ".toCharArray(),
    "[DEBUG] ".toCharArray(),
    "[TRACE] ".toCharArray(),
  };

  private static final String Inject = "{}";
  private static final int    ISize  = Inject.length();

  private static final String   bar     = "  | ";
  private static final String   com     = "  |- ";
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
