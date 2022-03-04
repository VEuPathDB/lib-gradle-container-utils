package org.veupathdb.lib.gradle.container.tasks.fgputil

import org.veupathdb.lib.gradle.container.tasks.base.Action

import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Arrays

open class DownloadFgpUtil : Action() {

  companion object {
    const val TaskName = "download-fgputil"

    private const val ReleaseUrl =
      "https://github.com/VEuPathDB/FgpUtil/releases/%s"
  }

  private val tag: String by lazy {
    val tmp = opts.version

    if (tmp == "latest") {
      log.info("Determining real git tag.")
      getRealTag()
    } else {
      tmp
    }
  }

  private val vendorDir: File by lazy {
    val tmp = File(RootDir, "vendor")
    tmp.mkdir()
    tmp
  }

  private val opts get() = options.fgputil

  ////////////////////////////////////////////////////////////////////////////

  override val pluginDescription: String
    get() {
      return "Downloads the selected (or latest) version of FgpUtil"
    }

  override fun fillIncrementalOutputFiles(files: MutableCollection<File>) {
    Arrays.stream(opts.targets)
      .map { name -> File(vendorDir, name.value) }
      .forEach(files::add)
  }

  override fun execute() {
    for (i in opts.targets) {
      val url = String.format(ReleaseUrl, "download/$tag/${i.value}")
      log.info("Downloading dependency: $url")
      val res = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .build()
        .send(
          HttpRequest.newBuilder(URI.create(url))
            .GET()
            .build(),
          HttpResponse.BodyHandlers.ofInputStream()
        )

      val file = File(vendorDir, i.value)
      file.delete()
      file.createNewFile()

      FileOutputStream(file).use {
        res.body().transferTo(it)
        it.flush()
      }
    }
  }

  private fun getRealTag(): String {
    val res = HttpClient.newBuilder()
      .followRedirects(HttpClient.Redirect.NEVER)
      .build()
      .send(
        HttpRequest.newBuilder(URI.create(String.format(ReleaseUrl,
          "latest")))
          .GET()
          .build(),
        HttpResponse.BodyHandlers.discarding()
      )

    val location = res.headers().map()["location"]!![0]

    return location.substring(location.lastIndexOf('/') + 1)
  }
}
