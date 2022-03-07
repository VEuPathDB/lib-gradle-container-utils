package org.veupathdb.lib.gradle.container.tasks.base.build;

data class GlobalVendorBuildConfiguration(
  var vendorDirectory: String = "vendor"
) : GlobalBuildConfiguration()
