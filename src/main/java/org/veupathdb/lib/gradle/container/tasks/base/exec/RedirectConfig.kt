package org.veupathdb.lib.gradle.container.tasks.base.exec

import java.io.*
import java.util.Objects

@SuppressWarnings("unused")
class RedirectConfig {
  companion object {
    const val ToStdOut:  Byte = 0
    const val ToStdErr:  Byte = 1
    const val ToDevNull: Byte = 2
    const val ToFile:    Byte = 3
    const val ToWriter:  Byte = 4
    const val ToPrint:   Byte = 5
    const val ToStream:  Byte = 6

    @JvmStatic
    fun toStdOut() = RedirectConfig(ToStdOut)

    @JvmStatic
    fun toStdErr() = RedirectConfig(ToStdErr)

    @JvmStatic
    fun toDevNull() = RedirectConfig(ToDevNull)

    @JvmStatic
    fun toFile(file: String) = RedirectConfig(File(file))

    @JvmStatic
    fun toFile(file: File) = RedirectConfig(Objects.requireNonNull(file))

    @JvmStatic
    fun toWriter(writer: Writer) = RedirectConfig(writer)

    @JvmStatic
    fun toPrintStream(stream: PrintStream) = RedirectConfig(stream)

    @JvmStatic
    fun toStream(stream: OutputStream) = RedirectConfig(stream)
  }

  private val value: Any?

  private val type: Byte

  private constructor(type: Byte) {
    this.value = null
    this.type  = type
  }

  private constructor(file: File) {
    type  = ToFile
    value = file
  }

  private constructor(writer: PrintStream) {
    type  = ToPrint
    value = writer
  }

  private constructor(writer: Writer) {
    type  = ToWriter
    value = writer
  }

  private constructor(writer: OutputStream) {
    type  = ToStream
    value = writer
  }

  fun forStdOut(proc: Process) = apply(proc.inputStream)

  fun forStdErr(proc: Process) = apply(proc.errorStream)

  fun apply(`in`: InputStream) {
    when (type) {
      ToStdOut -> applyStd(`in`, System.out)
      ToStdErr -> applyStd(`in`, System.err)
      ToDevNull -> pipe(`in`, DiscardingOutputStream())
      ToFile -> applyFile(`in`, value as File)
      ToWriter -> applyWriter(`in`, value as Writer)
      ToPrint -> applyStd(`in`, value as PrintStream)
      ToStream -> pipe(`in`, value as OutputStream)
    }
  }

  private fun applyStd(stream: InputStream, tgt: PrintStream) {
    val buf = when(stream) {
      is BufferedInputStream -> stream
      else                   -> BufferedInputStream(stream)
    }

    pipe(buf, BufferedOutputStream(tgt))
  }

  fun applyFile(i: InputStream, o: File) {
    if (!o.exists()) {
      //noinspection ResultOfMethodCallIgnored
      o.createNewFile()
    }

    pipe(i, FileOutputStream(o))
  }

  fun applyWriter(i: InputStream, o: Writer) = pipe(i, WriterOutputStream(o))

  fun pipe(i: InputStream, o: OutputStream) {
    val input = when(i) {
      is BufferedInputStream -> i
      else                   -> BufferedInputStream(i)
    }

    val output = when(o) {
      is BufferedOutputStream -> o
      else                    -> BufferedOutputStream(o)
    }

    pipe(input, output)
  }

  fun pipe(i: BufferedInputStream, o: BufferedOutputStream) {
    Thread {
      val bytes = ByteArray(8192)
      var read = i.read(bytes)

      while (read > -1) {
        o.write(bytes, 0, read)
        o.flush()
        read = i.read(bytes)
      }
    }.start()
  }

  //
  //
  // Helper Classes
  //
  //

  private class WriterOutputStream(val writer: Writer): OutputStream() {
    override fun write(b: Int) { writer.write(b) }
  }

  private class DiscardingOutputStream: OutputStream() {
    override fun write(b: Int) {
      // Do nothing.
    }

    override fun write(b: ByteArray) {
      // Do nothing.
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
      // Do nothing.
    }

    override fun flush() {
      // Do nothing.
    }

    override fun close() {
      // Do nothing.
    }
  }
}
