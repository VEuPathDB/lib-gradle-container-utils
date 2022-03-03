package org.veupathdb.lib.gradle.container.tasks.raml

import org.jetbrains.annotations.NotNull
import org.veupathdb.lib.gradle.container.tasks.base.exec.ExecConfiguration

import java.util.Objects

data class GenRamlConfig(
  var repoDocsDir: String = "docs",
  var resourceDocsDir: String = "src/main/resources",
  var apiDocFileName: String = "api.html",
) : ExecConfiguration()
