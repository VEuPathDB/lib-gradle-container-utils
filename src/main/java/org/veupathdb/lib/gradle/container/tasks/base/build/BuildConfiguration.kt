package org.veupathdb.lib.gradle.container.tasks.base.build;

/**
 * Generic Build Configuration
 * <p>
 * Provides a base set of options relevant to most if not all tasks that build
 * external projects dependencies to this project.
 */
open class BuildConfiguration(var url: String, var targetVersion: String)