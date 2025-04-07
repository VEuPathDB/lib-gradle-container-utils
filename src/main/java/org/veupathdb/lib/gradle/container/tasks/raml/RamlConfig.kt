package org.veupathdb.lib.gradle.container.tasks.raml

import org.gradle.api.Project
import org.veupathdb.lib.gradle.container.tasks.base.exec.ExecConfiguration
import org.veupathdb.lib.gradle.container.tasks.jaxrs.GenerateJaxRS
import org.veupathdb.lib.gradle.container.tasks.jaxrs.InstallRaml4JaxRS
import java.io.File
import java.net.URI

class RamlConfig(project: Project): ExecConfiguration() {
  companion object {
    const val DefaultResourceDocsDir = "src/main/resources"
    const val DefaultAPIDocFile = "docs/${GenerateRamlDocs.DefaultDocFileName}"
    const val DefaultSchemaRootDir = "schema"
    const val DefaultMergedRamlFile = "$DefaultSchemaRootDir/${ExecMergeRaml.DefaultOutputLibraryName}"
    const val DefaultRamlMergeToolVersion = "v2.0.3"
    inline val DefaultRootRamlFile
      get() = GenerateJaxRS.DefaultRootRamlApiDefinitionFile
    inline val DefaultRaml4JaxRSDownloadURL
      get() = InstallRaml4JaxRS.DefaultRaml4JaxRSDownloadUrl
  }

  /**
   * Directory where the generated API doc resource file(s) will be placed.
   *
   * This path should be in a location that is included as additional resources
   * in the final jar build.
   *
   * **WARNING**: This file is required by https://github.com/VEuPathDB/lib-jaxrs-container-core
   * to display in the automatic API doc endpoint.
   *
   * This field may be set using Gradle's `file` function to resolve the path
   * relative to the (sub)project root.
   * ```
   * sourceDocsDir = file("src/main/resources")
   * ```
   *
   * If the target path is in the root project and not the current subproject,
   * the field may be set using:
   * ```
   * sourceDocsDir = rootProject.file("src/main/resources")
   * ```
   *
   * For intermediary projects, the project will need to be resolved using the
   * `project(...)` method to get the value on which the `file` method may be
   * called:
   * ```
   * sourceDocsDir = project(":someProject").file("src/main/resources")
   * ```
   *
   * Defaults to [DefaultResourceDocsDir].
   */
  var resourceDocsDir: File = project.file(DefaultResourceDocsDir)

  /**
   * Path to the API documentation output file.
   *
   * This file is typically used in a GitHub pages deployment to allow easily
   * referencing the API docs.
   *
   * Default value is [DefaultAPIDocFile].
   */
  var apiDocOutputFile: File = project.file(DefaultAPIDocFile)

  /**
   * Path to the RAML schema directory used when merging RAML files and
   * generating JaxRS Java code.
   *
   * This field may be set using Gradle's `file` function to resolve the path
   * relative to the (sub)project root.
   * ```
   * schemaRootDir = file("schema")
   * ```
   *
   * If the target path is in the root project and not the current subproject,
   * the field may be set using:
   * ```
   * schemaRootDir = rootProject.file("schema")
   * ```
   *
   * For intermediary projects, the project will need to be resolved using the
   * `project(...)` method to get the value on which the `file` method may be
   * called:
   * ```
   * schemaRootDir = project(":someProject").file("schema")
   * ```
   *
   * Defaults to [DefaultSchemaRootDir]
   */
  var schemaRootDir: File = project.file(DefaultSchemaRootDir)
    set(value) {
      if (!setOutput)
        mergedOutputFile = value.resolve(ExecMergeRaml.DefaultOutputLibraryName)

      field = value
    }

  /**
   * Path to the root RAML API definition to use when generating JaxRS Java
   * code.
   *
   * Defaults to [DefaultRootRamlFile]
   */
  var rootApiDefinition: File = project.file(DefaultRootRamlFile)

  /**
   * Defines the path where the merged RAML output file will be placed.
   *
   * This value will be automatically added to the file exclusion list when
   * calling the RAML merge tool.
   *
   * This field may be set using Gradle's `file` function to resolve the path
   * relative to the (sub)project root.
   * ```
   * outputFilePath = file("schema/library.raml")
   * ```
   *
   * If the target path is in the root project and not the current subproject,
   * the field may be set using:
   * ```
   * outputFilePath = rootProject.file("schema/library.raml")
   * ```
   *
   * For intermediary projects, the project will need to be resolved using the
   * `project(...)` method to get the value on which the `file` method may be
   * called:
   * ```
   * outputFilePath = project(":someProject").file("schema/library.raml")
   * ```
   *
   * Defaults to [DefaultMergedRamlFile].
   */
  var mergedOutputFile: File = project.file(DefaultMergedRamlFile)
    set(value) {
      field = value
      setOutput = true
    }
  private var setOutput = false

  /**
   * List of paths in addition to the [output file][mergedOutputFile] to
   * exclude when executing the RAML merge tool.
   *
   * Entries in this list will be resolved relative to the
   * [schema root][schemaRootDir], and may be files or directories.
   *
   * Defaults to an empty list.
   */
  var mergeExcludedFiles: Collection<String> = listOf()

  /**
   * Version of the RAML merge tool to use.
   *
   * See https://github.com/VEuPathDB/script-raml-merge/releases to check for
   * updates if desired.
   *
   * Defaults to [DefaultRamlMergeToolVersion]
   */
  var mergeToolVersion: String = DefaultRamlMergeToolVersion

  /**
   * Download URL for the Raml4JaxRS code generation tool.
   *
   * Defaults to [DefaultRaml4JaxRSDownloadURL].
   */
  var raml4JaxRSDownloadURL: URI = URI.create(DefaultRaml4JaxRSDownloadURL)

  /**
   * Sets whether stream types should be created for generated JaxRS POJO types.
   *
   * Defaults to `true`.
   */
  var generateModelStreams: Boolean = true
}
