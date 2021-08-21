package org.veupathdb.lib.gradle.container.tasks.base.build;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GlobalVendorBuildConfiguration extends GlobalBuildConfiguration {
  @NotNull
  private String vendorDirectory = "vendor";

  public @NotNull String getVendorDirectory() {
    return vendorDirectory;
  }

  public void setVendorDirectory(@NotNull String vendorDirectory) {
    this.vendorDirectory = Objects.requireNonNull(vendorDirectory);
  }

  @Override
  public String toString() {
    return "GlobalVendorBuildConfiguration{" +
      "vendorDirectory='" + vendorDirectory + '\'' +
      '}';
  }
}
