package org.veupathdb.lib.gradle.container.tasks.base.exec

open class ExecConfiguration(
  var arguments:   List<String>        = ArrayList(),
  var environment: Map<String, String> = HashMap(),
)
