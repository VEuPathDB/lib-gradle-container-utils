package org.veupathdb.lib.gradle.container.tasks;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.tasks.base.ExecAction;

import java.io.File;
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
    Log.open(args);

    args.addAll(Arrays.asList("api.raml", "--theme", "raml2html-modern-theme"));

    Log.close();
  }

  @Override
  @NotNull
  protected File getStdOutRedirect() {
    return Log.getter(new File(RootDir, OutputFile));
  }

  @Override
  protected void postExec() {
    Log.open();

    Log.debug("Copying generated docs to target doc directories");

    Util.moveFile(
      getStdOutRedirect(),
      new File(Util.getOrCreateDir(new File(RootDir, Options.getRepoDocsDirectory())), OutputFile),
      new File(Util.getOrCreateDir(new File(RootDir, SrcDocsDir)), OutputFile)
    );

    Log.close();
  }
}
