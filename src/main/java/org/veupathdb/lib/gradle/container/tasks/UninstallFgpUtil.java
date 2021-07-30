package org.veupathdb.lib.gradle.container.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;
import org.veupathdb.lib.gradle.container.ContainerUtilsPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UninstallFgpUtil extends DefaultTask {
  public static void init(final Task task) {
    task.setDescription("Uninstall FgpUtil");
    task.setGroup("VEuPathDB");
  }

  @TaskAction
  public void execute() {
    final var opts = (ContainerUtilsPlugin.Options) getProject()
      .getExtensions()
      .getByName(ContainerUtilsPlugin.ExtensionName);

    try {
      Files.walk(new File(getProject().getRootDir(), opts.getVendorDirectory()).toPath(), 1)
        .filter(p -> p.getFileName().startsWith("fgputil"))
        .map(Path::toFile)
        .forEach(f -> {
          if (!f.delete()) {
            getLogger().error("Failed to delete file " + f);
            throw new RuntimeException("Failed to delete file " + f);
          }
        });
    } catch (IOException e) {
      getLogger().error("Failed to walk vendor directory");
      throw new RuntimeException("Failed to walk vendor directory");
    }
  }
}
