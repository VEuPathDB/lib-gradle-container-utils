package org.veupathdb.lib.gradle.container.tasks.raml

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.veupathdb.lib.gradle.container.tasks.base.Action
import java.net.URI
import java.nio.file.Files

open class InstallMergeRaml : Action() {

  companion object {
    private const val RamlMergeReleaseURL = "https://api.github.com/repos/VEuPathDB/script-raml-merge/releases/"

    private const val LockFileName = "merge-raml.lock"

    private const val BinaryFileName = "merge-raml"

    private const val BinDirectoryName = ".bin"

    private const val DLFileName = "merge-raml-download.tar.gz"

    const val TaskName = "install-raml-merge"
  }

  private val jackson by lazy { ObjectMapper() }

  private val binDirectory by lazy { RootDir.resolve(BinDirectoryName) }
  private val downloadFile by lazy { binDirectory.resolve(DLFileName) }

  @get:Input
  val targetVersion
    get() = options.raml.mergeToolVersion

  @get:OutputFile
  val binaryFile
    get() = binDirectory.resolve(BinaryFileName)

  @get:OutputFile
  val lockFile
    get() = binDirectory.resolve(LockFileName)

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

    // If we already have the desired version installed, then there is nothing
    // to do.
    if (lockVersion == targetVersion)
      return

    // Get the release details for the target version.
    val release = getReleaseInfo(targetVersion)

    // If the downloaded version is already the desired version, then there is
    // nothing we need to do.
    if (lockVersion == release.tag)
      return

    // We have a different version downloaded, cleanup and download a new copy
    executeCleanup(release)
  }

  private fun executeCleanup(release: GitHubRelease? = null) {
    binaryFile.delete()
    lockFile.delete()
    if (release == null)
      executeDownload()
    else
      executeDownload(release)
  }

  private fun executeDownload() {
    executeDownload(getReleaseInfo(options.raml.mergeToolVersion))
  }

  private fun executeDownload(release: GitHubRelease) {
    // Get the download link to download the binary.
    val downloadLink = URI.create(release.getDownloadLink(getOS())).toURL()

    Files.createDirectories(binDirectory.toPath());
    downloadFile.createNewFile()
    downloadFile.outputStream().use { output -> downloadLink.openStream().use { input -> input.transferTo(output) } }

    unpackDownload()
    downloadFile.delete()

    lockFile.createNewFile()
    lockFile.writer().use { output -> output.write(release.tag!!) }
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

  private fun getReleaseInfo(targetVersion: String) =
    jackson.readValue(targetVersion.toDownloadURL(), GitHubRelease::class.java)

  private fun String.toDownloadURL() =
    URI.create(
      if (this == "latest")
        RamlMergeReleaseURL + "latest"
      else
        RamlMergeReleaseURL + "tags/" + this
    ).toURL()

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
