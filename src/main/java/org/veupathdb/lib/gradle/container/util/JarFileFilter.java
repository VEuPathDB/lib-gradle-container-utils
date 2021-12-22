package org.veupathdb.lib.gradle.container.util;

import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Methods for filtering files merged into fat jars.
 *
 * TODO: Add an option to add additional file filters.
 *
 * @since 2.0.0
 */
public class JarFileFilter {

  private static final FileFilter[] JarExclusionExtensions = {
    FileFilter.contains("log4j").and(FileFilter.contains(".dat")),
    FileFilter.endsWith(".sf"),
    FileFilter.endsWith(".dsa"),
    FileFilter.endsWith(".rsa"),
    FileFilter.endsWith(".md"),
  };

  @NotNull
  public static Function<File, Stream<Object>> expandFiles(@NotNull Project project) {
    return file -> {
      if (file.isDirectory())
        return Stream.of(file);

      return Stream.of(project.zipTree(file));
    };
  }

  public static boolean excludeJarFiles(@NotNull File file) {
    for (final var filter : JarExclusionExtensions) {
      if (!filter.test(file))
        return false;
    }

    return true;
  }
}
