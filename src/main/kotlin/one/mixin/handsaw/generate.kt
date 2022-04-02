package one.mixin.handsaw

import java.io.File

interface Generator {
  fun generate(parseResult: ParseResult, outputFile: String?)

  fun validPlatform(platform: String): Boolean
}

val allGenerators = listOf(AndroidGenerator(), IOSGenerator())

val ANDROID_PLACE_HOLDER = "%[\\d]+[\$][d|s]".toRegex()
val IOS_PLACE_HOLDER = "%[\\d]+[\$][@]".toRegex()

class AndroidGenerator : Generator {
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

      val line = "\t<string name=\"$k\">$value</string>\n"
      result.append(line)
    }
    return "<resources>\n$result</resources>"
  }

  override fun validPlatform(platform: String): Boolean {
    return platform.equals("Android", true) || platform.equals("Mobile", true)
  }
}

class IOSGenerator : Generator {
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

    parseResult.langList.forEach { lang ->
      val text = convertLang(lang, parseResult)
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

  private fun convertLang(lang: String, parseResult: ParseResult): String {
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
        if ("\'" in value) {
          value = value.replace("\'", "\\\'")
        }
        if ("\"" in value) {
          value = value.replace("\"", "\\\"")
        }
        value = value.replace(ANDROID_PLACE_HOLDER) { r ->
          r.value.dropLast(1).plus('@')
        }
      } catch (e: IndexOutOfBoundsException) {
        value = ""
      }
      if (value.isBlank()) {
        // skip if value is empty
        return@forEach
      }

      val line = "\"$k\" = \"$value\";\n"
      result.append(line)
    }
    return result.toString()
  }

  override fun validPlatform(platform: String): Boolean {
    return platform.equals("iOS", true) || platform.equals("Mobile", true)
  }
}