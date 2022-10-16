package org.veupathdb.lib.gradle.container.tasks.base

import org.gradle.api.tasks.Internal
import org.veupathdb.lib.gradle.container.tasks.jaxrs.GeneratedModelDirectory
import org.veupathdb.lib.gradle.container.tasks.jaxrs.GeneratedResourceDirectory

import java.io.File
import java.util.stream.Stream

abstract class JaxRSSourceAction : SourceAction() {

  companion object {
    private const val GeneratedDir = "generated"
    private const val ModelDir     = "$GeneratedDir/$GeneratedModelDirectory"
    private const val ResourceDir  = "$GeneratedDir/$GeneratedResourceDirectory"
  }


  @Internal
  protected fun getGeneratedSourceDirectories(): Stream<File> {
    log.open()
    return log.close(getProjectSourceDirectories()
      .map { File(it, GeneratedDir) }
      .filter(File::exists))
  }

  @Internal
  protected fun getGeneratedModelDirectories(): Stream<File> {
    log.open()
    return log.close(getProjectSourceDirectories()
      .map { File(it, ModelDir) }
      .filter(File::exists))
  }

  @Internal
  protected fun getGeneratedResourceDirectories(): Stream<File> {
    log.open()
    return log.close(getProjectSourceDirectories()
      .map { File(it, ResourceDir) }
      .filter(File::exists))
  }

}
