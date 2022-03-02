package org.veupathdb.lib.gradle.container.exec;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.util.Logger;
import org.veupathdb.lib.gradle.container.util.StackIterator;

import java.io.File;
import java.util.Stack;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Maven Wrapper
 * <p>
 * Provides methods for running Maven targets.
 *
 * @since 1.0.0
 */
public class Maven {
  private static final String TargetDir = "target";

  // Commands & Flags
  private static final String
    Command = "mvn",
    Clean   = "clean",
    Install = "install",
    FQuiet  = "--quiet";

  @NotNull
  private final Logger Log;

  public Maven(@NotNull final Logger log) {
    log.constructor(log);
    Log = log;
  }

  /**
   * Executes {@code mvn clean install} in the given target directory.
   *
   * @param workDir Command target directory.
   *
   * @since 1.0.0
   */
  public void cleanInstall(@NotNull final File workDir) {
    Log.open(workDir);

    final var cmd = new ProcessBuilder(Command, Clean, Install, FQuiet).directory(workDir);

    try {
      final var proc = cmd.start();

      if (proc.waitFor() != 0) {
        System.out.println(new String(proc.getInputStream().readAllBytes()));

        throw new RuntimeException(new String(proc.getErrorStream().readAllBytes()));
      }
    } catch (Exception e) {
      Log.fatal(e, "Failed to build maven project in {}", workDir);
    }

    Log.close();
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
  @NotNull
  public Stream<File> findOutputJars(@NotNull final File workDir) {
    Log.open(workDir);

    final var allDirs    = new Stack<File>();
    final var targetDirs = new Stack<File>();
    final var jarFiles   = new Stack<File>();

    allDirs.push(workDir);

    // Locate target directories
    while (!allDirs.empty()) {

      //noinspection ConstantConditions
      for (final var child : allDirs.pop().listFiles()) {

        if (child.isDirectory()) {
          allDirs.push(child);

          if (child.getName().equals(TargetDir)) {
            targetDirs.push(child);
          }
        }
      }
    }

    // Scan target dirs for jar files
    while (!targetDirs.empty()) {
      final var dir = targetDirs.pop();

      //noinspection ConstantConditions
      for (final var file : dir.listFiles()) {
        if (file.isFile() && file.getName().endsWith(".jar")) {
          jarFiles.push(file);
        }
      }
    }

    Log.info("Located {} output jars.", jarFiles.size());

    return Log.close(StreamSupport.stream(new StackIterator<>(jarFiles), false));
  }
}
