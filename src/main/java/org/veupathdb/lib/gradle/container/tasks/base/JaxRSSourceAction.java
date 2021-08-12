package org.veupathdb.lib.gradle.container.tasks.base;

import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.stream.Stream;

public abstract class JaxRSSourceAction extends SourceAction {
  private static final String GeneratedDir = "generated";
  private static final String ModelDir = GeneratedDir + "/model";

  @NotNull
  @Internal
  protected Stream<File> getGeneratedSourceDirectories() {
    log().open();
    return log().close(getProjectSourceDirectories()
      .map(f -> new File(f, GeneratedDir))
      .filter(File::exists));
  }

  @NotNull
  @Internal
  protected Stream<File> getGeneratedModelDirectories() {
    log().open();
    return log().close(getProjectSourceDirectories()
      .map(file -> new File(file, ModelDir)))
      .filter(File::exists);
  }

}
