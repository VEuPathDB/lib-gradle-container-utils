package org.veupathdb.lib.gradle.container.tasks.jaxrs

import org.veupathdb.lib.gradle.container.tasks.base.JaxRSSourceAction
import java.io.File

open class JaxRSGenerateStreams : JaxRSSourceAction() {

  companion object {
    const val TaskName = "patch-gen-streams"
  }

  override val pluginDescription: String
    get() = "Generates streaming output supported extensions of RAML generated model types."

  override fun execute() {
    log.open()

    // Get the list of generated model source code directories
    getGeneratedModelDirectories()
      // Iterate through them and move down the call change
      .forEach(this::handleModelDirectory)

    log.close()
  }

  private fun handleModelDirectory(dir: File) {
    log.open(dir)

    // get package name
    val filePackage = projectConfig().projectPackage + ".generated.model"

    // List the files present in the model directory
    dir.listFiles()?.forEach {
      // If the file is ends with "Impl" then we care about it
      if (it.name.endsWith("Impl.java"))
        handleModelFileList(filePackage, it)
    }

    log.close()
  }

  // Here we know that we have a handle on a file whose name ends with "Impl"
  private fun handleModelFileList(filePackage: String, file: File) {
    log.open(filePackage, file)

    // Get the name of the class we will be generating
    val newClassName = file.name.let { it.substring(0, it.length - 9) } + "Stream"
    val extendsName  = file.name.let { it.substring(0, it.length - 5) }

    // Get a handle on the new file being created
    val newFile = file.resolveSibling("$newClassName.java")

    // Create the new file
    newFile.createNewFile()

    newFile.bufferedWriter().use {
      it.write(templateSource(filePackage, newClassName, extendsName))
      it.flush()
    }

    log.close()
  }
}

private fun templateSource(
  packName: String,
  className: String,
  extendName: String,
) = """
package $packName;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.StreamingOutput;

public class $className extends $extendName implements StreamingOutput {

  private final Consumer<OutputStream> _streamer;

  public $className(Consumer<OutputStream> streamer) {
    _streamer = streamer;
  }

  @Override
  public void write(OutputStream output) throws IOException, WebApplicationException {
    _streamer.accept(output);
  }
}
"""