package com.famly.app.data.export

import com.famly.app.data.local.entity.CategoryEntity
import com.famly.app.data.local.entity.TransactionEntity
import com.famly.app.domain.MoneyFormatter
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream

object ExcelExporter {

    private val PRIMARY = byteArrayOf(0x2D.toByte(), 0x6A.toByte(), 0x4F.toByte())
    private val PRIMARY_DARK = byteArrayOf(0x1B.toByte(), 0x43.toByte(), 0x32.toByte())
    private val ROW_ALT = byteArrayOf(0xF1.toByte(), 0xF5.toByte(), 0xF3.toByte())
    private val BORDER = byteArrayOf(0xE2.toByte(), 0xE8.toByte(), 0xE5.toByte())
    private val MUTED = byteArrayOf(0x5C.toByte(), 0x65.toByte(), 0x60.toByte())
    private val EXPENSE = byteArrayOf(0xE6.toByte(), 0x39.toByte(), 0x46.toByte())
    private val TEXT = byteArrayOf(0x1A.toByte(), 0x1D.toByte(), 0x1A.toByte())

    private val HEADERS = arrayOf("Дата", "Тип", "Категория", "Сумма", "Заметка")

    fun export(
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        periodDescription: String,
    ): ByteArray {
        val categoryMap = categories.associateBy { it.id }
        val rows = transactions
            .sortedByDescending { it.dateEpochDay }
            .map { tx ->
                val cat = categoryMap[tx.categoryId]
                ExcelRow(
                    date = MoneyFormatter.formatShortDate(tx.dateEpochDay),
                    type = if (tx.type == "expense") "Расход" else "Доход",
                    category = cat?.name ?: "",
                    amountRubles = tx.amountKopecks / 100.0,
                    note = tx.note ?: "",
                    isExpense = tx.type == "expense",
                )
            }

        XSSFWorkbook().use { workbook ->
            workbook.properties.coreProperties.creator = "Мой (Наш) Бюджет"
            val sheet = workbook.createSheet("Операции")
            sheet.isDisplayGridlines = false

            val titleStyle = createFontStyle(workbook, 18, true, PRIMARY_DARK)
            val subtitleStyle = createFontStyle(workbook, 11, false, MUTED)
            val headerStyle = createHeaderStyle(workbook)
            val thinBorder = createThinBorder(workbook)

            sheet.createRow(0).heightInPoints = 24f
            sheet.createRow(1).heightInPoints = 24f
            sheet.createRow(2).heightInPoints = 24f
            sheet.addMergedRegion(CellRangeAddress(0, 2, 1, 4))
            sheet.getRow(0).createCell(1).apply {
                setCellValue("Мой (Наш) Бюджет")
                cellStyle = titleStyle
            }

            sheet.createRow(3).apply {
                heightInPoints = 20f
            }
            sheet.addMergedRegion(CellRangeAddress(3, 3, 1, 4))
            sheet.getRow(3).createCell(1).apply {
                setCellValue("Операции · $periodDescription")
                cellStyle = subtitleStyle
            }

            sheet.createRow(4).heightInPoints = 8f

            val headerRowIndex = 5
            val headerRow = sheet.createRow(headerRowIndex)
            headerRow.heightInPoints = 24f
            HEADERS.forEachIndexed { i, title ->
                headerRow.createCell(i).apply {
                    setCellValue(title)
                    cellStyle = headerStyle
                }
            }

            rows.forEachIndexed { index, row ->
                val excelRow = sheet.createRow(headerRowIndex + 1 + index)
                excelRow.heightInPoints = 20f
                val values = listOf(row.date, row.type, row.category, row.amountRubles, row.note)
                values.forEachIndexed { colIndex, value ->
                    val cell = excelRow.createCell(colIndex)
                    if (colIndex == 3) {
                        cell.setCellValue(row.amountRubles)
                    } else {
                        cell.setCellValue(value.toString())
                    }
                    cell.cellStyle = createDataStyle(
                        workbook,
                        thinBorder,
                        index % 2 == 1,
                        colIndex == 1 && row.isExpense,
                        colIndex == 3,
                    )
                }
            }

            val minWidths = intArrayOf(12, 10, 14, 11, 18)
            HEADERS.indices.forEach { colIndex ->
                val maxLen = maxOf(
                    HEADERS[colIndex].length,
                    rows.maxOfOrNull {
                        when (colIndex) {
                            0 -> it.date.length
                            1 -> it.type.length
                            2 -> it.category.length
                            3 -> it.amountRubles.toLong().toString().length
                            else -> it.note.length
                        }
                    } ?: 0,
                )
                val width = (maxLen + 2).coerceIn(minWidths[colIndex], if (colIndex == 4) 44 else 28)
                sheet.setColumnWidth(colIndex, width * 256)
            }

            sheet.setAutoFilter(CellRangeAddress(headerRowIndex, headerRowIndex + rows.size, 0, HEADERS.size - 1))
            sheet.createFreezePane(0, headerRowIndex + 1)

            return ByteArrayOutputStream().use { out ->
                workbook.write(out)
                out.toByteArray()
            }
        }
    }

