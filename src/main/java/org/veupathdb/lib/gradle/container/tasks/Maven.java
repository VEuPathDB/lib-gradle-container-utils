package org.veupathdb.lib.gradle.container.tasks;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Maven {
  private static final String TargetDir = "target";

  // Commands & Flags
  private static final String
    Command = "mvn",
    Clean   = "clean",
    Install = "install";

  private final Logger Log;

  public Maven(final Project project) {
    Log = project.getLogger();
  }

  public File[] cleanInstall(final File workDir) {
    try {
      final var proc = Runtime.getRuntime().exec(
        new String[]{Command, Clean, Install},
        new String[0],
        workDir
      );

      if (proc.waitFor() != 0) {
        throw new RuntimeException(new String(proc.getErrorStream().readAllBytes()));
      }
    } catch (Exception e) {
      Log.error("Failed to build maven project in " + workDir);
      throw new RuntimeException("Failed to build maven project in " + workDir);
    }

    try {
      return Files.walk(workDir.toPath(), 1)
        .map(Path::toFile)
        .filter(File::isDirectory)
        .map(file -> new File(file, TargetDir))
        .map(file -> file.listFiles((dir, name) -> name.endsWith(".jar")))
        .flatMap(Arrays::stream)
        .toArray(File[]::new);
    } catch (Exception e) {
      Log.error("Failed to collect compiled jars from " + workDir);
      throw new RuntimeException("Failed to collect compiled jars from " + workDir, e);
    }
  }
}
