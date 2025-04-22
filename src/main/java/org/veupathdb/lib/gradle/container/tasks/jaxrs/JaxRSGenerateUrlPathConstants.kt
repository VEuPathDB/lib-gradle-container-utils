package org.veupathdb.lib.gradle.container.tasks.jaxrs

import java.io.File

import org.veupathdb.lib.gradle.container.tasks.base.JaxRSSourceAction
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

abstract class JaxRSGenerateUrlPathConstants : JaxRSSourceAction() {
  companion object {
    const val TaskName = "generate-resource-class-url-path-constants"

    private val SubPathRegex = Regex("^ {2}@Path\\(\"([^\"]+)\"\\)$")
    private val MethodNameRegex = Regex("^ {2}(?:Get|Post|Delete|Patch|Put)(\\w+) .+$")
    private val VariableRegex = Regex("\\{[^}]+\\}")
  }

  override val pluginDescription: String
    get() = "Generates constant values for the URL paths of generated" +
    " controller methods."

  override fun register() {
    super.register()

    dependsOn(GenerateJaxRS.TaskName)
  }

   override fun execute() {
    log.exec {
      getGeneratedResourceDirectories()
        .mapNotNull(File::listFiles)
        .flatMap(Array<File>::asSequence)
        .forEach(::injectConstants)
    }
  }

  private fun injectConstants(file: File) = log.exec(file) {
    val result = file.bufferedReader().use(::findConstants)
    val tmpFile = File("/tmp/${file.name}")
    tmpFile.deleteOnExit()

    file.copyTo(tmpFile, overwrite = true)

    FileChannel.open(tmpFile.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE).use { target ->
      target.position(result.offset.toLong()).write(buildConstants(result))

      FileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE).use { source ->
        source.transferTo(result.offset.toLong(), Long.MAX_VALUE, target)
      }
    }

    tmpFile.copyTo(file, overwrite = true)
  }

  private fun findConstants(stream: BufferedReader): ScanResult {
    var rootPath = ""
    val subPaths = HashSet<String>(8)
    val variables = HashSet<String>(12)

    val it = stream.lineSequence().iterator()

    var readCount = 0

    // Scan until we hit the root path
    while (it.hasNext()) {
      val line = it.next()
      readCount += line.length+1

      if (line.startsWith("@Path(\"")) {
        rootPath = line.substring(7, line.indexOf('"', 7))

        VariableRegex.findAll(rootPath)
          .map { it.value }
          .forEach { variables.add(it) }

        break
      }
    }

    // Scan until we hit the class def start (skip over other annotations).
    //
    // Once we hit the class def start, we have the offset of the position where
    // we will inject the computed constants.
    while (it.hasNext()) {
      val line = it.next()
      readCount += line.length+1

      if (line.startsWith("public "))
        break
    }

    // Scan for URL paths
    while (it.hasNext()) {
      val line = it.next()
      if (
        SubPathRegex.matchEntire(line)
          ?.also { subPaths.add(it.parseSubPath(variables)) } == null
        && line.startsWith("  class") // if we hit a response subclass, then we are done.
      ) break
    }


    if (rootPath.isEmpty())
      throw IllegalStateException("did not find a root path")
    if (!it.hasNext())
      throw IllegalStateException("reached end of file without encountering response subclasses")

    return ScanResult(rootPath, subPaths, variables, readCount)
  }

  private fun buildConstants(result: ScanResult): ByteBuffer {
    val sb = ByteArrayOutputStream(result.subPaths.size * 32 + result.variables.size * 24)
    val prefix = "  String ".toByteArray()
    val suffix = "\";\n\n".toByteArray()
    val plainInfix = " = \"".toByteArray()
    val subPathInfix = " = ROOT_PATH + \"".toByteArray()

    val write = { key: String, infix: ByteArray, value: String ->
      sb.write(prefix)
      sb.write(key.toByteArray())
      sb.write(infix)
      sb.write(value.toByteArray())
      sb.write(suffix)
    }

    write("ROOT_PATH", plainInfix, result.rootPath)

    for (path in result.subPaths)
      write(computeConstName(path) + "_PATH", subPathInfix, path)

    for (name in result.variables)
      write(computeConstName(name) + "_VAR", plainInfix, name)

    return ByteBuffer.wrap(sb.toByteArray())
  }

  private fun MatchResult.parseSubPath(variables: MutableSet<String>): String {
    val path = groupValues[1]

    VariableRegex.findAll(path)
      .map { it.value }
      .forEach { variables.add(it) }

    return path
  }

  private fun MatchResult.parseConstName(): String {
    return groupValues[1]
  }

  private data class ScanResult(
    val rootPath: String,
    val subPaths: Set<String>,
    val variables: Set<String>,
    val offset: Int
  )
}
