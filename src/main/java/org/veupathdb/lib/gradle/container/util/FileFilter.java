package org.veupathdb.lib.gradle.container.util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.function.Predicate;

@FunctionalInterface
public interface FileFilter extends Predicate<File> {

  @NotNull
  static FileFilter contains(@NotNull String substring) {
    return f -> f.getName().contains(substring);
  }

  @NotNull
  static FileFilter endsWith(@NotNull String suffix) {
    return f -> f.getName().endsWith(suffix);
  }

  static FileFilter beginsWith(@NotNull String prefix) {
    return f -> f.getName().startsWith(prefix);
  }

  @NotNull
  default FileFilter and(@NotNull FileFilter other) {
    return f -> test(f) && other.test(f);
  }
}
