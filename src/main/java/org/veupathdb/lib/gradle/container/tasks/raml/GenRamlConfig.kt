package org.veupathdb.lib.gradle.container.tasks.raml

import org.veupathdb.lib.gradle.container.tasks.base.exec.ExecConfiguration

data class GenRamlConfig(
  var repoDocsDir: String = "docs",
  var resourceDocsDir: String = "src/main/resources",
  var apiDocFileName: String = "api.html",
) : ExecConfiguration()
