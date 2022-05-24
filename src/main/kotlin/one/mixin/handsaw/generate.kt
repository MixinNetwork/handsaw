package one.mixin.handsaw

import net.pearx.kasechange.toCamelCase
import java.io.File

interface Generator {
  fun generate(parseResult: ParseResult, outputFile: String?)
  fun validPlatform(platform: String): Boolean
}

val androidPlaceHolder = "%[\\d]+[\$][d|s]".toRegex()
val iosPlaceHolder = "%@".toRegex()

class AndroidGenerator : Generator {
  override fun generate(parseResult: ParseResult, outputFile: String?) {
    val path = if (outputFile.isNullOrBlank()) {
      System.getProperty("user.dir")
    } else {
      outputFile
    }
    val outDir =
      File("$path${File.separator}output${File.separator}Android")
    if (outDir.exists()) {
      outDir.delete()
    }
    outDir.mkdir()

    parseResult.langList.forEach { lang ->
      val text = convertLang(lang, parseResult)
      val dirName = "value-$lang"
      val fileName = "strings.xml"

      val dir = File("${outDir.path}${File.separator}$dirName")
      if (dir.exists()) {
        dir.delete()
      }
      dir.mkdirs()

      val file = File("$dir${File.separator}$fileName")
      if (file.exists()) {
        file.delete()
      }
      file.createNewFile()
      file.bufferedWriter().use { out ->
        out.write(text)
      }
    }
  }

  private fun convertLang(lang: String, parseResult: ParseResult): String {
    val index = parseResult.langList.indexOf(lang)
    val data = parseResult.dataList
    val platformMap = parseResult.platformMap
    val result = StringBuilder()
    var pluralKey = ""
    data.forEach { (k, v) ->
      val platform = platformMap[k]
      if (platform == null || !validPlatform(platform)) return@forEach

      var value: String
      try {
        value = v[index]
        if ("\'" in value) {
          value = value.replace("\'", "\\\'")
        }
        if ("\"" in value) {
          value = value.replace("\"", "\\\"")
        }
        if ("&" in value) {
          value = value.replace("&", "&amp;")
        }
      } catch (e: IndexOutOfBoundsException) {
        value = ""
      }
      if (value.isBlank()) {
        // skip if value is empty
        return@forEach
      }

      if (pluralKey == k) {
        result.append("\t\t<item quantity=\"one\">$value</item>\n")
          .append("\t</plurals>\n")
        pluralKey = ""
      } else if ("$pluralKey.count" == k) {
        result.append("\t\t<item quantity=\"other\">$value</item>\n")
          .append("\t</plurals>\n")
        pluralKey = ""
      } else if (k.endsWith(".count")) {
        val localPluralKey = k.substringBeforeLast(".count")
        val singleExists = data[localPluralKey]
        if (singleExists != null && singleExists.isNotEmpty() && singleExists.getOrNull(
            index
          )?.isNotBlank() == true
        ) {
          result.append("\t<plurals name=\"$localPluralKey\">\n")
            .append("\t\t<item quantity=\"other\">$value</item>\n")
          pluralKey = localPluralKey
        } else {
          result.append("\t<plurals name=\"$localPluralKey\" tools:ignore=\"UnusedQuantity\">\n")
            .append("\t\t<item quantity=\"other\">$value</item>\n")
            .append("\t</plurals>\n")
        }
      } else if (pluralKey == "" && data["$k.count"] != null) {
        result.append("\t<plurals name=\"$k\">\n")
          .append("\t\t<item quantity=\"one\">$value</item>\n")
        pluralKey = k
      } else {
        result.append("\t<string name=\"$k\">$value</string>\n")
      }
    }
    return "<resources xmlns:tools=\"http://schemas.android.com/tools\">\n$result</resources>"
  }

  override fun validPlatform(platform: String): Boolean =
    platform.contains("android", true)
}

