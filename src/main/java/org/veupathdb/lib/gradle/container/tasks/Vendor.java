package org.veupathdb.lib.gradle.container.tasks;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.veupathdb.lib.gradle.container.ContainerUtilsPlugin;

import java.io.File;

public class Vendor {
  public static final String DefaultVendorDir = "vendor";

  private final Logger Log;

  private final ContainerUtilsPlugin.Options Opts;

  private final File Root;

  public Vendor(final Project project) {
    Log  = project.getLogger();
    Opts = (ContainerUtilsPlugin.Options) project.getExtensions().getByName(ContainerUtilsPlugin.ExtensionName);
    Root = project.getRootDir();
  }

  File getOrCreateVendorDir() {
    final var dir = new File(Root, Opts.getVendorDirectory());

    if (!dir.exists()) {
      if (!dir.mkdir()) {
        Log.error("Failed to create vendor directory");
        throw new RuntimeException("Failed to create vendor directory");
      }
    } else {
      if (!dir.isDirectory()) {
        Log.error("Path " + Opts.getVendorDirectory() + " exists but is not a directory");
        throw new RuntimeException("Path " + Opts.getVendorDirectory() + " exists but is not a directory");
      }
    }

    return dir;
  }
}
