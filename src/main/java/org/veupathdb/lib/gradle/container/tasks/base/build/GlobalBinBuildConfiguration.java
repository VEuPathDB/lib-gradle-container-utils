package org.veupathdb.lib.gradle.container.tasks.base.build;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GlobalBinBuildConfiguration extends GlobalBuildConfiguration {
  @NotNull
  private String binDirectory = ".bin";

  @NotNull
  public String getBinDirectory() {
    return binDirectory;
  }

  public void setBinDirectory(@NotNull String binDirectory) {
    this.binDirectory = Objects.requireNonNull(binDirectory);
  }

  @Override
  public String toString() {
    return "GlobalBinBuildConfiguration{" +
      "binDirectory='" + binDirectory + '\'' +
      '}';
  }
}
