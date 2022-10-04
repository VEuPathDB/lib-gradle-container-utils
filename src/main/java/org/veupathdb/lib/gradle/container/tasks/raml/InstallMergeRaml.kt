package org.veupathdb.lib.gradle.container.tasks.raml

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.veupathdb.lib.gradle.container.tasks.base.Action
import java.lang.RuntimeException
import java.net.URL

open class InstallMergeRaml : Action() {

  companion object {
    private const val RamlMergeLatestURL = "https://api.github.com/repos/VEuPathDB/script-raml-merge/releases/latest"

    private const val LockFileName = "merge-raml.lock"

    private const val BinaryFileName = "merge-raml"

    private const val BinDirectoryName = ".bin"

    private const val DLFileName = "merge-raml-download.tar.gz"

    const val TaskName = "install-raml-merge"
  }


  private val jackson by lazy { ObjectMapper() }

  private val binDirectory by lazy { RootDir.resolve(BinDirectoryName) }
  private val lockFile by lazy { binDirectory.resolve(LockFileName) }
  private val binaryFile by lazy { binDirectory.resolve(BinaryFileName) }
  private val downloadFile by lazy { binDirectory.resolve(DLFileName) }


  override val pluginDescription: String
    get() = "Downloads the raml merge script from GitHub"

  override fun execute() {
    // If we have the lock file and the binary then go down the update path.
    if (lockFile.exists() && binaryFile.exists())
      return executeUpdate()

    // If we have one of the files for some reason, recover in the cleanup path.
    if (lockFile.exists() || binaryFile.exists())
      return executeCleanup()

    // Since we don't have any trace of it yet, just do the initial download
    return executeDownload()
  }


  private fun executeUpdate() {
    // Get the lock version
    val lockVersion = lockFile.readText().trim()

    // Get the latest script release details
    val release = getReleaseInfo()

    // If the downloaded version is already the latest, then there is nothing
    // we need to do.
    if (lockVersion == release.tag)
      return

    // If we have a newer version for some reason (local development of the
    // script) do nothing.
    if (lockVersion > release.tag!!)
      return

    // So we have an older version downloaded, cleanup and execute a download
    binaryFile.delete()
    lockFile.delete()
    executeDownload(release)
  }

  private fun executeCleanup() {
    binaryFile.delete()
    lockFile.delete()
    executeDownload()
  }

  private fun executeDownload() {
    executeDownload(getReleaseInfo())
  }

  private fun executeDownload(release: GitHubRelease) {
    // Get the download link to download the binary.
    val downloadLink = URL(release.getDownloadLink(getOS()))

    downloadFile.createNewFile()
    downloadFile.outputStream().use { output -> downloadLink.openStream().use { input -> input.transferTo(output) } }

    unpackDownload()
    downloadFile.delete()

    lockFile.createNewFile()
    lockFile.writer().use { output -> output.write(release.tag) }
  }

  private fun unpackDownload() {
    with(
      ProcessBuilder("tar", "-xf", downloadFile.path)
        .directory(binDirectory)
        .start()
    ) {
      errorStream.transferTo(System.err)
      require(waitFor() == 0)
    }
  }

  private fun getReleaseInfo() =
    jackson.readValue(URL(RamlMergeLatestURL), GitHubRelease::class.java)

  private fun getOS() =
    System.getProperty("os.name").lowercase().let {
      when {
        it.contains("mac") -> OS.MAC
        it.contains("nux") -> OS.NIX
        it.contains("nix") -> OS.NIX
        else               -> throw RuntimeException("Unsupported operating system.")
      }
    }

  private fun GitHubRelease.getDownloadLink(os: OS): String {
    val search = if (os == OS.MAC) "darwin" else "linux"

    for (asset in assets) {
      if (asset.browserDownloadURL!!.contains(search))
        return asset.browserDownloadURL!!
    }

    throw IllegalStateException("No download found for $os")
  }
}

private enum class OS {
  MAC,
  NIX,
  DOS,
}

@JsonIgnoreProperties(ignoreUnknown = true)
private class GitHubRelease {
  @JsonProperty("tag_name")
  var tag: String? = null

  @JsonProperty("assets")
  var assets: List<GitHubReleaseAsset> = emptyList()
}

@JsonIgnoreProperties(ignoreUnknown = true)
private class GitHubReleaseAsset {
  @JsonProperty("name")
  var name: String? = null

  @JsonProperty("browser_download_url")
  var browserDownloadURL: String? = null
}