package org.veupathdb.lib.gradle.container.tasks;

import org.veupathdb.lib.gradle.container.tasks.base.Defaults;
import org.veupathdb.lib.gradle.container.tasks.base.ExecAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

public class GenerateRamlDocs extends ExecAction {

  public static final String TaskName = "generate-raml-docs";

  private static final String SrcDocsDir = "src/main/resources";
  private static final String OutputFile = "api.html";

  @Override
  protected File getWorkDirectory() {
    return RootDir;
  }

  @Override
  protected String pluginDescription() {
    return "Generates HTML documentation from the RAML API spec.";
  }

  @Override
  protected String getCommandName() {
    return "raml2html";
  }

  @Override
  protected void appendArguments(final List<String> args) {
    args.addAll(Arrays.asList(
      "api.raml",
      "--theme", "raml2html-modern-theme"
    ));
  }

  @Override
  protected File getStdOutRedirect() {
    return new File(RootDir, OutputFile);
  }

  @Override
  protected void postExec() {
    Log.trace("GenerateRamlDocs#postExec()");

    final var src = getStdOutRedirect();

    try {
      Log.debug("Copying api docs to repo doc directory");
      Files.copy(
        src.toPath(),
        getOrCreateRepoDocsDir().toPath().resolve(OutputFile),
        StandardCopyOption.REPLACE_EXISTING
      );

      Log.debug("Copying api docs to resources directory");
      Files.move(
        src.toPath(),
        getOrCreateSrcDocsDir().toPath().resolve(OutputFile),
        StandardCopyOption.REPLACE_EXISTING
      );
    } catch (IOException e) {
      Log.error("Failed to copy/move generated API docs to target locations.");
      throw new RuntimeException("Failed to copy/move generated API docs to target locations.", e);
    }
  }

  private File getOrCreateRepoDocsDir() {
    final String tmp;
    final var out = new File(
      RootDir,
      (tmp = getOptions().getRepoDocsDirectory()) == null
        ? Defaults.DefaultDocsDirectory
        : tmp
    );

    if (!out.exists()) {
      if (!out.mkdirs()) {
        throw new RuntimeException("Failed to create docs directory " + out);
      }
    } else {
      if (!out.isDirectory()) {
        throw new RuntimeException("Path " + out + " exists but is not a directory");
      }
    }

    return out;
  }

  private File getOrCreateSrcDocsDir() {
    final var out = new File(RootDir, SrcDocsDir);

    if (!out.exists()) {
      if (!out.mkdirs()) {
        throw new RuntimeException("Failed to create docs directory " + out);
      }
    } else {
      if (!out.isDirectory()) {
        throw new RuntimeException("Path " + out + " exists but is not a directory");
      }
    }

    return out;
  }
}
