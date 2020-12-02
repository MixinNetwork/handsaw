import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileInputStream

interface Parser {
  fun parse(fileName: String): ParseResult?
}

class XLSXParser : Parser {

  override fun parse(fileName: String): ParseResult? {
    val sheet = readFromXLSXFile(fileName)

    var langList = mutableListOf<String>()
    val dataList = mutableMapOf<String, List<String>>()

    val firstRow = sheet.getRow(0) ?: return null
    val langCol = mutableListOf<Int>()
    firstRow.cellIterator().forEach { c ->
      val cellValue = c.stringCellValue
      if (cellValue.isNullOrBlank()) return@forEach

      langCol.add(c.columnIndex)
      langList.add(c.stringCellValue)
    }

    langList = name2ISO(langList.drop(1).toMutableList())

    sheet.rowIterator().forEach rowLoop@{ r ->
      if (r == firstRow) return@rowLoop
      val firstCol = r.getCell(0)
      val firstColString = firstCol.stringCellValue
      if (firstColString.isNullOrBlank()) return@rowLoop

      val rowList = mutableListOf<String>()
      r.cellIterator().forEach colLoop@{ c ->
        if (c == firstCol) return@colLoop

        val cellValue = c.stringCellValue
        rowList.add(if (cellValue.isNullOrBlank()) {
          ""
        } else {
          cellValue
        })
      }
      dataList[firstColString] = rowList
    }
    return ParseResult(langList, dataList)
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
          "英文" -> "en"
         else -> ""
      }
    }
    return langList
  }
}

data class ParseResult(
    val langList: List<String>,
    val dataList: Map<String, List<String>>
)