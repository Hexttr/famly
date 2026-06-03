import { formatShortDate } from '../data/mockData'
import type { Category, Transaction } from '../types'

const PRIMARY = 'FF2D6A4F'
const PRIMARY_DARK = 'FF1B4332'
const HEADER_TEXT = 'FFFFFFFF'
const ROW_ALT = 'FFF1F5F3'
const BORDER = 'FFE2E8E5'
const MUTED = 'FF5C6560'
const EXPENSE = 'FFE63946'

const HEADERS = ['Дата', 'Тип', 'Категория', 'Сумма', 'Заметка'] as const

const thinBorder = {
  top: { style: 'thin' as const, color: { argb: BORDER } },
  left: { style: 'thin' as const, color: { argb: BORDER } },
  bottom: { style: 'thin' as const, color: { argb: BORDER } },
  right: { style: 'thin' as const, color: { argb: BORDER } },
}

async function fetchLogoBase64(): Promise<string> {
  const res = await fetch('/logo.png')
  const blob = await res.blob()
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const dataUrl = reader.result as string
      resolve(dataUrl.split(',')[1] ?? '')
    }
    reader.onerror = reject
    reader.readAsDataURL(blob)
  })
}

function columnWidth(text: string, min = 10, max = 42): number {
  return Math.min(Math.max(text.length + 2, min), max)
}

export async function exportOperationsToExcel(options: {
  transactions: Transaction[]
  categories: Category[]
  periodDescription: string
}): Promise<ArrayBuffer> {
  const { default: ExcelJS } = await import('exceljs')
  const { transactions, categories, periodDescription } = options

  const rows = transactions.map((tx) => {
    const cat = categories.find((c) => c.id === tx.categoryId)
    return {
      date: formatShortDate(tx.date),
      type: tx.type === 'expense' ? 'Расход' : 'Доход',
      category: cat?.name ?? '',
      amount: tx.amount,
      note: tx.note ?? '',
      isExpense: tx.type === 'expense',
    }
  })

  const workbook = new ExcelJS.Workbook()
  workbook.creator = 'Мой (Наш) Бюджет'
  workbook.created = new Date()

  const sheet = workbook.addWorksheet('Операции', {
    views: [{ showGridLines: false }],
  })

  sheet.properties.defaultRowHeight = 18

  const logoBase64 = await fetchLogoBase64()
  if (logoBase64) {
    const imageId = workbook.addImage({ base64: logoBase64, extension: 'png' })
    sheet.addImage(imageId, {
      tl: { col: 0.2, row: 0.2 },
      ext: { width: 68, height: 68 },
    })
  }

  sheet.getRow(1).height = 24
  sheet.getRow(2).height = 24
  sheet.getRow(3).height = 24

  sheet.mergeCells('B1:E3')
  const titleCell = sheet.getCell('B1')
  titleCell.value = 'Мой (Наш) Бюджет'
  titleCell.font = { name: 'Calibri', size: 18, bold: true, color: { argb: PRIMARY_DARK } }
  titleCell.alignment = { vertical: 'middle', horizontal: 'left' }

  sheet.mergeCells('B4:E4')
  const subtitleCell = sheet.getCell('B4')
  subtitleCell.value = `Операции · ${periodDescription}`
  subtitleCell.font = { name: 'Calibri', size: 11, color: { argb: MUTED } }
  subtitleCell.alignment = { vertical: 'middle', horizontal: 'left' }

  sheet.getRow(5).height = 8

  const headerRowIndex = 6
  const headerRow = sheet.getRow(headerRowIndex)
  headerRow.height = 24
  HEADERS.forEach((title, i) => {
    const cell = headerRow.getCell(i + 1)
    cell.value = title
    cell.font = { name: 'Calibri', size: 11, bold: true, color: { argb: HEADER_TEXT } }
    cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: PRIMARY } }
    cell.alignment = { vertical: 'middle', horizontal: 'center' }
    cell.border = {
      top: { style: 'thin', color: { argb: PRIMARY_DARK } },
      left: { style: 'thin', color: { argb: PRIMARY_DARK } },
      bottom: { style: 'thin', color: { argb: PRIMARY_DARK } },
      right: { style: 'thin', color: { argb: PRIMARY_DARK } },
    }
  })

  rows.forEach((row, index) => {
    const excelRow = sheet.getRow(headerRowIndex + 1 + index)
    excelRow.height = 20

    const values = [row.date, row.type, row.category, row.amount, row.note]
    values.forEach((value, colIndex) => {
      const cell = excelRow.getCell(colIndex + 1)
      cell.value = value
      cell.font = {
        name: 'Calibri',
        size: 11,
        color: { argb: colIndex === 1 && row.isExpense ? EXPENSE : 'FF1A1D1A' },
      }
      cell.fill = {
        type: 'pattern',
        pattern: 'solid',
        fgColor: { argb: index % 2 === 1 ? ROW_ALT : 'FFFFFFFF' },
      }
      cell.alignment = {
        vertical: 'middle',
        horizontal: colIndex === 3 ? 'right' : 'left',
        wrapText: colIndex === 4,
      }
      cell.border = thinBorder

      if (colIndex === 3) {
        cell.numFmt = '# ##0'
      }
    })
  })

  const widthSamples: string[][] = [HEADERS as unknown as string[]]
  rows.forEach((row) => {
    widthSamples.push([
      row.date,
      row.type,
      row.category,
      String(row.amount),
      row.note,
    ])
  })

  sheet.columns = HEADERS.map((_, colIndex) => {
    const maxLen = Math.max(...widthSamples.map((sample) => (sample[colIndex] ?? '').length))
    const minWidths = [12, 10, 14, 11, 18]
    return {
      width: columnWidth(''.padEnd(maxLen), minWidths[colIndex], colIndex === 4 ? 44 : 28),
    }
  })

  sheet.autoFilter = {
    from: { row: headerRowIndex, column: 1 },
    to: { row: headerRowIndex + rows.length, column: HEADERS.length },
  }

  sheet.views = [{ state: 'frozen', ySplit: headerRowIndex, showGridLines: false }]

  return workbook.xlsx.writeBuffer()
}