class IOSGenerator(
  private val keyType: KeyType = KeyType.Default
) : Generator {
  override fun generate(parseResult: ParseResult, outputFile: String?) {
    val path = if (outputFile.isNullOrBlank()) {
      System.getProperty("user.dir")
    } else {
      outputFile
    }
    val outDir = File("$path${File.separator}output${File.separator}iOS")
    if (outDir.exists()) {
      outDir.delete()
    }
    outDir.mkdir()

    val keyEnValMap =
      if (keyType == KeyType.EnValue) mutableMapOf<String, String>() else null
    parseResult.langList.forEach { lang ->
      val text = convertLang(lang, parseResult, keyEnValMap)
      val dirName = "$lang.lproj"
      val fileName = "Localizable.strings"

      val dir = File("${outDir.path}${File.separator}$dirName")
      if (dir.exists()) {
        dir.delete()
      }
      dir.mkdirs()

      val file = File("$dir${File.separator}$fileName")
      if (file.exists()) {
        file.delete()
      }
      file.createNewFile()
      file.bufferedWriter().use { out ->
        out.write(text)
      }
    }
  }

  private fun convertLang(
    lang: String,
    parseResult: ParseResult,
    keyEnValMap: MutableMap<String, String>?
  ): String {
    val index = parseResult.langList.indexOf(lang)
    val data = parseResult.dataList
    val platformMap = parseResult.platformMap
    val result = StringBuilder()
    data.forEach { (k, v) ->
      val platform = platformMap[k]
      if (platform == null || !validPlatform(platform)) return@forEach

      var value: String
      try {
        value = v[index]
        if ("\"" in value) {
          value = value.replace("\"", "\\\"")
        }

        val phCount = androidPlaceHolder.findAll(value).count()
        value = value.replace(androidPlaceHolder) { r ->
          if (phCount > 1) {
            if (r.value.last() == 's') {
              r.value.dropLast(1).plus("@")
            } else {
              r.value
            }
          } else {
            val remain = r.value.drop(3)
            if (remain.last() == 's') {
              remain.dropLast(1).plus("%@")
            } else {
              remain.dropLast(1).plus("%d")
            }
          }
        }
      } catch (e: IndexOutOfBoundsException) {
        value = ""
      }
      if (value.isBlank()) {
        // skip if value is empty
        return@forEach
      }

      if (keyEnValMap != null && lang == "en") {
        keyEnValMap[k] = value
      }

      val line = if (keyEnValMap != null) {
        "\"${keyEnValMap[k]}\" = \"$value\";\n"
      } else {
        val localKey = if (k.endsWith(".count")) {
          k.replace(".count", "_count")
        } else k
        "\"${localKey.lowercase()}\" = \"$value\";\n"
      }

      result.append(line)
    }
    return result.toString()
  }

  override fun validPlatform(platform: String): Boolean =
    platform.contains("ios", true)
}

class FlutterGenerator : Generator {
  override fun generate(parseResult: ParseResult, outputFile: String?) {
    val validDataList = parseResult.dataList.filter { it ->
      parseResult.platformMap[it.key].let {
        it != null && validPlatform(it)
      }
    }

    val langData = parseResult.langList.groupBy({ it }, { lang ->
      validDataList.map {
        it.key to it.value[parseResult.langList.indexOf(lang)]
      }.toMap()
    }).map {
      it.key to it.value.first()
    }.toMap()

    val path = if (outputFile.isNullOrBlank()) {
      System.getProperty("user.dir")
    } else {
      outputFile
    }

    val outDir =
      File("$path${File.separator}output${File.separator}Flutter")
    if (outDir.exists()) {
      outDir.delete()
    }
    outDir.mkdir()

    langData.forEach { lang ->
      val file =
        File("${outDir.absolutePath}${File.separator}intl_${lang.key}.arb")
      if (file.exists()) {
        file.delete()
      }

      file.appendText("{")

      file.appendText("\n")

      val originalMap = lang.value

      val newMap = mutableMapOf<String, String>()

      originalMap.forEach {
        val value = it.value.let { _value ->
          var value = _value
          (androidPlaceHolder.findAll(value) + iosPlaceHolder.findAll(value)).forEachIndexed { index, matchResult ->
            value = value.replace(matchResult.value, "{arg$index}")
          }
          value.replace("\n", "\n")

          value.trim()
        }

        if (value.isBlank()) return@forEach

        if (it.key.endsWith(".count")) {
          val key = it.key.substringBeforeLast(".count").toCamelCase()
          val one = newMap[key]

          newMap[key] = "{count, plural, one{${one}} other{${value}}}"
          return@forEach
        }

        val key = it.key.toCamelCase().let { key ->
          if ("continue" == key) return@let "${key}Text"
          key
        }
        newMap[key] = value

      }

      newMap.toSortedMap().map {
        "\"${it.key}\" : \"${it.value}\""
      }.joinToString(separator = ",\n").let {
        file.appendText(it)
      }

      file.appendText("\n")
      file.appendText("}")
    }
  }

  override fun validPlatform(platform: String): Boolean =
    platform.contains("Desktop", true)

}

enum class KeyType {
  Default, EnValue
}
