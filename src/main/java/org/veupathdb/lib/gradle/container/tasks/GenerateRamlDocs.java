package org.veupathdb.lib.gradle.container.tasks;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.config.RedirectConfig;
import org.veupathdb.lib.gradle.container.tasks.base.ExecAction;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GenerateRamlDocs extends ExecAction {

  public static final String TaskName = "generate-raml-docs";

  private static final String SrcDocsDir = "src/main/resources";
  private static final File OutputFile = new File("api.html");

  @Override
  @NotNull
  protected File getWorkDirectory() {
    return RootDir;
  }

  @Override
  @NotNull
  public String pluginDescription() {
    return "Generates HTML documentation from the RAML API spec.";
  }

  @Override
  @NotNull
  protected String getCommandName() {
    return "raml2html";
  }

  @Override
  protected void appendArguments(@NotNull final List<String> args) {
    log().open(args);

    args.addAll(Arrays.asList("api.raml", "--theme", "raml2html-modern-theme"));

    log().close();
  }

  @Override
  @NotNull
  protected Optional<RedirectConfig> getStdOutRedirect() {
    return log().getter(Optional.of(RedirectConfig.toFile(OutputFile)));
  }

  @Override
  protected void postExec() {
    log().open();

    log().debug("Copying generated docs to target doc directories");

    util().moveFile(
      OutputFile,
      new File(util().getOrCreateDir(new File(RootDir, getOptions().getRepoDocsDirectory())), OutputFile.getName()),
      new File(util().getOrCreateDir(new File(RootDir, SrcDocsDir)), OutputFile.getName())
    );

    log().close();
  }
}
