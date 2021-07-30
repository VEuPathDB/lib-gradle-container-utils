package org.veupathdb.lib.gradle.container.tasks;

import org.gradle.api.Task;

public class FgpUtil extends VendorLib {
  public static final String ExtensionName = "FgpUtilVersion";

  private static final String FgpUtilURL = "https://github.com/VEuPathDB/FgpUtil";

  public static void execute(Task task) {
    final var repoDir = Git.clone(FgpUtilURL, createVendorDirectory(task.getProject().getRootDir()));
    final var version = (Git.GitExtension) task.getProject().getExtensions().getByName(ExtensionName);

    if (!version.isDefault()) {
      Git.checkout(repoDir, version.getVersion());
    }
  }
}
