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
    private const val LockFile = "raml4jaxrs.lock"

    const val TaskName = "install-raml-4-jax-rs"

    const val OutputFile = "raml-to-jaxrs.jar"

    const val RamlToJaxrsDownloadLink = "https://github.com/VEuPathDB/maven-packages/blob/main/raw-packages/org/raml/jaxrs/3.0.7/raml-to-jaxrs-cli-3.0.7.jar?raw=true";
  }

  override fun execute() {
    install();
  }

  override val pluginDescription
    get() = "Downloads and installs the Raml for JaxRS generator."

  private fun install() {
    log.open()

    val res = HttpClient.newBuilder()
      .followRedirects(HttpClient.Redirect.NEVER)
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
    file.delete()
    file.createNewFile()

    FileOutputStream(file).use {
      res.body().transferTo(it)
      it.flush()
    }

    log.close()
  }
}
