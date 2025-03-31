package org.veupathdb.lib.gradle.container.tasks.check

import org.veupathdb.lib.gradle.container.tasks.base.Action
import java.io.File
import java.util.Arrays
import java.util.stream.Collectors

open class CheckEnv : Action() {
  companion object {
    const val TaskName = "check-env"

    const val NPM    = "npm"
    const val Maven  = "mvn"
    const val Docker = "docker"
  }

  fun allCommandFilter(name: String) =
    when (name) {
      NPM    -> true
      Maven  -> true
      Docker -> true
      else   -> false
    }


  override fun execute() {
    var ok = true

    val path = System.getenv("PATH")
      .split(':')
      .stream()
      .map(::File)
      .map(File::listFiles)
      .filter { it != null }
      .flatMap(Arrays::stream)
      .map(File::getName)
      .filter(this::allCommandFilter)
      .collect(Collectors.toSet()) as Set<String>

    if (NPM !in path) {
      ok = false
      log.error("NPM is not installed.  Please install it and try again.")
    }

    if (Maven !in path) {
      ok = false
      log.error("Maven is not installed.  Please install it and try again.")
    }

    if (Docker !in path) {
      logger.warn("Docker is not installed.  The command 'make docker' will not work.")
    }

    if (!ok) {
      throw RuntimeException("Bad environment.")
    }
  }

  override val pluginDescription: String
    get() = "Checks the environment for required tools."


}