    private data class ExcelRow(
        val date: String,
        val type: String,
        val category: String,
        val amountRubles: Double,
        val note: String,
        val isExpense: Boolean,
    )

    private fun createFontStyle(
        workbook: XSSFWorkbook,
        size: Int,
        bold: Boolean,
        rgb: ByteArray,
    ): XSSFCellStyle = workbook.createCellStyle().apply {
        val font = workbook.createFont()
        font.fontName = "Calibri"
        font.fontHeightInPoints = size.toShort()
        font.bold = bold
        font.setColor(XSSFColor(rgb, null))
        setFont(font)
        alignment = HorizontalAlignment.LEFT
        verticalAlignment = VerticalAlignment.CENTER
    }

    private fun createHeaderStyle(workbook: XSSFWorkbook): XSSFCellStyle =
        workbook.createCellStyle().apply {
            val font = workbook.createFont()
            font.fontName = "Calibri"
            font.fontHeightInPoints = 11
            font.bold = true
            font.color = IndexedColors.WHITE.index
            setFont(font)
            setFillForegroundColor(XSSFColor(PRIMARY, null))
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            setTopBorderColor(XSSFColor(PRIMARY_DARK, null))
            setBottomBorderColor(XSSFColor(PRIMARY_DARK, null))
            setLeftBorderColor(XSSFColor(PRIMARY_DARK, null))
            setRightBorderColor(XSSFColor(PRIMARY_DARK, null))
        }

    private fun createThinBorder(workbook: XSSFWorkbook): XSSFCellStyle =
        workbook.createCellStyle().apply {
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            setTopBorderColor(XSSFColor(BORDER, null))
            setBottomBorderColor(XSSFColor(BORDER, null))
            setLeftBorderColor(XSSFColor(BORDER, null))
            setRightBorderColor(XSSFColor(BORDER, null))
        }

    private fun createDataStyle(
        workbook: XSSFWorkbook,
        borderBase: XSSFCellStyle,
        alternate: Boolean,
        expenseType: Boolean,
        isAmount: Boolean,
    ): XSSFCellStyle = workbook.createCellStyle().apply {
        cloneStyleFrom(borderBase)
        val font = workbook.createFont()
        font.fontName = "Calibri"
        font.fontHeightInPoints = 11
        font.setColor(XSSFColor(if (expenseType) EXPENSE else TEXT, null))
        setFont(font)
        setFillForegroundColor(
            XSSFColor(if (alternate) ROW_ALT else byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()), null),
        )
        fillPattern = FillPatternType.SOLID_FOREGROUND
        alignment = if (isAmount) HorizontalAlignment.RIGHT else HorizontalAlignment.LEFT
        verticalAlignment = VerticalAlignment.CENTER
        wrapText = !isAmount
        if (isAmount) {
            dataFormat = workbook.createDataFormat().getFormat("# ##0")
        }
    }
}
