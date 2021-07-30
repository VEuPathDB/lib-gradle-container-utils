package org.veupathdb.lib.gradle.container.tasks;

import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;

import java.util.Arrays;

public class UninstallFgpUtil extends VendorTask {
  public static void init(final Task task) {
    task.setDescription("Uninstall FgpUtil");
    task.setGroup("VEuPathDB");
  }

  @TaskAction
  public void execute() {
    final var vendorDir = getVendorDir();

    if (vendorDir.isEmpty())
      return;

    Arrays.stream(vendorDir.get().listFiles())
      .filter(p -> p.getName().startsWith("fgputil"))
      .forEach(f -> {
        if (!f.delete()) {
          getLogger().error("Failed to delete file " + f);
          throw new RuntimeException("Failed to delete file " + f);
        }
      });
  }
}
