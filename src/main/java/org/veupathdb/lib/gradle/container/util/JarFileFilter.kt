package org.veupathdb.lib.gradle.container.util

import org.gradle.api.Project
import java.io.File

import java.util.stream.Stream
import java.util.function.Function

/**
 * Methods for filtering files merged into fat jars.
 *
 * TODO: Add an option to add additional file filters.
 *
 * @since 2.0.0
 */
object JarFileFilter {

  @JvmStatic
  private val JarExclusionExtensions = arrayOf(
    FileFilter.contains("log4j").and(FileFilter.contains(".dat")),
    FileFilter.endsWith(".sf"),
    FileFilter.endsWith(".dsa"),
    FileFilter.endsWith(".rsa"),
    FileFilter.endsWith(".md"),
  )

  @JvmStatic
  fun expandFiles(project: Project) =
    Function<File, Stream<File>> { file ->
      if (file.isDirectory)
        return@Function Stream.of(file)

      return@Function project.zipTree(file)
        .filter(JarFileFilter::excludeJarFiles)
        .files
        .stream()
    }

  @JvmStatic
  fun excludeJarFiles(file: File): Boolean {
    for (filter in JarExclusionExtensions) {
      if (filter.test(file))
        return false
    }

    return true
  }
}
