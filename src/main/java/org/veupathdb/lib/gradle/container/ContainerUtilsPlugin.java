package org.veupathdb.lib.gradle.container;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.logging.TestExceptionFormat;
import org.gradle.api.tasks.testing.logging.TestLogEvent;
import org.jetbrains.annotations.NotNull;
import org.veupathdb.lib.gradle.container.config.Options;
import org.veupathdb.lib.gradle.container.tasks.*;
import org.veupathdb.lib.gradle.container.tasks.docker.DockerBuild;
import org.veupathdb.lib.gradle.container.tasks.fgputil.InstallFgpUtil;
import org.veupathdb.lib.gradle.container.tasks.fgputil.UninstallFgpUtil;
import org.veupathdb.lib.gradle.container.tasks.jaxrs.*;
import org.veupathdb.lib.gradle.container.tasks.raml.GenerateRamlDocs;
import org.veupathdb.lib.gradle.container.util.JarFileFilter;

import java.util.Arrays;
import java.util.Optional;


/**
 * Gradle Container Utils Plugin
 * <p>
 * Root class of the Gradle plugin.  This class is responsible for registering
 * all the tasks and configuration extensions.
 *
 * @since 1.0.0
 */
public class ContainerUtilsPlugin implements Plugin<Project> {
  public static final String ExtensionName = "containerBuild";

  /**
   * Applies the plugin tasks, extensions, and configuration to the Gradle
   * project.
   *
   * @param project Gradle project.
   *
   * @since 1.0.0
   */
  public void apply(final Project project) {
    // Register Global Options
    project.getExtensions().create(ExtensionName, Options.class);

    // Register Tasks
    final var tasks = project.getTasks();

    tasks.create(InstallFgpUtil.TaskName, InstallFgpUtil.class, InstallFgpUtil::init);
    tasks.create(UninstallFgpUtil.TaskName, UninstallFgpUtil.class, UninstallFgpUtil::init);

    tasks.create(InstallRaml4JaxRS.TaskName, InstallRaml4JaxRS.class, InstallRaml4JaxRS::init);
    tasks.create(UninstallRaml4JaxRS.TaskName, UninstallRaml4JaxRS.class, UninstallRaml4JaxRS::init);

    tasks.create(GenerateJaxRS.TaskName, GenerateJaxRS.class, GenerateJaxRS::init);
    tasks.create(GenerateRamlDocs.TaskName, GenerateRamlDocs.class, GenerateRamlDocs::init);

    tasks.create(JaxRSDiscriminatorPatch.TaskName, JaxRSDiscriminatorPatch.class, JaxRSDiscriminatorPatch::init);
    tasks.create(JaxRSEnumValuePatch.TaskName, JaxRSEnumValuePatch.class, JaxRSEnumValuePatch::init);

    tasks.create(DockerBuild.TaskName, DockerBuild.class, DockerBuild::init);

    tasks.create(PrintPackage.TaskName, PrintPackage.class, PrintPackage::init);

    project.afterEvaluate(ContainerUtilsPlugin::afterEvaluate);
  }

  private static void afterEvaluate(final Project project) {
    project.getLogger().trace("afterEvaluate(Project)");

    final var opts = (Options) project.getExtensions().getByName(ExtensionName);

    project.getLogger().debug("Options: {}", opts);

    setProjectProps(project, opts);
    setJarProps(project, opts);
    configureTestLogging(project);
    configureRepositories(project, opts);

    final var tasks = project.getTasks();

    // Make sure InstallFgpUtil is called before any compile tasks.
    tasks.withType(JavaCompile.class)
      .forEach(t -> t.dependsOn(tasks.getByName(InstallFgpUtil.TaskName)));

    // Make sure that Raml for Jax RS is installed before attempting to generate
    // java types.

    final var genJaxrs = tasks.getByName(GenerateJaxRS.TaskName);

    genJaxrs.dependsOn(tasks.getByName(InstallRaml4JaxRS.TaskName));

    genJaxrs.finalizedBy(
      JaxRSDiscriminatorPatch.TaskName,
      JaxRSEnumValuePatch.TaskName
    );
  }

  private static void setProjectProps(@NotNull Project project, @NotNull Options opts) {
    project.setVersion(opts.getProjectConfig().getVersion());
    project.setGroup(opts.getProjectConfig().getGroup());
  }

  private static void setJarProps(@NotNull Project project, @NotNull Options opts) {
    project.getLogger().debug("Configuring jar tasks.");
    final var conf = opts.getProjectConfig();

    for (final var jar : project.getTasks().withType(Jar.class)) {
      jar.getLogger().debug("Applying config to jar task {}.", jar);

      // Set DuplicateStrategy
      jar.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);

      // Set Archive Name
      jar.getArchiveFileName().set("service.jar");

      // Apply changes needed to build a fat jar
      configureFatJar(project, jar);

      // Set Manifest Attributes
      final var attrs = jar.getManifest().getAttributes();

      attrs.put("Main-Class", conf.getProjectPackage() + "." + conf.getMainClassName());
      attrs.put("Implementation-Title", conf.getName());
      attrs.put("Implementation-Version", conf.getVersion());
    }
  }

  private static void configureFatJar(
    @NotNull Project project,
    @NotNull Jar jar
  ) {
    jar.from(project.getConfigurations()
      .getByName("runtimeClasspath")
      .getFiles()
      .stream()
      .flatMap(JarFileFilter.expandFiles(project))
      .toArray());
  }

  private static final TestLogEvent[] Events = {
    TestLogEvent.PASSED,
    TestLogEvent.FAILED,
    TestLogEvent.SKIPPED,
    TestLogEvent.STANDARD_OUT,
    TestLogEvent.STANDARD_ERROR,
  };

  private static void configureTestLogging(@NotNull Project project) {
    final var events = Arrays.asList(Events);

    project.getTasks().withType(Test.class).forEach(test -> {
      test.testLogging(tlc -> {
        tlc.getEvents().addAll(events);
        tlc.setExceptionFormat(TestExceptionFormat.FULL);
        tlc.setShowExceptions(true);
        tlc.setShowCauses(true);
        tlc.setShowStackTraces(true);
        tlc.setShowStandardStreams(true);
      });
      test.setEnableAssertions(true);
      test.setIgnoreFailures(true);
      test.useJUnitPlatform();
    });
  }

  private static void configureRepositories(@NotNull Project project, @NotNull Options opts) {
    project.getRepositories().mavenCentral();
    project.getRepositories().maven(mar -> {
      mar.setName("GitHubPackages");
      mar.setUrl(project.uri("https://maven.pkg.github.com/veupathdb/maven-packages"));

      var creds = mar.getCredentials();

      creds.setUsername(Optional.ofNullable((String) project.findProperty("gpr.user"))
        .orElseGet(() -> System.getenv("GITHUB_USERNAME")));
      creds.setPassword(Optional.ofNullable((String) project.findProperty("gpr.key"))
        .orElseGet(() -> System.getenv("GITHUB_TOKEN")));
    });
  }
}
