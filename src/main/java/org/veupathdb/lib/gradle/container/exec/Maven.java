package org.veupathdb.lib.gradle.container.exec;

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
