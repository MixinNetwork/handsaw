package one.mixin.handsaw

import net.pearx.kasechange.toCamelCase
import java.io.File

interface Generator {
  fun generate(parseResult: ParseResult, outputFile: String?)
  fun validPlatform(platform: String): Boolean
}

private val androidPlaceHolder = "%[\\d]+[\$][d|s]".toRegex()
private val iosPlaceHolder = "%@".toRegex()

private const val dot3 = "..."
private const val ellipsis = "…"
private const val twoStar = "**"

class AndroidGenerator : Generator {
  private val needPluralLangList = listOf("en")
  private val invalidStringList = listOf("%@")

  override fun generate(parseResult: ParseResult, outputFile: String?) {
    val path = if (outputFile.isNullOrBlank()) {
      System.getProperty("user.dir")
    } else {
      outputFile
    }
    val outDir = File("$path${File.separator}output${File.separator}Android")
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

      val value: String = try {
        v[index]
          .replace("\'", "\\\'")
          .replace("\"", "\\\"")
          .replace("&", "&amp;")
          .replace(dot3, ellipsis)
      } catch (e: IndexOutOfBoundsException) {
        ""
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
        result.appendRu(value, lang)
          .append("\t\t<item quantity=\"other\">$value</item>\n")
          .append("\t</plurals>\n")
        pluralKey = ""
      } else if (k.endsWith(".count")) {
        val localPluralKey = k.substringBeforeLast(".count")
        val singleExists = data[localPluralKey]
        if (singleExists != null &&
          singleExists.isNotEmpty() &&
          singleExists.getOrNull(index)?.isNotBlank() == true
        ) {
          getPluralHead(result, localPluralKey, lang)
          result.appendRu(value, lang)
            .append("\t\t<item quantity=\"other\">$value</item>\n")
          pluralKey = localPluralKey
        } else {
          result.append("\t<plurals name=\"$localPluralKey\" tools:ignore=\"UnusedQuantity\">\n")
            .appendRu(value, lang)
            .append("\t\t<item quantity=\"other\">$value</item>\n")
            .append("\t</plurals>\n")
        }
      } else if (pluralKey == "" && data["$k.count"] != null) {
        getPluralHead(result, k, lang)
        result.append("\t\t<item quantity=\"one\">$value</item>\n")
        pluralKey = k
      } else {
        result.append("\t<string name=\"$k\">$value</string>\n")
      }
    }

    if (invalidStringList.any { result.indexOf(it) >= 0 })
      throw FormatException("Android generated strings contains invalid characters in $invalidStringList")

    return if (result.indexOf("tools:") < 0) {
      "<resources>\n$result</resources>"
    } else {
      "<resources xmlns:tools=\"http://schemas.android.com/tools\">\n$result</resources>"
    }
  }

  private fun getPluralHead(
    result: StringBuilder,
    localPluralKey: String,
    lang: String
  ) {
    result.append("\t<plurals name=\"$localPluralKey\"")
    if (needPluralLangList.containsIgnoreCase(lang)) {
      result.append(">\n")
    } else {
      result.append(" tools:ignore=\"UnusedQuantity\">\n")
    }
  }

  private fun StringBuilder.appendRu(
    value: String,
    lang: String,
  ): StringBuilder = if (lang.equals("ru", true)) {
    append("\t\t<item quantity=\"few\">$value</item>\n")
      .append("\t\t<item quantity=\"many\">$value</item>\n")
  } else this

  override fun validPlatform(platform: String): Boolean =
    platform.split(',').containsIgnoreCase(Platform.Android.toString())
}

class IOSGenerator(
  private val platform: Platform = Platform.IOS,
  private val keyType: KeyType = KeyType.Default,
) : Generator {
  override fun generate(parseResult: ParseResult, outputFile: String?) {
    val path = if (outputFile.isNullOrBlank()) {
      System.getProperty("user.dir")
    } else {
      outputFile
    }
    val outDir = File("$path${File.separator}output${File.separator}$platform")
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
    val enIndex = parseResult.langList.indexOf("en")
    val data = parseResult.dataList
    val platformMap = parseResult.platformMap
    val result = StringBuilder()
    data.forEach { (k, v) ->
      val platform = platformMap[k]
      if (platform == null || !validPlatform(platform)) return@forEach

      var value: String
      try {
        value = v[index]

        if (value.isBlank()) {
          // use en value if current value is empty
          value = v[enIndex]
        }

        value = value.replace("\"", "\\\"")
          .replace(twoStar, "")
          .replace(ellipsis, dot3)

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
    platform.split(',').containsIgnoreCase(this.platform.toString())
}

class FlutterGenerator : Generator {
  override fun generate(parseResult: ParseResult, outputFile: String?) {
    val validDataList = parseResult.dataList.filter { it ->
      parseResult.platformMap[it.key].let {
        it != null && validPlatform(it)
      }
    }

    val langData = parseResult.langList.groupBy({ it }, { lang ->
      validDataList.mapNotNull {
        val v = it.value.getOrNull(parseResult.langList.indexOf(lang)) ?: return@mapNotNull null
        it.key to v
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

      originalMap.forEach inner@{
        val value = it.value.let { _value ->
          var value = _value
          (androidPlaceHolder.findAll(value) + iosPlaceHolder.findAll(value)).forEachIndexed { index, matchResult ->
            value = value.replace(matchResult.value, "{arg$index}")
          }
          value.replace("\n", "\n")
            .replace(twoStar, "")

          value.trim()
        }

        if (value.isBlank()) return@inner

        if (it.key.endsWith(".count")) {
          val key = it.key.substringBeforeLast(".count").toCamelCase()
          val one = newMap[key]

          newMap[key] = "{count, plural, one{${one}} other{${value}}}"
          return@inner
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
    platform.split(',').containsIgnoreCase(Platform.Desktop.toString())
}

enum class KeyType {
  Default, EnValue
}

data class FormatException(val msg: String) : Exception(msg)

