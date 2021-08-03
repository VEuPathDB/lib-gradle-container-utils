package org.veupathdb.lib.gradle.container.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class Logger {
  public static final byte LogLevelNone  = 0;
  public static final byte LogLevelError = 1;
  public static final byte LogLevelWarn  = 2;
  public static final byte LogLevelInfo  = 3;
  public static final byte LogLevelDebug = 4;
  public static final byte LogLevelTrace = 5;

  private final byte level;

  private int call;

  public Logger(byte level) {
    this.level = level;
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  public void open() {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(LogLevelTrace, "%s#%s()", stack.getClassName(), stack.getMethodName());
    }
    call++;
  }

  public void open(@Nullable final Object arg1) {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(LogLevelTrace, "%s#%s(%s)", stack.getClassName(), stack.getMethodName(), arg1);
    }
    call++;
  }

  public void open(@Nullable final Object arg1, @Nullable final Object arg2) {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(LogLevelTrace, "%s#%s(%s, %s, %s)", stack.getClassName(), stack.getMethodName(), arg1, arg2);
    }
    call++;
  }

  public void open(
    @Nullable final Object arg1,
    @Nullable final Object arg2,
    @Nullable final Object arg3
  ) {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(LogLevelTrace, "%s#%s(%s, %s, %s)", stack.getClassName(), stack.getMethodName(), arg1, arg2, arg3);
    }
    call++;
  }

  public void close() {
    log(LogLevelTrace, "return: void");
    call--;
  }

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
      log(LogLevelTrace, "%s#%s() -> %s", stack.getClassName(), stack.getMethodName(), val);
    }
    return val;
  }

  public void constructor(@Nullable final Object arg1) {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(LogLevelTrace, "%s#new(%s)", stack.getClassName(), arg1);
    }
  }

  public void noop() {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(LogLevelTrace, "%s#%s(): no op", stack.getClassName(), stack.getMethodName());
    }
  }

  public void noop(@Nullable final Object arg1) {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(LogLevelTrace, "%s#%s(%s): no op", stack.getClassName(), stack.getMethodName(), arg1);
    }
  }

  @Contract("_, null -> null")
  public <T> T map(@Nullable final Object in, @Nullable final T out) {
    if (level >= LogLevelTrace) {
      final var stack = new Exception().getStackTrace()[1];
      log(LogLevelTrace, "%s#%s(%s): %s", stack.getClassName(), stack.getMethodName(), in, out);
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

  public void info(
    @NotNull final String fmt,
    @Nullable final Object val1,
    @Nullable final Object val2
  ) {
    log(LogLevelInfo, fmt, val1, val2);
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  public void debug(@Nullable final Object val) {
    log(LogLevelDebug, val);
  }

  public void debug(@NotNull final String fmt, @NotNull final Supplier<?> fn1) {
    log(LogLevelDebug, fmt, fn1);
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

  ////////////////////////////////////////////////////////////////////////////////////////////////

  public void log(final byte level, @NotNull final Supplier<?> fn) {
    if (this.level >= level) {
      System.out.println(pad(call) + fn.get());
    }
  }

  public void log(final byte level, @Nullable final Object val) {
    if (this.level >= level) {
      System.out.println(pad(call) + val);
    }
  }

  public void log(final byte level, @NotNull final String fmt, @NotNull final Supplier<?> fn1) {
    if (this.level >= level) {
      System.out.printf(pad(call) + fmt + "%n", fn1.get());
    }
  }

  public void log(
    final byte level,
    @NotNull final String fmt,
    @NotNull final Supplier<?> fn1,
    @NotNull final Supplier<?> fn2
  ) {
    if (this.level >= level) {
      System.out.printf(pad(call) + fmt + "%n", fn1.get(), fn2.get());
    }
  }

  public void log(final byte level, @NotNull final String fmt, @Nullable final Object val1) {
    if (this.level >= level) {
      System.out.printf(pad(call) + fmt + "%n", val1);
    }
  }

  public void log(
    final byte level,
    @NotNull final String fmt,
    @Nullable final Object val1,
    @Nullable final Object val2
  ) {
    if (this.level >= level) {
      System.out.printf(pad(call) + fmt + "%n", val1, val2);
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
      System.out.printf(pad(call) + fmt + "%n", val1, val2, val3);
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
      System.out.printf(pad(call) + fmt + "%n", val1, val2, val3, val4);
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
      System.out.printf(pad(call) + fmt + "%n", val1, val2, val3, val4, val5);
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  @NotNull
  private static String pad(int ind) {
    if (ind < 15) {
      return padding[ind];
    } else {
      return bar.repeat(ind - 15) + padding[14] + com;
    }
  }

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
