package org.veupathdb.lib.gradle.container.exec;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.Stack;
import java.util.stream.Collectors;

public class Maven {
  private static final String TargetDir = "target";

  // Commands & Flags
  private static final String
    Command = "mvn",
    Clean   = "clean",
    Install = "install",
    FQuiet  = "--quiet";

  private final Logger Log;

  public Maven(final Project project) {
    Log = project.getLogger();
  }

  public File[] cleanInstall(final File workDir) {
    Log.trace("Maven#cleanInstall(File)");

    final var cmd = new ProcessBuilder(Command, Clean, Install, FQuiet).directory(workDir);

    try {
      final var proc = cmd.start();

      if (proc.waitFor() != 0) {
        throw new RuntimeException(new String(proc.getErrorStream().readAllBytes()));
      }
    } catch (Exception e) {
      Log.error("Failed to build maven project in " + workDir);
      throw new RuntimeException("Failed to build maven project in " + workDir, e);
    }

    return findJars(workDir);
  }

  public File[] findJars(final File workDir) {
    Log.trace("Maven#findJars({})", workDir);

    final var dirs = new Stack<File>();
    final var targ = new Stack<File>();
    final var jars = new Stack<File>();

    dirs.push(workDir);

    // Locate target directories
    while (!dirs.empty()) {

      //noinspection ConstantConditions
      for (final var child : dirs.pop().listFiles()) {

        if (child.isDirectory()) {
          dirs.push(child);

          if (child.getName().equals(TargetDir)) {
            targ.push(child);
          }
        }
      }
    }

    // Scan target dirs for jar files
    while (!targ.empty()) {
      final var dir = targ.pop();

      //noinspection ConstantConditions
      for (final var file : dir.listFiles()) {
        if (file.isFile() && file.getName().endsWith(".jar")) {
          jars.push(file);
        }
      }
    }

    final var out = new File[jars.size()];
    for (int i = 0; i < out.length; i++) {
      out[i] = jars.pop();
    }

    System.out.printf("Located %d output jars.\n", out.length);
    if (Log.isDebugEnabled()) {
      Log.debug(
        "Jar Files: {}",
        Arrays.stream(out)
          .map(File::getName)
          .collect(Collectors.joining("\n  ", "[\n  ", "\n]"))
      );
    }

    return out;
  }
}
