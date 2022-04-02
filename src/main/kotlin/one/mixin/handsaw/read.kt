package one.mixin.handsaw

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.FileNotFound
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import javax.xml.parsers.DocumentBuilderFactory

interface Reader {
  fun read(dirName: String, outputDir: String?)
}

class AndroidReader : Reader {
  override fun read(dirName: String, outputDir: String?) {
    val dir = File(dirName)
    if (!dir.exists()) {
      throw FileNotFound(dirName)
    }
    val stringFiles = mutableListOf<File>()
    dir.listFiles { _, name ->
      name.startsWith("values")
    }?.sorted()?.forEach { f ->
      stringFiles.addAll(
        f.listFiles { _, name ->
          name.equals("strings.xml", true)
        }?.toList() ?: throw CliktError("no strings.xml found")
      )
    }

    val path = if (outputDir.isNullOrBlank()) {
      System.getProperty("user.dir")
    } else {
      outputDir
    }
    val outDir = File("$path${File.separator}output")
    if (outDir.exists()) {
      outDir.delete()
    }
    outDir.mkdirs()
    val outputFile = File("$outDir${File.separator}client.xlsx")
    if (outputFile.exists()) {
      outputFile.delete()
    }
    outputFile.createNewFile()

    val xssfWorkbook = XSSFWorkbook()
    val sheet = xssfWorkbook.createSheet("Android string")
    val headerRow = sheet.createRow(0)
    val column = stringFiles.size + 2
    for (i in 0 until column) {
      val cell = headerRow.createCell(i)
      when (i) {
        0 -> {
          cell.setCellValue("platform")
        }
        1 -> {
          cell.setCellValue("key")
        }
        else -> {
          var fileName = stringFiles[i - 2].parentFile.name.drop(6)
          fileName = if (fileName.startsWith('-', false)) {
            fileName.drop(1)
              .take(2)
          } else {
            "en"
          }
          cell.setCellValue(getIOSDesc(fileName))
        }
      }
    }

    val enFile = stringFiles[0]
    val enKeyList = mutableListOf<String>()
    val factory = DocumentBuilderFactory.newInstance()
    val builder =  factory.newDocumentBuilder()
    val doc = builder.parse(enFile)
    doc.documentElement.normalize()

    val stringNodes = doc.getElementsByTagName("string")
    for (i in 0 until stringNodes.length) {
      val node = stringNodes.item(i)
      val key = node.attributes.getNamedItem("name").nodeValue
      enKeyList.add(key)
      var value = node.firstChild.nodeValue
      val row = sheet.createRow(i + 1)
      val platformCell = row.createCell(0)
      platformCell.setCellValue("Mobile")
      val keyCell = row.createCell(1) // skip platform
      keyCell.setCellValue(key)
      val enCell = row.createCell(2) // skip platform + key
      value = escape(value)
      enCell.setCellValue(value)
    }

    for (i in 1 until stringFiles.size) {
      val curFile = stringFiles[i]
      val curDoc = builder.parse(curFile)
      curDoc.documentElement.normalize()
      val curStringNodes = curDoc.getElementsByTagName("string")
      for (j in 0 until curStringNodes.length) {
        val node = curStringNodes.item(j)
        val key = node.attributes.getNamedItem("name").nodeValue
        var value = node.firstChild.nodeValue
        val index = enKeyList.indexOf(key)
        val curRow = sheet.getRow(index + 1)
        val curCell = curRow.createCell(i + 2) // skip platform + key
        value = escape(value)
        curCell.setCellValue(value)
      }
    }

    FileOutputStream(outputFile).use { out ->
      xssfWorkbook.use { xw ->
        xw.write(out)
      }
    }
  }

  private fun escape(text: String): String {
    var value = text
    if ("\\\'" in value) {
      value = value.replace("\\\'", "\'")
    }
    if ("\\\"" in value) {
      value = value.replace("\\\"", "\"")
    }
    if ("&amp;" in value) {
      value = value.replace("&amp;", "&")
    }
    return value
  }

  private fun getIOSDesc(ios: String) : String {
    return when (ios) {
      "en" -> "英文"
      "zh" -> "中文"
      "ja" -> "日文"
      "ms" -> "马来西亚"
      "in" -> "印尼"
      else -> ios
    }
  }
}