package org.veupathdb.lib.gradle.container.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Path;
import java.util.function.Supplier;

public class Logger {
  @SuppressWarnings("unused") public static final byte LogLevelNone  = 0;
  @SuppressWarnings("unused") public static final byte LogLevelError = 1;
  @SuppressWarnings("unused") public static final byte LogLevelWarn  = 2;
  @SuppressWarnings("unused") public static final byte LogLevelInfo  = 3;
  @SuppressWarnings("unused") public static final byte LogLevelDebug = 4;
  @SuppressWarnings("unused") public static final byte LogLevelTrace = 5;

  private final byte           level;
  private final String         projPath;
  private final BufferedWriter writer;
  private final long           start;

  private int call;

  public Logger(final byte level, final File rootDir) {
    this.start    = System.currentTimeMillis();
    this.level    = level;
    this.projPath = rootDir.getPath();

    try {
      this.writer = new BufferedWriter(new FileWriter("/dev/stdout"));
    } catch (IOException e) {
      backupWrite(LogLevelError, "Failed to open stdout.");
      throw new RuntimeException("Failed to open stdout.", e);
    }
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
        "%s#%s()",
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
        "%s#%s(%s)",
        stack.getClassName().substring(stack.getClassName().lastIndexOf('.') + 1),
        stack.getMethodName(), arg1
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
        "%s#%s(%s, %s)",
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
        "%s#%s(%s, %s, %s)",
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
   * Decrements the call stack count, and if trace logging is enabled, prints
   * a void return message.
   */
  public void close() {
    log(LogLevelTrace, "return: void");
    call--;
  }

  /**
   * Decrements the call stack count and returns the given value.  If trace
   * logging is enabled, also prints a log statement with the return value.
   *
   * @param val Passthrough value.
   * @param <T> Type of the passthrough value.
   *
   * @return The given passthrough value.
   */
  @Contract("null -> null")
  public <T> T close(final T val) {
    log(LogLevelTrace, "return: %s", val);
    call--;
    return val;
  }

  @Contract("null -> null")
  public <T> T getter(final T val) {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(
        LogLevelTrace,
        "%s#%s() -> %s",
        stack.getClassName().substring(stack.getClassName().lastIndexOf('.') + 1),
        stack.getMethodName(),
        val
      );
    }

    return val;
  }

