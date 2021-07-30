package org.veupathdb.lib.gradle.container.tasks;

import java.io.File;

class VendorLib {
  static final String VendorDirPath = "vendor";

  static File createVendorDirectory(final File rootDir) {
    final var dir = new File(rootDir, VendorDirPath);

    if (!dir.exists()) {
      if (!dir.mkdir()) {
        throw new RuntimeException("Failed to create vendor directory");
      }
    } else {
      if (!dir.isDirectory()) {
        throw new RuntimeException("Path " + VendorDirPath + " exists but is not a directory");
      }
    }

    return dir;
  }
}
