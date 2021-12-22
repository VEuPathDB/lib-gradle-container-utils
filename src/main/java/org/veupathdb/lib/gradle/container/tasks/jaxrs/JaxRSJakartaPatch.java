package org.veupathdb.lib.gradle.container.tasks.jaxrs;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.tasks.base.JaxRSSourceAction;

import java.io.*;
import java.util.Arrays;

public class JaxRSJakartaPatch extends JaxRSSourceAction {
  public static final String TaskName = "patch-jakarta";

  @Override
  @SuppressWarnings("ConstantConditions")
  public void execute() {
    log().open();

    log().debug("Patching enum files.");
    getGeneratedSourceDirectories()
      .filter(f -> f.getName().contains("resources"))
      .map(File::listFiles)
      .flatMap(Arrays::stream)
      .filter(this::onlyRelevant)
      .forEach(this::patch);

    log().close();
  }

  @Override
  public @NotNull String pluginDescription() {
    return "Corrects javax.ws imports to jakarta.ws.";
  }

  private void patch(@NotNull final File file) {
    log().open(file);

    final var tmpFile = makeTmpFile(file);

    patchContents(file, tmpFile);

    file.delete();
    tmpFile.renameTo(file);

    log().close();
  }

  private boolean onlyRelevant(@NotNull final File test) {
    log().open(test);

    try (final var reader = new BufferedReader(new FileReader(test))) {
      String line;

      while ((line= reader.readLine()) != null) {
        if (line.startsWith("import javax.ws")) {
          return log().close(true);
        }

        if (line.contains(" interface ")) {
          return log().close(false);
        }
      }

    } catch (IOException e) {
      log().fatal(e);
    }

    return log().close(false);
  }

  private void patchContents(@NotNull final File from, @NotNull final File to) {
    log().open(from, to);

    try (
      final var reader = new BufferedReader(new FileReader(from));
      final var writer = new BufferedWriter(new FileWriter(to))
    ) {
      String line;

      while ((line = reader.readLine()) != null) {

        if (line.startsWith("import javax.ws")) {
          line = line.replace("javax.ws", "jakarta.ws");
        }

        writer.write(line);
      }

      writer.flush();
    } catch (IOException e) {
      log().fatal(e);
    }

    log().close();
  }

  private @NotNull File makeTmpFile(@NotNull final File mirror) {
    log().close(mirror);

    final var newFile = new File(mirror.getParentFile(), "_" + mirror.getName());

    try {
      newFile.createNewFile();
    } catch (IOException e) {
      return log().fatal(e);
    }

    return log().close(newFile);
  }
}
