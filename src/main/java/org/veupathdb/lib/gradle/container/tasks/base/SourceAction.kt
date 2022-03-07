package org.veupathdb.lib.gradle.container.tasks.base

import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

import java.io.File
import java.util.stream.Stream

abstract class SourceAction : Action() {

  companion object {
    private const val LogResDirsCheck = "Testing resource directory {}"
    private const val LogResDirsFound = "Found resource directory {}"
  }

  @Internal
  protected fun getProjectSourceDirectories(): Stream<File> {
    log.open()

    val projectPath = projectConfig().projectPackage.replace('.', '/')
    log.debug("Project path: {}", projectPath)

    return log.close(getSourceDirectories()
      .map { File(it, projectPath) }
      .filter(File::exists))
  }

  /**
   * Returns a stream containing all the source directories for the current
   * project.
   * <p>
   * All files in the stream are guaranteed to have existed as of the time of
   * this function call.
   *
   * @return Stream of source directories.
   */
  @Internal
  protected fun getSourceDirectories(): Stream<File> =
    log.getter(
      getSourceSets()
        .stream()
        .map(SourceSet::getAllSource)
        .map(SourceDirectorySet::getSrcDirs)
        .flatMap(Collection<File>::stream)
        .filter(File::exists))

  /**
   * Returns a stream containing all the resource directories for the current
   * project.
   * <p>
   * All files in the stream are guaranteed to have existed as of the time of
   * this function call.
   *
   * @return Stream of resource directories.
   */
  @Internal
  protected fun getResourceDirectories(): Stream<File> {
    log.open()

    var out = getSourceSets()
      .stream()
      .map(SourceSet::getResources)
      .map(SourceDirectorySet::getSrcDirs)
      .flatMap(Collection<File>::stream)

    if (log.isDebug) {
      out = out.peek(newFileLogger(LogResDirsCheck))
    }

    out = out.filter(File::exists)

    if (log.isDebug) {
      out = out.peek(newFileLogger(LogResDirsFound))
    }

    return log.close(out)
  }

  @Internal
  protected fun getSourceSets(): SourceSetContainer {
    return javaPluginExtension.sourceSets
  }

  protected fun newFileLogger(prefix: String): (File) -> Unit =
    if (log.isDebug)
      { f -> log.debug(prefix, f) }
    else
      { _ -> }

  protected interface SourceSearchResult {
    val file: File
    val line: Int
    val found: Boolean
  }

  protected class EmptySearchResult : SourceSearchResult {

    companion object {
      @JvmStatic
      val Instance: SourceSearchResult = EmptySearchResult()
    }

    override val file
      get() = throw RuntimeException("Cannot get file from empty result.")

    override val line
      get() = throw RuntimeException("Cannot get line number from empty result.")

    override val found = false
  }

  protected class FullSearchResult(
    override val file: File,
    override val line: Int
  ) : SourceSearchResult {
    override val found = true
  }
}
