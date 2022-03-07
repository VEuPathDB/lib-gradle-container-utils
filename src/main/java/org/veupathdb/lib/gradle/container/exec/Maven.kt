package org.veupathdb.lib.gradle.container.exec

import org.veupathdb.lib.gradle.container.util.Logger
import org.veupathdb.lib.gradle.container.util.StackIterator

import java.io.File
import java.util.Stack
import java.util.stream.Stream
import java.util.stream.StreamSupport

/**
 * Maven Wrapper
 * <p>
 * Provides methods for running Maven targets.
 *
 * @since 1.0.0
 */
class Maven(val Log: Logger) {

  companion object {
    private const val TargetDir = "target"

    // Commands & Flags
    private const val Command = "mvn"

    private const val Clean = "clean"

    private const val Install = "install"

    private const val FQuiet = "--quiet"
  }

  init {
    Log.constructor(Log)
  }

  /**
   * Executes {@code mvn clean install} in the given target directory.
   *
   * @param workDir Command target directory.
   *
   * @since 1.0.0
   */
  fun cleanInstall(workDir: File) {
    Log.open(workDir)

    val cmd = ProcessBuilder(Command, Clean, Install, FQuiet).directory(workDir)

    try {
      val proc = cmd.start()

      if (proc.waitFor() != 0) {
        println(String(proc.inputStream.readAllBytes()))

        throw RuntimeException(String(proc.errorStream.readAllBytes()))
      }
    } catch (e: Exception) {
      Log.fatal(e, "Failed to build maven project in {}", workDir)
    }

    Log.close()
  }

  /**
   * Recursively locates all the jar files in the given directory.
   *
   * @param workDir Target directory to crawl for jar files.
   *
   * @return A stream containing zero or more located jar files.
   *
   * @since 1.1.0
   */
  fun findOutputJars(workDir: File): Stream<File> {
    Log.open(workDir)

    val allDirs = Stack<File>()
    val targetDirs = Stack<File>()
    val jarFiles = Stack<File>()

    allDirs.push(workDir)

    // Locate target directories
    while (!allDirs.empty()) {

      //noinspection ConstantConditions
      for (child in allDirs.pop().listFiles()!!) {

        if (child.isDirectory) {
          allDirs.push(child)

          if (child.name.equals(TargetDir)) {
            targetDirs.push(child)
          }
        }
      }
    }

    // Scan target dirs for jar files
    while (!targetDirs.empty()) {
      val dir = targetDirs.pop()

      //noinspection ConstantConditions
      for (file in dir.listFiles()!!) {
        if (file.isFile && file.name.endsWith(".jar")) {
          jarFiles.push(file)
        }
      }
    }

    Log.info("Located {} output jars.", jarFiles.size)

    return Log.close(StreamSupport.stream(StackIterator(jarFiles), false))
  }
}
