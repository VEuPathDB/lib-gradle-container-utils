package org.veupathdb.lib.gradle.container.tasks.base;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.config.ServiceProperties;
import org.veupathdb.lib.gradle.container.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Properties;
import java.util.Stack;

public class Utils {
  private static final String PropFileName = "service.properties";

  private final Logger Log;

  Utils(Logger log) {
    Log = log;
  }

  @NotNull
  public ServiceProperties loadServiceProperties(@NotNull final File projectRoot) {
    Log.trace("Util#loadServiceProperties(%s)", projectRoot);

    final var props = new Properties();

    try (final var stream = new FileInputStream(new File(projectRoot, PropFileName))) {
      props.load(stream);
    } catch (IOException e) {
      Log.error("Failed to read project's %s file.", PropFileName);
      throw new RuntimeException("Failed to read project's prop file.", e);
    }

    return new ServiceProperties(props);
  }

  public void deleteRecursive(@NotNull final File target) {
    Log.trace("Util#deleteRecursive(%s)", target);

    if (!target.isDirectory()) {
      if (!target.delete()) {
        Log.error("Failed to delete file " + target);
        throw new RuntimeException("Failed to delete file " + target);
      }

      return;
    }

    final var dirs = new Stack<File>();
    final var dels = new Stack<File>();

    dirs.push(target);
    dels.push(target);

    while (!dirs.empty()) {
      //noinspection ConstantConditions
      for (final var child : dirs.pop().listFiles()) {
        if (child.isDirectory()) {
          dirs.push(child);
          dels.push(child);
        } else {
          if (!child.delete()) {
            Log.error("Failed to delete file " + child);
            throw new RuntimeException("Failed to delete file " + child);
          }
        }
      }
    }

    while (!dels.empty()) {
      final var del = dels.pop();

      if (!del.delete()) {
        Log.error("Failed to delete directory " + del);
        throw new RuntimeException("Failed to delete directory " + del);
      }
    }
  }

  public File getOrCreateDir(@NotNull final File dir) {
    Log.trace("Utils#getOrCreateDir(%s)", dir);

    if (!dir.exists()) {
      if (!dir.mkdirs()) {
        Log.error("Failed to create docs directory " + dir);
        throw new RuntimeException("Failed to create docs directory " + dir);
      }
    } else {
      if (!dir.isDirectory()) {
        Log.error("Path " + dir + " exists but is not a directory");
        throw new RuntimeException("Path " + dir + " exists but is not a directory");
      }
    }

    return dir;
  }

  public void moveFile(@NotNull final File src, @NotNull final File tgt) {
    Log.trace("Utils#moveFile(src = %s, tgt = %s)", src, tgt);

    try {
      Files.move(src.toPath(), tgt.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      Log.error("Failed to move file " + src + " to " + tgt);
      throw new RuntimeException("Failed to move file " + src + " to " + tgt, e);
    }
  }

  public void moveFile(
    @NotNull final File src,
    @NotNull final File tgt1,
    @NotNull final File tgt2
  ) {
    Log.trace("Utils#moveFile(src = %s, tgt1 = %s, tgt2 = %s)", src, tgt1, tgt2);

    try {
      Files.copy(src.toPath(), tgt1.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      Log.error("Failed to copy file " + src + " to " + tgt1);
      throw new RuntimeException("Failed to copy file " + src + " to " + tgt1, e);
    }

    try {
      Files.move(src.toPath(), tgt2.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      Log.error("Failed to move file " + src + " to " + tgt2);
      throw new RuntimeException("Failed to move file " + src + " to " + tgt2, e);
    }
  }

  public void moveFilesTo(@NotNull final File tgtDir, @NotNull final File... srcFiles) {
    Log.trace("Utils#moveFilesTo(tgtDir = %s, srcFiles = %s)", () -> tgtDir, () -> Arrays.toString(srcFiles));

    if (!tgtDir.exists()) {
      Log.error("Cannot move files to dir " + tgtDir + ".  Directory does not exist.");
      throw new RuntimeException("Cannot move files to dir " + tgtDir + ".  Directory does not exist.");
    } else if (!tgtDir.isDirectory()) {
      Log.error("Cannot move files to path " + tgtDir + ".  Path does not point to a directory.");
      throw new RuntimeException("Cannot move files to path " + tgtDir + ".  Path does not point to a directory.");
    }

    final var path = tgtDir.toPath();

    File src = null;
    Path tgt = null;

    try {
      //noinspection ForLoopReplaceableByForEach
      for (int i = 0; i < srcFiles.length; i++) {
        src = srcFiles[i];
        tgt = path.resolve(src.getName());

        Files.move(src.toPath(), tgt, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException e) {
      Log.error("Failed to move file " + src + " to " + tgt);
      throw new RuntimeException("Failed to move file " + src + " to " + tgt, e);
    }
  }

  @NotNull
  public String readFile(@NotNull final File src) {
    Log.trace("Utils#readFile(src = %s)", src);

    try {
      return Files.readString(src.toPath());
    } catch (IOException e) {
      Log.error("Failed to read contents of file " + src);
      throw new RuntimeException("Failed to read contents of file " + src, e);
    }
  }

  public void overwriteFile(@NotNull final File tgt, @NotNull final String content) {
    Log.trace("Utils#overwriteFile(tgt = %s, content = ...)", tgt);

    try {
      Files.writeString(
        tgt.toPath(),
        content,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.CREATE
      );
    } catch (IOException e) {
      Log.error("Failed to write contents to file " + tgt);
      throw new RuntimeException("Failed to write contents to file " + tgt, e);
    }

  }
}
