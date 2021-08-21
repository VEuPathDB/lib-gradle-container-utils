package org.veupathdb.lib.gradle.container.tasks.base;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.util.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Stack;
import java.util.stream.Stream;

/**
 * General Utilities
 *
 * @since 1.1.0
 */
public class Utils {
  private final Logger Log;

  Utils(Logger log) {
    log.constructor(log);
    Log = log;
  }

  /**
   * Deletes the given target file/directory.
   * <p>
   * If the given {@code File} represents a directory, the directory tree
   * will be recursively deleted.
   *
   * @param target File/directory to delete.
   *
   * @since 1.1.0
   */
  public void deleteRecursive(@NotNull final File target) {
    Log.open(target);

    if (!target.isDirectory()) {
      if (!target.delete()) {
        Log.error("Failed to delete file {}", target);
        throw new RuntimeException("Failed to delete file " + target);
      }

      return;
    }

    var dirsDeleted  = 0;
    var filesDeleted = 0;

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
            Log.error("Failed to delete file {}", child);
            throw new RuntimeException("Failed to delete file " + child);
          }

          filesDeleted++;
        }
      }
    }

    while (!dels.empty()) {
      final var del = dels.pop();

      if (!del.delete()) {
        Log.error("Failed to delete directory {}", del);
        throw new RuntimeException("Failed to delete directory " + del);
      }

      dirsDeleted++;
    }

    Log.debug("Deleted {} files and {} directories", filesDeleted, dirsDeleted);

    Log.close();
  }

  /**
   * Ensures the directory represented by the given {@code File} exists and is
   * a directory.
   *
   * @param dir Target directory to check and create if needed.
   *
   * @return The input {@code File}.
   *
   * @since 1.1.0
   */
  public File getOrCreateDir(@NotNull final File dir) {
    Log.open(dir);

    if (!dir.exists()) {
      if (!dir.mkdirs()) {
        Log.error("Failed to create docs directory {}", dir);
        throw new RuntimeException("Failed to create docs directory " + dir);
      }
    } else {
      if (!dir.isDirectory()) {
        Log.error("Path {} exists but is not a directory", dir);
        throw new RuntimeException("Path " + dir + " exists but is not a directory");
      }
    }

    return Log.close(dir);
  }

  /**
   * Moves the given source file to the path represented by the given target
   * {@code File}s.
   *
   * @param src Source file to move.
   * @param tgt Target new location.
   *
   * @since 1.1.0
   */
  public void moveFile(@NotNull final File src, @NotNull final File tgt) {
    Log.open(src, tgt);

    try {
      Files.move(src.toPath(), tgt.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      Log.error("Failed to move file {} to {}", src, tgt);
      throw new RuntimeException("Failed to move file " + src + " to " + tgt, e);
    }

    Log.close();
  }

  /**
   * Moves the given source file to both the paths represented by the given
   * target {@code File}s.
   *
   * @param src  Source file to move.
   * @param tgt1 Target location 1.
   * @param tgt2 Target location 2.
   *
   * @since 1.1.0
   */
  public void moveFile(
    @NotNull final File src,
    @NotNull final File tgt1,
    @NotNull final File tgt2
  ) {
    Log.open(src, tgt1, tgt2);

    try {
      Files.copy(src.toPath(), tgt1.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      Log.fatal(e, "Failed to copy file {} to {}", src, tgt1);
    }

    try {
      Files.move(src.toPath(), tgt2.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      Log.fatal(e, "Failed to move file {} to {}", src, tgt2);
    }

    Log.close();
  }

  /**
   * Moves the files in the given stream to the target parent directory.
   *
   * @param tgtDir   Target directory into which the files in the given stream
   *                 will be moved.
   * @param srcFiles Stream containing 0 or more files to move to the given
   *                 target directory.
   *
   * @since 1.1.0
   */
  public void moveFilesTo(@NotNull final File tgtDir, @NotNull final Stream<File> srcFiles) {
    Log.open(tgtDir, srcFiles);

    if (!tgtDir.exists()) {
      Log.fatal("Cannot move files to dir {}.  Directory does not exist.", tgtDir);
    } else if (!tgtDir.isDirectory()) {
      Log.fatal("Cannot move files to path {}.  Path does not point to a directory.", tgtDir);
    }

    srcFiles.forEach(f -> moveFile(f, new File(tgtDir, f.getName())));

    Log.close();
  }

  /**
   * Reads the contents of the given {@code File} into a string.
   *
   * @param src File to read.
   *
   * @return The contents of the target file as a string.
   *
   * @since 1.1.0
   */
  @NotNull
  public String readFile(@NotNull final File src) {
    Log.open(src);

    try {
      return Log.close(Files.readString(src.toPath()));
    } catch (IOException e) {
      return Log.fatal(e, "Failed to read contents of file {}", src);
    }
  }

  /**
   * Writes the given string contents to the given file, overwriting anything
   * the target file previously contained.
   * <p>
   * If the target file did not previously exist, it will be created.
   *
   * @param tgt     Target file to write to.
   * @param content Contents to write to the target file.
   *
   * @since 1.1.0
   */
  public void overwriteFile(@NotNull final File tgt, @NotNull final String content) {
    Log.open(tgt, "...");

    try {
      Files.writeString(
        tgt.toPath(),
        content,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.CREATE
      );
    } catch (IOException e) {
      Log.fatal(e, "Failed to write contents to file {}", tgt);
    }

    Log.close();
  }

  /**
   * Returns a stream of the direct child files of the given target directory.
   * <p>
   * If the given target directory does not exist, or is not a directory, an
   * empty stream will be returned.
   *
   * @param root Target directory.
   *
   * @return A stream containing the direct children of the given directory.
   *
   * @since 1.1.0
   */
  @NotNull
  public Stream<File> listChildren(@NotNull final File root) {
    Log.open(root);

    if (!root.exists() || !root.isDirectory()) {
      return Stream.empty();
    }

    //noinspection ConstantConditions
    return Log.close(Arrays.stream(root.listFiles()));
  }
}
