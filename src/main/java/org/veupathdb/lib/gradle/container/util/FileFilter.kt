package org.veupathdb.lib.gradle.container.util

import java.io.File
import java.util.function.Predicate

fun interface FileFilter : Predicate<File> {

  companion object {
    @JvmStatic
    fun contains(substring: String): FileFilter {
      return FileFilter { it.name.contains(substring) }
    }

    @JvmStatic
    fun endsWith(suffix: String): FileFilter {
      return FileFilter { it.name.endsWith(suffix) }
    }

    @JvmStatic
    fun beginsWith(prefix: String): FileFilter {
      return FileFilter { it.name.startsWith(prefix) }
    }
  }

  fun and(other: FileFilter): FileFilter {
    return FileFilter { test(it) && other.test(it) }
  }
}
