package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class SourceAction extends Action {
  private static final String LogResDirsCheck = "Testing resource directory {}";
  private static final String LogResDirsFound = "Found resource directory {}";


  @NotNull
  @Internal
  protected Stream<File> getProjectSourceDirectories() {
    log().open();

    final var projectPath = projectConfig().getProjectPackage().replace('.', '/');
    log().debug("Project path: {}", projectPath);

    return log().close(getSourceDirectories()
      .map(f -> new File(f, projectPath))
      .filter(File::exists));
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
  @NotNull
  @Internal
  protected Stream<File> getSourceDirectories() {
    return log().getter(
      getSourceSets()
        .stream()
        .map(SourceSet::getAllSource)
        .map(SourceDirectorySet::getSrcDirs)
        .flatMap(Collection::stream)
        .filter(File::exists));
  }

  /**
   * Returns a stream containing all the resource directories for the current
   * project.
   * <p>
   * All files in the stream are guaranteed to have existed as of the time of
   * this function call.
   *
   * @return Stream of resource directories.
   */
  @NotNull
  @Internal
  protected Stream<File> getResourceDirectories() {
    log().open();

    var out = getSourceSets()
      .stream()
      .map(SourceSet::getResources)
      .map(SourceDirectorySet::getSrcDirs)
      .flatMap(Collection::stream);

    if (log().debugEnabled()) {
      out = out.peek(newFileLogger(LogResDirsCheck));
    }

    out = out.filter(File::exists);

    if (log().debugEnabled()) {
      out = out.peek(newFileLogger(LogResDirsFound));
    }

    return log().close(out);
  }

  @NotNull
  @Internal
  protected SourceSetContainer getSourceSets() {
    return getJavaPluginExtension().getSourceSets();
  }

  @NotNull
  protected Consumer<File> newFileLogger(@NotNull final String prefix) {
    return log().debugEnabled()
      ? f -> log().debug(prefix, f)
      : f -> {};
  }


  protected interface SourceSearchResult {
    @NotNull
    File file();
    int line();
    boolean found();
  }

  protected static class EmptySearchResult implements SourceSearchResult {
    public static final SourceSearchResult Instance = new EmptySearchResult();

    @Override
    public @NotNull File file() {
      throw new RuntimeException("Cannot get file from empty result.");
    }

    @Override
    public int line() {
      throw new RuntimeException("Cannot get line number from empty result.");
    }

    @Override
    public boolean found() {
      return false;
    }
  }

  protected static class FullSearchResult implements SourceSearchResult {
    @NotNull
    private final File file;
    private final int line;

    public FullSearchResult(@NotNull final File file, final int line) {
      this.file = file;
      this.line = line;
    }

    @Override
    public @NotNull File file() {
      return file;
    }

    @Override
    public int line() {
      return line;
    }

    @Override
    public boolean found() {
      return true;
    }
  }
}
