package org.veupathdb.lib.gradle.container.tasks.raml

import org.veupathdb.lib.gradle.container.tasks.base.exec.ExecConfiguration
import java.io.File

data class GenRamlConfig(
  var repoDocsDir: String = "docs",
  var resourceDocsDir: String = "src/main/resources",
  var apiDocFileName: String = "api.html",
  var schemaRootDir: String = "schema",
  var outputFilePath: File = File(schemaRootDir, "library.raml"),
  var excludedFiles: Collection<String> = listOf("library.raml"),
  var mergeRamlVersion: String = "v2.0.0"
) : ExecConfiguration()
