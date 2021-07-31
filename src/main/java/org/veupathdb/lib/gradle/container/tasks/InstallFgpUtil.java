package org.veupathdb.lib.gradle.container.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;

public class InstallFgpUtil extends VendorAction {
  private static final String LockFile = "fgputil.lock";
  private static final String URL      = "https://github.com/VEuPathDB/FgpUtil";

  private static final byte StateNew    = 1;
  private static final byte StateUpdate = 2;
  private static final byte StateSkip   = 127;

  public static void init(final InstallFgpUtil task) {
    task.setDescription("Install FgpUtil");
    task.setGroup("VEuPathDB");
    task.getActions().add(t -> task.execute());
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  private Git.Target fgpUtilVersion = Git.Target.Default;

  @SuppressWarnings("unused")
  public Git.Target getFgpUtilVersion() {
    return fgpUtilVersion;
  }

  @SuppressWarnings("unused")
  public void setFgpUtilVersion(final String fgpUtilVersion) {
    this.fgpUtilVersion = Git.Target.of(fgpUtilVersion);
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  private void execute() {
    final var log = getLogger();

    final File repoDir;

    byte state = calcState();
    switch (state) {
      case StateNew -> {
        log.info("Cloning FgpUtil");
        createVendorDir();
        repoDir = gitClone();
      }
      case StateUpdate -> {
        log.info("Configured FgpUtil version changed.  Rebuilding");
        removeOldJars();
        repoDir = gitClone();
      }
      case StateSkip -> {
        return;
      }
      default -> {
        log.error("Unknown state " + state);
        throw new IllegalStateException("Unknown state " + state);
      }
    }

    // If we made it here, we have freshly cloned the FgpUtil repo and switched
    // it to the target revision/branch/tag.
    assembleJars(repoDir);
    writeLock();
    cleanup(repoDir);
  }

  private void removeOldJars() {
    final var log = getLogger();

    log.info("Removing old FgpUtil jar files");

    try {
      final var it = Files.walk(vendorDir().toPath(), 1)
        .filter(path -> path.getFileName().startsWith("fgputil-"))
        .iterator();

      while (it.hasNext()) {
        final var next = it.next().toFile();

        log.debug("Attempting to remove file " + next);

        if (!it.next().toFile().delete()) {
          log.error("Failed to delete file " + next);
          throw new RuntimeException("Failed to delete file " + next);
        }
      }
    } catch (IOException e) {
      log.error("Failed to cleanup old FgpUtil jar files");
      throw new RuntimeException("Failed to cleanup old FgpUtil jar files", e);
    }
  }

  private void assembleJars(final File repoDir) {
    final var mvn = new Maven(getProject());
    final var log = getLogger();

    log.info("Building FgpUtil");
    final var jars = mvn.cleanInstall(repoDir);

    try {
      for (final var jar : jars) {
        Files.move(
          jar.toPath(),
          vendorDir().toPath().resolve(jar.getName()),
          StandardCopyOption.REPLACE_EXISTING
        );
      }
    } catch (Exception e) {
      log.error("Failed to move one or more jar files to the vendor directory");
      throw new RuntimeException("Failed to move one or more jar files to the vendor directory", e);
    }
  }

  private File gitClone() {
    final var log  = getLogger();
    final var git  = new Git(getProject());
    final var repo = git.clone(URL, vendorDir());

    if (!fgpUtilVersion.isDefault()) {
      log.info("Switching FgpUtil to " + fgpUtilVersion.name());

      git.checkout(repo, fgpUtilVersion);
    }

    return repo;
  }

  private void writeLock() {
    try {
      Files.writeString(
        new File(vendorDir(), LockFile).toPath(),
        fgpUtilVersion.name(),
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.CREATE
      );
    } catch (Exception e) {
      getLogger().error("Failed to write FgpUtil lock file");
      throw new RuntimeException("Failed to write FgpUtil lock file", e);
    }
  }

  private void cleanup(final File repoDir) {
    try {
      Files.walk(repoDir.toPath())
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(f -> {
          if (!f.delete()) {
            getLogger().error("Failed to remove path " + f);
            throw new RuntimeException("Failed to remove path " + f);
          }
        });
    } catch (IOException e) {
      getLogger().error("Failed to walk path " + repoDir);
      throw new RuntimeException("Failed to walk path " + repoDir, e);
    }
  }

  private byte calcState() {
    final var optDir = getVendorDir();

    if (optDir.isEmpty())
      return StateNew;

    final var lock = new File(optDir.get(), LockFile);

    if (!lock.exists())
      return StateNew;

    if (!lock.isFile()) {
      getLogger().error("Path " + lock + " is not a regular file");
      throw new RuntimeException("Path " + lock + " is not a regular file");
    }

    if (!lock.canRead()) {
      getLogger().error("Cannot read file " + lock);
      throw new RuntimeException("Cannot read file " + lock);
    }

    try {
      return Git.Target.of(Files.readString(lock.toPath())).is(fgpUtilVersion)
        ? StateSkip
        : StateUpdate;
    } catch (IOException e) {
      getLogger().error("Failed to read file " + lock);
      throw new RuntimeException("Failed to read file " + lock, e);
    }
  }
}
