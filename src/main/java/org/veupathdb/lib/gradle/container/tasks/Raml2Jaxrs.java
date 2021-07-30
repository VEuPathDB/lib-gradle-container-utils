package org.veupathdb.lib.gradle.container.tasks;

import org.gradle.api.Task;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class Raml2Jaxrs extends VendorLib {
  private static final String TmpDir = "jaxrs-build";

  private static final String Raml2JaxrsURL = "https://github.com/mulesoft-labs/raml-for-jax-rs.git";


  public static void execute(Task task) {
    final var buildDir = createTmpDir(task.getProject().getRootDir());

    Git.clone(Raml2JaxrsURL, buildDir);

    correctPoms(buildDir);
  }

  private static void correctPoms(final File tmpDir) {
    try {
      final var it = Files.walk(tmpDir.toPath(), 2)
        .filter(path -> path.endsWith(".pom"))
        .iterator();

      while (it.hasNext()) {
        final var path = it.next();

        Files.writeString(path, Files.readString(path).replaceAll("3.0.[0-9]-SNAPSHOT", "3.0.7"), StandardOpenOption.TRUNCATE_EXISTING);
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to correct raml-for-jaxrs pom file(s)", e);
    }
  }

  private static File createTmpDir(final File root) {
    final var file = new File(root, TmpDir);

    if (file.exists()) {
      throw new RuntimeException("JaxRS build directory exists, remove it and try again.");
    }

    if (!file.mkdir()) {
      throw new RuntimeException("Failed to create JaxRS build directory");
    }

    return file;
  }
}
