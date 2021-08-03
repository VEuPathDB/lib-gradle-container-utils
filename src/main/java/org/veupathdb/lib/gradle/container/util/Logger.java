package org.veupathdb.lib.gradle.container.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class Logger {
  public static final byte LogLevelInfo  = 1;
  public static final byte LogLevelDebug = 2;
  public static final byte LogLevelTrace = 3;

  private final byte level;

  public Logger(byte level) {
    this.level = level;
  }

  public void error(@Nullable final String line) {
    System.out.println(line);
  }

  public void error(@NotNull final String fmt, @Nullable Object val1) {
    System.out.printf(fmt + "%n", val1);
  }

  public void error(
    @NotNull  final String fmt,
    @Nullable final Object val1,
    @Nullable final Object val2
  ) {
    System.out.printf(fmt + "%n", val1, val2);
  }

  public void info(@Nullable final String line) {
    if (level >= LogLevelInfo) {
      System.out.println(line);
    }
  }

  public void info(@NotNull final Supplier<?> messageSupplier) {
    if (level >= LogLevelInfo) {
      System.out.println(messageSupplier.get());
    }
  }

  public void info(@NotNull final String fmt, @Nullable Object val1) {
    if (level >= LogLevelInfo) {
      System.out.printf(fmt + "%n", val1);
    }
  }

  public void info(
    @NotNull  final String fmt,
    @Nullable final Object val1,
    @Nullable final Object val2
  ) {
    if (level >= LogLevelInfo) {
      System.out.printf(fmt + "%n", val1, val2);
    }
  }

  public void debug(@Nullable final String line) {
    if (level >= LogLevelDebug) {
      System.out.println(line);
    }
  }

  public void debug(@NotNull final String fmt, @NotNull final Supplier<?> fn1) {
    if (level >= LogLevelDebug) {
      System.out.printf(fmt + "%n", fn1.get());
    }
  }

  public void debug(@NotNull final String fmt, @Nullable Object val1) {
    if (level >= LogLevelDebug) {
      System.out.printf(fmt + "%n", val1);
    }
  }

  public void debug(@NotNull final String fmt, @Nullable Object val1, @Nullable Object val2) {
    if (level >= LogLevelDebug) {
      System.out.printf(fmt + "%n", val1, val2);
    }
  }

  public void debug(@NotNull final String fmt, @NotNull Supplier<?> fn1, @NotNull Supplier<?> fn2) {
    if (level >= LogLevelDebug) {
      System.out.printf(fmt + "%n", fn1.get(), fn2.get());
    }
  }

  public void trace(@Nullable final String line) {
    if (level >= LogLevelTrace) {
      System.out.println(line);
    }
  }

  public void trace(@NotNull final String fmt, @Nullable Object val1) {
    if (level >= LogLevelTrace) {
      System.out.printf(fmt + "%n", val1);
    }
  }

  public void trace(
    @NotNull final String      fmt,
    @NotNull final Supplier<?> val1,
    @NotNull final Supplier<?> val2)
  {
    if (level >= LogLevelTrace) {
      System.out.printf(fmt + "%n", val1.get(), val2.get());
    }
  }

  public void trace(@NotNull final String fmt, @Nullable Object val1, @Nullable Object val2) {
    if (level >= LogLevelTrace) {
      System.out.printf(fmt + "%n", val1, val2);
    }
  }

  public void trace(
    @NotNull  final String fmt,
    @Nullable final Object val1,
    @Nullable final Object val2,
    @Nullable final Object val3
  ) {
    if (level >= LogLevelTrace) {
      System.out.printf(fmt + "%n", val1, val2, val3);
    }
  }

}
