package org.veupathdb.lib.gradle.container.tasks;

import org.gradle.api.Task;

import java.util.Arrays;

public class UninstallFgpUtil extends VendorAction {
  public static void init(final Task task) {
    task.setDescription("Uninstall FgpUtil");
    task.setGroup("VEuPathDB");
    task.getActions().add(t -> new UninstallFgpUtil(t).execute());
  }

  public UninstallFgpUtil(final Task task) {
    super(task);
  }

  private void execute() {
    final var vendorDir = getVendorDir();

    if (vendorDir.isEmpty())
      return;

    Arrays.stream(vendorDir.get().listFiles())
      .filter(p -> p.getName().startsWith("fgputil"))
      .forEach(f -> {
        if (!f.delete()) {
          Task.getLogger().error("Failed to delete file " + f);
          throw new RuntimeException("Failed to delete file " + f);
        }
      });
  }
}
