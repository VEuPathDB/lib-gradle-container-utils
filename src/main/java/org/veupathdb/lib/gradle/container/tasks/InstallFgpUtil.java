package org.veupathdb.lib.gradle.container.tasks;

import org.gradle.api.Task;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;

public class InstallFgpUtil extends Vendor {
  public static final String ExtensionName = "FgpUtilVersion";

  private static final String LockFile = "fgputil.lock";
  private static final String URL      = "https://github.com/VEuPathDB/FgpUtil";

  private static final byte StateNew    = 1;
  private static final byte StateUpdate = 2;
  private static final byte StateSkip   = 127;

  private byte state;

  public static void init(final Task task) {
    task.getProject().getExtensions().create(ExtensionName, Git.Extension.class);
    task.setDescription("Install FgpUtil");
    task.setGroup("VEuPathDB");
    task.onlyIf(element -> {
      final var t = (InstallFgpUtil) element;
      return (t.state = t.calcState()) != StateSkip;
    });
  }

  @TaskAction
  public void execute() {
    final var log = getLogger();

    final File repoDir;

    createVendorDir();

    switch (state) {
      case StateNew -> {
        log.info("Cloning FgpUtil");
        repoDir = gitClone();
      }
      case StateUpdate -> {
        log.info("Configured FgpUtil version changed.  Rebuilding");
        removeOldJars();
        repoDir = gitClone();
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
    final var vers = getConfigVersion();

    if (!vers.isDefault()) {
      log.info("Switching FgpUtil to " + vers.name());

      git.checkout(repo, vers);
    }

    return repo;
  }

  private void writeLock() {
    try {
      Files.writeString(
        new File(vendorDir(), LockFile).toPath(),
        getConfigVersion().name(),
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
      return Git.Target.of(Files.readString(lock.toPath())).is(getConfigVersion())
        ? StateSkip
        : StateUpdate;
    } catch (IOException e) {
      getLogger().error("Failed to read file " + lock);
      throw new RuntimeException("Failed to read file " + lock, e);
    }
  }

  private Git.Target getConfigVersion() {
    return getExtension().getVersion();
  }

  private Git.Extension extension;
  private Git.Extension getExtension() {
    return extension == null
      ? extension = (Git.Extension) getExtensions().findByName(ExtensionName)
      : extension;
  }
}
