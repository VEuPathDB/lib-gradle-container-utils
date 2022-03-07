package org.veupathdb.lib.gradle.container.util

import org.gradle.api.file.FileTreeElement

/**
 * Methods for filtering files merged into fat jars.
 *
 * TODO: Add an option to add additional file filters.
 *
 * @since 2.0.0
 */
object JarFileFilter {

  @JvmStatic
  private val JarExclusionExtensions = arrayOf<(String) -> Boolean>(
    { it.contains("log4j") && it.contains(".dat") },
    { it.endsWith(".sf") },
    { it.endsWith(".dsa") },
    { it.endsWith(".rsa") },
    { it.endsWith(".md") },
  )

  @JvmStatic
  fun excludeJarFiles(file: FileTreeElement): Boolean {
    val name = file.name.toLowerCase()

    for (filter in JarExclusionExtensions) {
      if (filter(name))
        return false
    }

    return true
  }
}
