import { useMemo, useState } from 'react'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import {
  REPORT_PERIODS,
  filterTransactionsByPeriod,
  getReportPeriodDescription,
  type ReportPeriod,
} from '../data/mockData'
import { getTheme, radius, shadows } from '../theme'

function downloadBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

function fileDateSuffix() {
  return new Date().toISOString().slice(0, 10)
}

export function BackupExportScreen() {
  const { accounts, categories, transactions, settings } = useApp()
  const theme = getTheme(settings.theme)
  const [period, setPeriod] = useState<ReportPeriod>('month')
  const [exporting, setExporting] = useState(false)

  useSetPageHeader({ showBack: true, backTo: '/more' })

  const filteredTx = useMemo(
    () => filterTransactionsByPeriod(transactions, period),
    [transactions, period],
  )

  const periodDescription = getReportPeriodDescription(period)
  const periodSubtitle = `${periodDescription} · ${filteredTx.length} операций`

  const handleBackup = () => {
    const data = JSON.stringify({ accounts, categories, transactions, settings }, null, 2)
    downloadBlob(new Blob([data], { type: 'application/json' }), `famly-backup-${fileDateSuffix()}.json`)
  }

  const handleCsvExport = () => {
    const header = 'date;type;category;amount;note\n'
    const rows = filteredTx
      .map((tx) => {
        const cat = categories.find((c) => c.id === tx.categoryId)
        const note = (tx.note ?? '').replace(/"/g, '""')
        return `${tx.date};${tx.type};${cat?.name ?? ''};${tx.amount};"${note}"`
      })
      .join('\n')
    downloadBlob(
      new Blob(['\uFEFF' + header + rows], { type: 'text/csv;charset=utf-8' }),
      `famly-export-${fileDateSuffix()}.csv`,
    )
  }

  const handleExcelExport = async () => {
    if (exporting) return
    setExporting(true)
    try {
      const { exportOperationsToExcel } = await import('../utils/exportOperationsExcel')
      const buffer = await exportOperationsToExcel({
        transactions: filteredTx,
        categories,
        periodDescription,
      })
      downloadBlob(
        new Blob([buffer], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        }),
        `famly-export-${fileDateSuffix()}.xlsx`,
      )
    } finally {
      setExporting(false)
    }
  }

  const options = [
    {
      icon: '📦',
      title: 'Сохранить backup (JSON)',
      subtitle: 'Все счета, категории и операции',
      onClick: handleBackup,
    },
    {
      icon: '📄',
      title: 'Экспорт CSV',
      subtitle: periodSubtitle,
      onClick: handleCsvExport,
    },
    {
      icon: '📗',
      title: 'Таблица Excel (.xlsx)',
      subtitle: exporting ? 'Формируем файл…' : periodSubtitle,
      onClick: () => void handleExcelExport(),
      disabled: exporting,
    },
  ]

  return (
    <div style={{ padding: '0 16px 24px' }}>
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(4, 1fr)',
          gap: 8,
          marginBottom: 16,
        }}
      >
        {REPORT_PERIODS.map(({ id, label }) => {
          const active = period === id
          return (
            <button
              key={id}
              type="button"
              onClick={() => setPeriod(id)}
              style={{
                padding: '10px 4px',
                borderRadius: radius.md,
                border: `2px solid ${active ? theme.primary : `${theme.primary}45`}`,
                background: active ? theme.primary : theme.surface,
                color: active ? '#fff' : theme.textSecondary,
                fontSize: 12,
                fontWeight: 700,
                cursor: 'pointer',
                boxShadow: active ? 'none' : shadows.sm,
              }}
            >
              {label}
            </button>
          )
        })}
      </div>

      {options.map((opt) => (
        <button
          key={opt.title}
          type="button"
          onClick={opt.onClick}
          disabled={'disabled' in opt && opt.disabled}
          style={{
            width: '100%',
            display: 'flex',
            alignItems: 'center',
            gap: 14,
            padding: '14px 16px',
            marginBottom: 10,
            borderRadius: radius.md,
            border: `2px solid ${theme.primary}45`,
            background: theme.surface,
            boxShadow: shadows.card,
            color: theme.text,
            cursor: opt.disabled ? 'wait' : 'pointer',
            textAlign: 'left',
            opacity: opt.disabled ? 0.7 : 1,
          }}
        >
          <span style={{ fontSize: 26, width: 40, textAlign: 'center', lineHeight: 1, flexShrink: 0 }}>
            {opt.icon}
          </span>
          <span style={{ flex: 1, minWidth: 0 }}>
            <span style={{ display: 'block', fontWeight: 700, fontSize: 15 }}>{opt.title}</span>
            <span style={{ display: 'block', marginTop: 4, fontSize: 13, color: theme.textMuted }}>
              {opt.subtitle}
            </span>
          </span>
          <span style={{ color: theme.textMuted, fontSize: 18, flexShrink: 0 }}>›</span>
        </button>
      ))}

      <p style={{ margin: '8px 0 0', fontSize: 14, lineHeight: 1.5, color: theme.textSecondary, textAlign: 'center' }}>
        Выгрузите данные для резервной копии или анализа в таблице.
      </p>
    </div>
  )
}
