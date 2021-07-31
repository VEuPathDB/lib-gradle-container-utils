package org.veupathdb.lib.gradle.container.tasks;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.*;
import java.util.Arrays;

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
    try {
      final var proc = Runtime.getRuntime().exec(
        new String[]{Command, Clean, Install, FQuiet},
        new String[0],
        workDir
      );

      if (proc.waitFor() != 0) {
        throw new RuntimeException(new String(proc.getErrorStream().readAllBytes()));
      }
    } catch (Exception e) {
      Log.error("Failed to build maven project in " + workDir);
      throw new RuntimeException("Failed to build maven project in " + workDir, e);
    }

    try {
      //noinspection ConstantConditions
      return Arrays.stream(workDir.listFiles())
        .filter(File::isDirectory)
        .map(f -> new File(f, TargetDir))
        .filter(File::exists)
        .map(File::listFiles)
        .flatMap(Arrays::stream)
        .filter(f -> f.getName().endsWith(".jar"))
        .toArray(File[]::new);
    } catch (Exception e) {
      Log.error("Failed to collect compiled jars from " + workDir);
      throw new RuntimeException("Failed to collect compiled jars from " + workDir, e);
    }
  }
}
