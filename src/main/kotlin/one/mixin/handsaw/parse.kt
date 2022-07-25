package one.mixin.handsaw

import com.github.ajalt.clikt.core.InvalidFileFormat
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.io.FileInputStream
import java.io.StringWriter
import java.text.Collator
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

interface Parser {
  fun parse(path: String): ParseResult
}

class XLSXParser : Parser {
  override fun parse(path: String): ParseResult {
    val sheet = readFromXLSXFile(path)

    var langList = mutableListOf<String>()
    val dataList = sortedMapOf<String, MutableList<String>>(Collator.getInstance())
    val platformMap = mutableMapOf<String, String>()

    val firstRow = sheet.getRow(0) ?: throw InvalidFileFormat(path, "The xlsx file has no row")
    val langCol = mutableListOf<Int>()
    firstRow.cellIterator().forEach { c ->
      val cellValue = c.stringCellValue
      if (cellValue.isNullOrBlank()) return@forEach

      langCol.add(c.columnIndex)
      langList.add(c.stringCellValue)
    }

    langList = name2ISO(langList.drop(2) // skip platform + key
      .toMutableList())

    sheet.rowIterator().forEach rowLoop@{ r ->
      if (r == firstRow) return@rowLoop
      val platformCol = r.getCell(0) ?: return@rowLoop
      val keyCol = r.getCell(1) ?: return@rowLoop
      val keyColString = keyCol.stringCellValue
      if (keyColString.isNullOrBlank()) return@rowLoop

      val rowList = mutableListOf<String>()
      for (i in 0 until r.lastCellNum) {
        val c = r.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
        if (c == keyCol) continue

        val cellVal = if (c == null) {
          ""
        } else {
          val cellValue = c.stringCellValue
          if (cellValue.isNullOrBlank()) {
            ""
          } else {
            cellValue
          }
        }
        if (c == platformCol) {
          platformMap[keyColString] = cellVal
        } else {
          rowList.add(cellVal)
        }
      }
      dataList[keyColString] = rowList
    }
    return ParseResult(langList, dataList, platformMap)
  }

  private fun readFromXLSXFile(filepath: String): Sheet {
    val inputStream = FileInputStream(filepath)
    val xlWb = WorkbookFactory.create(inputStream)
    return xlWb.getSheetAt(0)
  }

  private fun name2ISO(langList: MutableList<String>): MutableList<String> {
    langList.forEachIndexed { i, l ->
      langList[i] = when (l) {
        "中文" -> "zh"
        "日文" -> "ja"
        "印尼" -> "in"
        "马来西亚" -> "ms"
        "英文" -> "en"
        else -> l
      }
    }
    return langList
  }
}

private val iSORegex = "^[a-z]{2}(-[A-Z]{2})?\$".toRegex()

class XMLParser : Parser {
  override fun parse(path: String): ParseResult {
    val dir = File(path)
    if (!dir.exists()) throw ParseException("Directory $path not exits!")

    val files = dir.listFiles { f -> f.extension == "xml" && iSORegex.matches(f.nameWithoutExtension) }
    if (files.isNullOrEmpty()) throw ParseException("No valid files under directory $path!")

    val enIndex = files.indexOfFirst { f -> f.nameWithoutExtension == "en" }
    if (enIndex < 0) throw ParseException("No en.xml under directory $path!")

    val dbf = DocumentBuilderFactory.newInstance().apply {
      setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
    }

    val langList = mutableListOf<String>()
    val dataList = sortedMapOf<String, MutableList<String>>(Collator.getInstance())
    val platformMap = mutableMapOf<String, String>()

    val mutableFiles = files.toMutableList()
    val enFile = mutableFiles.removeAt(enIndex)
    langList.add("en")
    val enKv = mutableMapOf<String, String>()

    val enDoc = dbf.newDocumentBuilder().parse(enFile)
    enDoc.documentElement.normalize()

    val enStringList = enDoc.getElementsByTagName("string")
    for (i in 0 until enStringList.length) {
      val node = enStringList.item(i)
      if (node.nodeType != Node.ELEMENT_NODE) continue

      val element = node as Element
      val name = element.getAttribute("name")
      val platform = element.getAttribute("platform")
      platformMap[name] = platform

      val childLen = element.childNodes.length
      val value = if (childLen > 1) {
        val sb = StringBuilder()
        for (j in 0 until childLen) {
          val childNode = element.childNodes.item(j)
          if (childNode.nodeType == Node.ELEMENT_NODE) {
            sb.append(getHTMLContent(dbf, childNode))
          } else if (childNode.nodeType == Node.TEXT_NODE) {
            sb.append(childNode.textContent)
          }
        }
        sb.toString()
      } else {
        element.textContent
      }

      val rowList = mutableListOf<String>()
      dataList[name] = rowList
      rowList.add(value)
      enKv[name] = value
    }

    mutableFiles.forEach { f ->
      val lang = f.nameWithoutExtension
      langList.add(lang)

      val doc = dbf.newDocumentBuilder().parse(f)
      doc.documentElement.normalize()

      val stringList = doc.getElementsByTagName("string")
      val kv = mutableMapOf<String, String>()
      for (i in 0 until stringList.length) {
        val node = stringList.item(i)
        if (node.nodeType != Node.ELEMENT_NODE) continue

        val element = node as Element
        val name = element.getAttribute("name")
        val value = element.textContent
        kv[name] = value
      }

      enKv.forEach en@{ k, _ ->
        val rowList = dataList[k] ?: return@en

        val curVal = kv[k]
        rowList.add(if (curVal.isNullOrBlank()) "" else curVal)
      }
    }

    return ParseResult(langList, dataList, platformMap)
  }

  private fun getHTMLContent(factory: DocumentBuilderFactory, item: Node): String {
    val builder = factory.newDocumentBuilder()
    val newDocument: Document = builder.newDocument()
    val importedNode: Node = newDocument.importNode(item, true)
    newDocument.appendChild(importedNode)
    val transformer: Transformer = TransformerFactory.newInstance().newTransformer()
    transformer.setOutputProperty(OutputKeys.METHOD, "html")
    val result = StreamResult(StringWriter())
    val source = DOMSource(newDocument)
    transformer.transform(source, result)
    return result.writer.toString()
  }
}

data class ParseResult(
  val langList: List<String>,
  val dataList: Map<String, List<String>>,
  val platformMap: Map<String, String>,
)

fun getParser(file: File): Parser = if (file.isDirectory) {
  XMLParser()
} else if (file.isFile && file.extension == "xlsx") {
  XLSXParser()
} else {
  throw ParseException("No supported parser for file ${file.name}")
}

data class ParseException(val msg: String): Exception(msg)