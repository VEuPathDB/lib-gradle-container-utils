package org.veupathdb.lib.gradle.container.tasks;

import org.jetbrains.annotations.NotNull;
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
  @NotNull
  protected File getWorkDirectory() {
    return RootDir;
  }

  @Override
  @NotNull
  protected String pluginDescription() {
    return "Generates HTML documentation from the RAML API spec.";
  }

  @Override
  @NotNull
  protected String getCommandName() {
    return "raml2html";
  }

  @Override
  protected void appendArguments(@NotNull final List<String> args) {
    args.addAll(Arrays.asList(
      "api.raml",
      "--theme", "raml2html-modern-theme"
    ));
  }

  @Override
  @NotNull
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

  @NotNull
  private File getOrCreateRepoDocsDir() {
    final var out = new File(RootDir, Options.getRepoDocsDirectory());

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

  @NotNull
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
