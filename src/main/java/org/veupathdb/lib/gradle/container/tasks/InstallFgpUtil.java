package org.veupathdb.lib.gradle.container.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public class InstallFgpUtil extends DefaultTask {
  public static final String ExtensionName = "FgpUtilVersion";

  private static final String LockFile = "fgputil.lock";
  private static final String URL      = "https://github.com/VEuPathDB/FgpUtil";

  public static void init(Task task) {
    task.getProject().getExtensions().create(ExtensionName, Git.GitExtension.class);
    task.setDescription("Install FgpUtil");
    task.setGroup("VEuPathDB");
  }

  @TaskAction
  public void execute() {
    final var log = getLogger();

    final var vendorDir   = new Vendor(getProject()).getOrCreateVendorDir();
    final var prevVersion = getPreviousVersion(vendorDir);
    final var version     = (Git.GitExtension) getProject().getExtensions().getByName(ExtensionName);

    final File repoDir;

    // If there was no previous version recorded, assume it wasn't ever built.
    if (prevVersion.isEmpty()) {
      log.info("Cloning FgpUtil");

      repoDir = gitClone(vendorDir, version);
    }

    // If the version in the lock file is different than what is set in the
    // build config, then the build config has been update.  Clear out the old
    // version and rebuild.
    else if (!prevVersion.get().equals(version.getVersion())) {
      log.info("FgpUtil version change detected.  Updating from " + prevVersion.get() + " to " + version.getVersion());

      removeOldJars(vendorDir);
      repoDir = gitClone(vendorDir, version);
    }

    // The version in the lock file matches the version currently in the gradle
    // config.  Nothing to do.
    else {
      return;
    }

    // If we made it here, we have freshly cloned the FgpUtil repo and switched
    // it to the target revision/branch/tag.
    assembleJars(vendorDir, repoDir);
    writeLock(vendorDir, version.getVersion());
    cleanup(repoDir);
  }

  private Optional<String> getPreviousVersion(final File vendorDir) {
    final var lock = new File(vendorDir, LockFile);

    if (!lock.exists())
      return Optional.empty();

    if (!lock.isFile()) {
      getLogger().error("Path " + lock + " is not a regular file");
      throw new RuntimeException("Path " + lock + " is not a regular file");
    }

    if (!lock.canRead()) {
      getLogger().error("Cannot read file " + lock);
      throw new RuntimeException("Cannot read file " + lock);
    }

    try {
      return Optional.of(Files.readString(lock.toPath()));
    } catch (IOException e) {
      getLogger().error("Failed to read file " + lock);
      throw new RuntimeException("Failed to read file " + lock, e);
    }
  }

  private void removeOldJars(final File vendorDir) {
    final var log = getLogger();

    log.info("Removing old FgpUtil jar files");

    try {
      final var it = Files.walk(vendorDir.toPath(), 1)
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

  private void assembleJars(final File vendorDir, final File repoDir) {
    final var mvn = new Maven(getProject());
    final var log = getLogger();

    log.info("Building FgpUtil");
    final var jars = mvn.cleanInstall(repoDir);

    try {
      for (final var jar : jars) {
        Files.move(jar.toPath(), vendorDir.toPath());
      }
    } catch (Exception e) {
      log.error("Failed to move one or more jar files to the vendor directory");
      throw new RuntimeException("Failed to move one or more jar files to the vendor directory", e);
    }
  }

  private File gitClone(final File vendorDir, final Git.GitExtension version) {
    final var log  = getLogger();
    final var git  = new Git(getProject());
    final var repo = git.clone(URL, vendorDir);

    if (!version.isDefault()) {
      log.info("Switching FgpUtil to " + version.getVersion());

      git.checkout(repo, version.getVersion());
    }

    return repo;
  }

  private void writeLock(final File vendorDir, final String version) {
    try {
      Files.writeString(new File(vendorDir, LockFile).toPath(), version, StandardOpenOption.TRUNCATE_EXISTING);
    } catch (Exception e) {
      getLogger().error("Failed to write FgpUtil lock file");
      throw new RuntimeException("Failed to write FgpUtil lock file", e);
    }
  }

  private void cleanup(final File repoDir) {
    if (!repoDir.delete()) {
      getLogger().error("Failed to remove path " + repoDir);
      throw new RuntimeException("Failed to remove path " + repoDir);
    }
  }

}
