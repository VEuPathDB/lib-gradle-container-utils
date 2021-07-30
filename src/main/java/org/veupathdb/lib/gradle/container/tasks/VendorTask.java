package org.veupathdb.lib.gradle.container.tasks;

import org.gradle.api.tasks.Internal;

import java.io.File;
import java.util.Optional;

public class VendorTask extends ExtTask {
  public static final String DefaultVendorDir = "vendor";

  @Internal
  private File vendorDir;

  private boolean checked;

  protected void createVendorDir() {
    if (checked && vendorDir != null)
      return;

    final var dir = new File(RootDir, getOptions().getVendorDirectory());

    if (!dir.exists()) {
      if (!dir.mkdir()) {
        Log.error("Failed to create vendor directory");
        throw new RuntimeException("Failed to create vendor directory");
      }
    } else {
      if (!dir.isDirectory()) {
        Log.error("Path " + dir + " exists but is not a directory");
        throw new RuntimeException("Path " + dir + " exists but is not a directory");
      }
    }

    checked = true;
    vendorDir = dir;
  }

  protected Optional<File> getVendorDir() {
    if (checked)
      return Optional.ofNullable(vendorDir);

    final var dir = new File(RootDir, getOptions().getVendorDirectory());
    checked = true;
    return dir.exists() ? Optional.of(vendorDir = dir) : Optional.empty();
  }

  protected File vendorDir() {
    return vendorDir;
  }
}
