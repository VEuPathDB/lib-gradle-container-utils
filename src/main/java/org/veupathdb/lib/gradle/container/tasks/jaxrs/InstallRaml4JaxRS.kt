package org.veupathdb.lib.gradle.container.tasks.jaxrs

import org.veupathdb.lib.gradle.container.tasks.base.BinInstallAction

import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * RAML 4 Jax-RS Generator Installation
 *
 * @since 1.1.0
 */
open class InstallRaml4JaxRS : BinInstallAction() {

  companion object {
    const val TaskName = "install-raml-4-jax-rs"

    const val OutputFile = "raml-to-jaxrs.jar"

    const val RamlToJaxrsDownloadLink = "https://github.com/VEuPathDB/maven-packages/blob/main/raw-packages/org/raml/jaxrs/3.0.7/raml-to-jaxrs-cli-3.0.7-jar-with-dependencies.jar?raw=true"
  }

  override fun execute() {
    createBuildRootIfNotExists();
    install();
  }

  override val pluginDescription
    get() = "Downloads and installs the Raml for JaxRS generator."

  private fun install() {
    log.open()

    val res = HttpClient.newBuilder()
      .followRedirects(HttpClient.Redirect.ALWAYS)
      .build()
      .send(
        HttpRequest.newBuilder(
          URI.create(RamlToJaxrsDownloadLink)
        )
          .GET()
          .build(),
        HttpResponse.BodyHandlers.ofInputStream()
      )
    val file = File(getBinRoot(), OutputFile)
    log.info("Creating file at ${file.path}")
    file.delete()
    file.createNewFile()

    log.info("Received ${res.statusCode()} status from download URL")

    log.info("Fetching raml tool from $RamlToJaxrsDownloadLink")
    FileOutputStream(file).use {
      res.body().transferTo(it)
      it.flush()
    }

    log.close()
  }

  protected fun createBuildRootIfNotExists() {
    log.open()

    val dir = getBinRoot()

    if (!dir.exists() && !dir.mkdirs()) {
      log.error("Failed to create build root $dir")
      throw RuntimeException("Failed to create build root $dir")
    }

    log.close()
  }
}
