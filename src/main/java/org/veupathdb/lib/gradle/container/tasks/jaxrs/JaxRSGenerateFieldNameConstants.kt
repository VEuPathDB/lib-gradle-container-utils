package org.veupathdb.lib.gradle.container.tasks.jaxrs

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.io.File

import org.veupathdb.lib.gradle.container.tasks.base.JaxRSSourceAction
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream

abstract class JaxRSGenerateFieldNameConstants : JaxRSSourceAction() {
  companion object {
    const val TaskName = "generate-model-class-field-json-key-constants"

    private val JacksonPropListAnnotation = "@" + JsonPropertyOrder::class.simpleName
    private val JacksonPropAnnotation = "@" + JsonProperty::class.simpleName

    private const val JacksonMultilinePropPrefix = "      value = \""

    private const val ConstantsClassName = "JsonField"

    private val AsciiDigitRange = 48..57
    private val AsciiLowerRange = 97..122
    private val AsciiUpperRange = 65..90
    private const val AsciiUnderscore = 95
    private const val CaseModifier = 32
  }


  override val pluginDescription: String
    get() = "Generates constant values for the JSON keys of POJO fields.\n" +
    "Not intended to be used directly."

  override fun execute() {
    log.open()
    val propertyNames = HashMap<String, String>(64)

    val pojos = getGeneratedModelDirectories()
      .map(File::listFiles)
      .filterNotNull()
      .flatMap(Array<File>::asSequence)
      .filterNot { it.name.endsWith("Impl.java") }
      .map(::matchImplToInterface)
      .filterNotNull()
      .onEach { (_, impl) -> appendJsonFields(impl, propertyNames) }
      .toList()

    if (pojos.isEmpty()) {
      log.error("no POJOs found to patch")
      log.close()
      return
    }

    computeConstNames(propertyNames)

    // Remove name entries that we couldn't generate a const field name for.
    with(propertyNames.iterator()) {
      while (hasNext()) {
        if (next().value.isBlank())
          remove()
      }
    }

    buildConstantsClass(pojos[0].first.parentFile, propertyNames.toSortedMap())

    patchPojos(pojos, propertyNames)
    log.close()
  }

  private fun matchImplToInterface(possibleInterface: File): Pair<File, File>? {
    log.open(possibleInterface)
    val implFile = File(
      possibleInterface.parentFile,
      possibleInterface.name.substring(0, possibleInterface.name.length - 5) + "Impl.java",
    )

    // If a matching implementation file exists for the given possible match,
    // then that possible match is an interface and should be processed.
    return log.close(if (implFile.exists())
      possibleInterface to implFile
    else
      null)
  }

  private fun appendJsonFields(impl: File, fields: MutableMap<String, String>) {
    log.open(impl, "Map<String, String>")
    getJsonFields(impl).forEach { fields[it] = "" }
    log.close()
  }

  private fun getJsonFields(impl: File) = sequence {
    impl.useLines { lines ->
      val it = lines.iterator()
      // skip lines until the @JsonPropertyOrder annotation, as it contains a
      // full list of all the JSON properties in a simple string array.
      for (line in it)
        if (line.startsWith(JacksonPropListAnnotation))
          streamProperties(it)
    }
  }

  private suspend fun SequenceScope<String>.streamProperties(lines: Iterator<String>) {
    for (line in lines) {
      if (line.startsWith("    \""))
        yield(line.substring(5, line.lastIndexOf('"')))
      else if (line.startsWith("})"))
        break
    }
  }

  private fun computeConstNames(names: MutableMap<String, String>) {
    for ((key, _) in names)
      names[key] = computeConstName(key)
  }

  private fun buildConstantsClass(outDir: File, names: Map<String, String>) {
    val constantsFile = File(outDir, "$ConstantsClassName.java")

    constantsFile.bufferedWriter().use {
      it.writeLine("""
        package ${options.service.projectPackage}.generated.model;
        
        public final class $ConstantsClassName {
      """.trimIndent())

      names.forEach { (value, name) ->
        it.writeLine("  public static final String $name = \"$value\";")
      }

      it.writeLine("}")

      it.flush()
    }
  }

  private fun patchPojos(pojos: List<Pair<File, File>>, names: Map<String, String>) {
    val prefix = "  $JacksonPropAnnotation("

    val fn = { i: BufferedReader, o: BufferedWriter ->
      val seq = i.lineSequence().iterator()

      for (line in seq) {
        if (line.startsWith(prefix)) {
          o.write(prefix)
          tryPatchPropAnnotation(line.substring(prefix.length), seq, names, o)
        } else {
          o.writeLine(line)
        }
      }
    }

    pojos.forEach { (iface, impl) ->
      processFile(iface, fn)
      processFile(impl, fn)
    }

  }

  private fun tryPatchPropAnnotation(
    remainder: String,
    sequence: Iterator<String>,
    names: Map<String, String>,
    buf: BufferedWriter
  ) {
    // Handle multi-line properties
    if (remainder.isBlank()) {
      buf.newLine()

      for (line in sequence) {
        when {
          line.startsWith(JacksonMultilinePropPrefix) -> {
            val name = line.substring(JacksonMultilinePropPrefix.length, line.lastIndexOf('"'))

            names[name]?.also {
              // write the prefix minus the double quote at the end
              buf.write(line.substring(0, JacksonMultilinePropPrefix.length - 1))
              buf.write("$ConstantsClassName.$it")

              if (line.endsWith(','))
                buf.append(',')

              buf.newLine()

              return
            }

            buf.writeLine(line)
            return
          }

          line.startsWith("  )") -> {
            buf.writeLine(line)
            return
          }
        }
      }
    }

    // Handle single-line properties
    val name = remainder.substring(1, remainder.lastIndexOf('"'))

    names[name]?.also {
      buf.write("$ConstantsClassName.$it")
      buf.append(')')
      buf.newLine()
      return
    }

    buf.writeLine(remainder)
  }

  private fun computeConstName(name: String): String {
    val out = ByteArrayOutputStream(name.length + 8)
    val raw = name.byteInputStream()

    // upper = 1
    // lower = 2
    // digit = 3
    // under = 4
    var lastType = 0
    var written = 0
    while (true) {
      when (val b = raw.read()) {
        -1 -> break

        in AsciiUpperRange -> {
          if (lastType == 2 || lastType == 3)
            out.write(AsciiUnderscore)
          out.write(b)
          lastType = 1
        }

        in AsciiLowerRange -> {
          if (lastType == 3 || lastType == 4)
            out.write(AsciiUnderscore)
          out.write(b - CaseModifier)
          lastType = 2
        }

        in AsciiDigitRange -> {
          if (written == 0 || lastType == 4)
            continue
          if (lastType != 3)
            out.write(AsciiUnderscore)
          out.write(b)
          lastType = 3
        }

        else -> {
          if (written == 0 || lastType == 4)
            continue

          out.write(AsciiUnderscore)
          lastType = 4
        }
      }

      written++
    }

    // If the whole property name is something that can't be converted to a java
    // variable name, return an empty string.  We will skip empty constant names
    // when generating the constants.
    if (written == 0)
      return ""

    // If the last character was an underscore, trim it off.
    if (lastType == 4)
      return out.toString().let { it.substring(0, it.length-1) }

    return out.toString()
  }
}
