package org.veupathdb.lib.gradle.container.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Objects;

@SuppressWarnings("unused")
public class RedirectConfig {
  private static final byte ToStdOut  = 0;
  private static final byte ToStdErr  = 1;
  private static final byte ToDevNull = 2;
  private static final byte ToFile    = 3;
  private static final byte ToWriter  = 4;
  private static final byte ToPrint   = 5;
  private static final byte ToStream  = 6;

  private final Object value;

  private final byte type;

  private RedirectConfig(byte type) {
    this.value = null;
    this.type  = type;
  }

  private RedirectConfig(@NotNull File file) {
    type  = ToFile;
    value = file;
  }

  private RedirectConfig(@NotNull PrintStream writer) {
    type  = ToPrint;
    value = writer;
  }

  private RedirectConfig(@NotNull Writer writer) {
    type  = ToWriter;
    value = writer;
  }

  private RedirectConfig(@NotNull OutputStream writer) {
    type  = ToStream;
    value = writer;
  }

  @Contract(pure = true)
  public void forStdOut(@NotNull Process proc) {
    apply(proc.getInputStream());
  }

  @Contract(pure = true)
  public void forStdErr(@NotNull Process proc) {
    apply(proc.getErrorStream());
  }

  @Contract(pure = true)
  @SuppressWarnings("ConstantConditions")
  private void apply(@NotNull InputStream in) {
    switch (type) {
      case ToStdOut -> applyStd(in, System.out);
      case ToStdErr -> applyStd(in, System.err);
      case ToDevNull -> pipe(in, new DiscardingOutputStream());
      case ToFile -> applyFile(in, (File) value);
      case ToWriter -> applyWriter(in, (Writer) value);
      case ToPrint -> applyStd(in, (PrintStream) value);
      case ToStream -> pipe(in, (OutputStream) value);
    }
  }

  @Contract(pure = true)
  private void applyStd(@NotNull InputStream stream, @NotNull PrintStream tgt) {
    final var buf = stream instanceof BufferedInputStream
      ? (BufferedInputStream) stream
      : new BufferedInputStream(stream);

    pipe(buf, new BufferedOutputStream(tgt));
  }

  @Contract(pure = true)
  private void applyFile(@NotNull InputStream in, @NotNull File out) {
    try {
      if (!out.exists()) {
        //noinspection ResultOfMethodCallIgnored
        out.createNewFile();
      }

      pipe(in, new FileOutputStream(out));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Contract(pure = true)
  private void applyWriter(@NotNull InputStream in, @NotNull Writer out) {
    pipe(in, new WriterOutputStream(out));
  }

  @Contract(pure = true)
  private void pipe(@NotNull InputStream in, @NotNull OutputStream out) {
    var input = in instanceof BufferedInputStream
      ? (BufferedInputStream) in
      : new BufferedInputStream(in);

    var output = out instanceof BufferedOutputStream
      ? (BufferedOutputStream) out
      : new BufferedOutputStream(out);

    pipe(input, output);
  }

  @Contract(pure = true)
  private void pipe(@NotNull BufferedInputStream in, @NotNull BufferedOutputStream out) {

    new Thread(() -> {
      final var bytes = new byte[8192];

      try {
        var read = 0;
        while ((read = in.read(bytes)) > -1) {
          out.write(bytes, 0, read);
          out.flush();
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      } finally {
        try {
          out.close();
        } catch (IOException e) {
          //noinspection ThrowFromFinallyBlock
          throw new RuntimeException(e);
        }
      }
    }).start();
  }

  //
  //
  // Static Constructors
  //
  //


  @NotNull
  public static RedirectConfig toStdOut() {
    return new RedirectConfig(ToStdOut);
  }

  @NotNull
  public static RedirectConfig toStdErr() {
    return new RedirectConfig(ToStdErr);
  }

  @NotNull
  public static RedirectConfig toDevNull() {
    return new RedirectConfig(ToDevNull);
  }

  @NotNull
  public static RedirectConfig toFile(@NotNull String file) {
    return new RedirectConfig(new File(Objects.requireNonNull(file)));
  }

  @NotNull
  public static RedirectConfig toFile(@NotNull File file) {
    return new RedirectConfig(Objects.requireNonNull(file));
  }

  @NotNull
  public static RedirectConfig toWriter(@NotNull Writer writer) {
    return new RedirectConfig(Objects.requireNonNull(writer));
  }

  @NotNull
  public static RedirectConfig toPrintStream(@NotNull PrintStream stream) {
    return new RedirectConfig(Objects.requireNonNull(stream));
  }

  @NotNull
  public static RedirectConfig toStream(@NotNull OutputStream stream) {
    return new RedirectConfig(Objects.requireNonNull(stream));
  }

  //
  //
  // Helper Classes
  //
  //

  private static class WriterOutputStream extends OutputStream {
    private final Writer writer;

    public WriterOutputStream(Writer writer) {
      this.writer = writer;
    }

    @Override
    public void write(int b) throws IOException {
      writer.write(b);
    }
  }

  private static class DiscardingOutputStream extends OutputStream {
    @Override
    public void write(int b) {
      // Do nothing.
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public void write(@NotNull byte[] b) {
      // Do nothing.
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public void write(@NotNull byte[] b, int off, int len) {
      // Do nothing.
    }

    @Override
    public void flush() {
      // Do nothing.
    }

    @Override
    public void close() {
      // Do nothing.
    }
  }
}
