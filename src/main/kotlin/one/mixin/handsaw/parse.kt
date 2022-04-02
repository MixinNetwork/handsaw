package one.mixin.handsaw

import com.github.ajalt.clikt.core.InvalidFileFormat
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileInputStream

interface Parser {
  fun parse(fileName: String): ParseResult?
}

class XLSXParser : Parser {
  override fun parse(fileName: String): ParseResult {
    val sheet = readFromXLSXFile(fileName)

    var langList = mutableListOf<String>()
    val dataList = mutableMapOf<String, List<String>>()
    val platformMap = mutableMapOf<String, String>()

    val firstRow = sheet.getRow(0) ?: throw InvalidFileFormat(fileName, "The xlsx file has no row")
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
      r.cellIterator().forEach colLoop@{ c ->
        if (c == keyCol) return@colLoop

        val cellValue = c.stringCellValue
        val cellVal = if (cellValue.isNullOrBlank()) {
          ""
        } else {
          cellValue
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

data class ParseResult(
  val langList: List<String>,
  val dataList: Map<String, List<String>>,
  val platformMap: Map<String, String>,
)