  public void constructor(@Nullable final Object arg1) {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(
        LogLevelTrace,
        "%s#new(%s)",
        stack.getClassName().substring(stack.getClassName().lastIndexOf('.') + 1),
        arg1
      );
    }
  }

  public void noop() {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(
        LogLevelTrace,
        "%s#%s(): no op",
        stack.getClassName().substring(stack.getClassName().lastIndexOf('.') + 1),
        stack.getMethodName()
      );
    }
  }

  public void noop(@Nullable final Object arg1) {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(
        LogLevelTrace,
        "%s#%s(%s): no op",
        stack.getClassName().substring(stack.getClassName().lastIndexOf('.') + 1),
        stack.getMethodName(),
        arg1
      );
    }
  }

  @Contract("_, null -> null")
  public <T> T map(@Nullable final Object in, @Nullable final T out) {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(
        LogLevelTrace,
        "%s#%s(%s): %s",
        stack.getClassName().substring(stack.getClassName().lastIndexOf('.') + 1),
        stack.getMethodName(),
        in,
        out
      );
    }

    return out;
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  public void error(@Nullable final Object val) {
    log(LogLevelError, val);
  }

  public void error(@NotNull final String fmt, @Nullable Object val1) {
    log(LogLevelError, fmt, val1);
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  public void info(@Nullable final Object val) {
    log(LogLevelInfo, val);
  }

  public void info(@NotNull final Supplier<?> messageSupplier) {
    log(LogLevelInfo, messageSupplier);
  }

  public void info(@NotNull final String fmt, @Nullable Object val1) {
    log(LogLevelInfo, fmt, val1);
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  public void debug(@Nullable final Object val) {
    log(LogLevelDebug, val);
  }

  public void debug(@NotNull final String fmt, @Nullable Object val1) {
    log(LogLevelDebug, fmt, val1);
  }

  public void debug(@NotNull final String fmt, @NotNull Supplier<?> fn1, @NotNull Supplier<?> fn2) {
    log(LogLevelDebug, fmt, fn1, fn2);
  }

  public void debug(@NotNull final String fmt, @Nullable Object val1, @Nullable Object val2) {
    log(LogLevelDebug, fmt, val1, val2);
  }

  public void debug(
    @NotNull final String fmt,
    @Nullable final Object val1,
    @Nullable final Object val2,
    @Nullable final Object val3
  ) {
    log(LogLevelDebug, fmt, val1, val2, val3);
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  public void log(final byte level, @NotNull final Supplier<?> fn) {
    if (this.level >= level) {
      write(level, fn.get());
    }
  }

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

  public void log(final byte level, @NotNull final String fmt, @Nullable final Object val1) {
    if (this.level >= level) {
      write(level, fmt, val1);
    }
  }

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

  @NotNull
  private String stringify(@Nullable final Object val) {
    final String tmp;

    if (val == null)
      return "null";

    if (val instanceof String)
      return (String) val;

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

    return String.valueOf(val);
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
    return LevelPrefixes[level - 1];
  }

  // Max execution time of 9,999,999ms or 4.444444 hours.
  private final char[] timeBuffer = {'[', '0', '0', '0', '0', '0', '0', '0', ']', ' '};

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
      buf[pos--] = (char) (rem % 10);
      rem /= 10;
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  void write(final byte level, @Nullable final Object val) {
    try {
      writePrefix(level);

      writer.write(String.valueOf(val));
      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  void write(final byte level, @NotNull final String fmt, @Nullable final Object val) {
    try {
      writePrefix(level);
      inject(0, 0, 1, fmt, val);

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
    @Nullable final Object val2
  ) {
    final var count = 2;
    var ind = 0;
    var pos = 0;

    try {
      writePrefix(level);

      pos = inject(pos, ind++, count, fmt, val1);

      if (pos > -1) {
        inject(pos, ind, count, fmt, val2);
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
    var ind = 0;
    var pos = 0;

    try {
      writePrefix(level);

      pos = inject(pos, ind++, count, fmt, val1);

      if (pos > -1) {
        pos = inject(pos, ind++, count, fmt, val2);
      }

      if (pos > -1) {
        inject(pos, ind, count, fmt, val3);
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
    var ind = 0;
    var pos = 0;

    try {
      writePrefix(level);

      pos = inject(pos, ind++, count, fmt, val1);

      if (pos > -1) {
        pos = inject(pos, ind++, count, fmt, val2);
      }

      if (pos > -1) {
        pos = inject(pos, ind++, count, fmt, val3);
      }

      if (pos > -1) {
        inject(pos, ind, count, fmt, val4);
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
    var ind = 0;
    var pos = 0;

    try {
      writePrefix(level);

      pos = inject(pos, ind++, count, fmt, val1);

      if (pos > -1) {
        pos = inject(pos, ind++, count, fmt, val2);
      }

      if (pos > -1) {
        pos = inject(pos, ind++, count, fmt, val3);
      }

      if (pos > -1) {
        pos = inject(pos, ind++, count, fmt, val4);
      }

      if (pos > -1)
        inject(pos, ind, count, fmt, val5);

      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      backupWrite(LogLevelError, "Failed to write to stdout");
      throw new RuntimeException("Failed to write to stdout", e);
    }
  }

  int inject(
    final int x,
    final int ind,
    final int count,
    @NotNull final String fmt,
    @Nullable final Object val
  ) throws IOException {
    final var pos = fmt.indexOf(Inject, x);

    if (pos < 0) {
      backupWrite(LogLevelWarn, "Logger: " + count + " parameters provided but there are " + (
        ind == 0
          ? "no placeholders in format string."
          : "only " + ind+1 + " placeholders in format string"
      ));
      writer.write(fmt);
      return -1;
    }

    writer.write(fmt.substring(x, pos));
    writer.write(stringify(val));

    return pos + ISize;
  }


  void writePrefix(final byte level) throws IOException {
    writer.write(timePrefix());
    writer.write(levelPrefix(level));

    if (level >= LogLevelTrace)
      writer.write(pad());
  }

  void backupWrite(final byte level, @Nullable final Object val) {
    System.out.print(timePrefix());
    System.out.print(levelPrefix(level));

    if (level >= LogLevelTrace)
      System.out.print(pad());

    System.out.println(val);
  }

  private static final char[][] LevelPrefixes = {
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
    "  |- ",
    "  |   |- ",
    "  |   |   |- ",
    "  |   |   |   |- ",
    "  |   |   |   |   |- ",
    "  |   |   |   |   |   |- ",
    "  |   |   |   |   |   |   |- ",
    "  |   |   |   |   |   |   |   |- ",
    "  |   |   |   |   |   |   |   |   |- ",
    "  |   |   |   |   |   |   |   |   |   |- ",
    "  |   |   |   |   |   |   |   |   |   |   |- ",
    "  |   |   |   |   |   |   |   |   |   |   |   |- ",
    "  |   |   |   |   |   |   |   |   |   |   |   |   |- ",
    "  |   |   |   |   |   |   |   |   |   |   |   |   |   |- ",
  };
}
