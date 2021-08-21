package org.veupathdb.lib.gradle.container.tasks.raml;

import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.config.RedirectConfig;
import org.veupathdb.lib.gradle.container.tasks.base.exec.ExecAction;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Generate RAML Documentation
 * <p>
 * Generates HTML API documentation from the project's root API RAML file.
 *
 * @since 1.1.0
 */
public class GenerateRamlDocs extends ExecAction {

  public static final String TaskName = "generate-raml-docs";

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
    return log().getter(Optional.of(RedirectConfig.toFile(execConfiguration().getApiDocFileName())));
  }

  @Override
  protected void postExec() {
    log().open();

    log().debug("Copying generated docs to target doc directories");

    final var conf = execConfiguration();

    util().moveFile(
      new File(conf.getApiDocFileName()),
      new File(util().getOrCreateDir(new File(RootDir, conf.getRepoDocsDir())), conf.getApiDocFileName()),
      new File(util().getOrCreateDir(new File(RootDir, conf.getResourceDocsDir())), conf.getApiDocFileName())
    );

    log().close();
  }

  @Override
  protected @NotNull GenRamlConfig execConfiguration() {
    return getOptions().getGenerateRamlDocsConfig();
  }
}
