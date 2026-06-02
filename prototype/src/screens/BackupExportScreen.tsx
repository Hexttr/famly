import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

export function BackupExportScreen() {
  const { accounts, categories, transactions, settings } = useApp()
  const theme = getTheme(settings.theme)

  useSetPageHeader({ showBack: true, backTo: '/more' })

  const handleBackup = () => {
    const data = JSON.stringify({ accounts, categories, transactions, settings }, null, 2)
    const blob = new Blob([data], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `famly-backup-${new Date().toISOString().slice(0, 10)}.json`
    a.click()
    URL.revokeObjectURL(url)
  }

  const handleCsvExport = () => {
    const header = 'date,type,category,amount,note\n'
    const rows = transactions
      .map((tx) => {
        const cat = categories.find((c) => c.id === tx.categoryId)
        return `${tx.date},${tx.type},${cat?.name ?? ''},${tx.amount},${tx.note ?? ''}`
      })
      .join('\n')
    const blob = new Blob([header + rows], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `famly-export-${new Date().toISOString().slice(0, 10)}.csv`
    a.click()
    URL.revokeObjectURL(url)
  }

  return (
    <div style={{ padding: 16 }}>
      <button
        type="button"
        onClick={handleBackup}
        style={{
          width: '100%',
          padding: 16,
          marginBottom: 12,
          borderRadius: 12,
          border: `1px solid ${theme.border}`,
          background: theme.surface,
          color: theme.text,
          fontWeight: 600,
          cursor: 'pointer',
          textAlign: 'left',
        }}
      >
        📦 Сохранить backup (JSON)
      </button>
      <button
        type="button"
        onClick={handleCsvExport}
        style={{
          width: '100%',
          padding: 16,
          borderRadius: 12,
          border: `1px solid ${theme.border}`,
          background: theme.surface,
          color: theme.text,
          fontWeight: 600,
          cursor: 'pointer',
          textAlign: 'left',
        }}
      >
        📊 Экспорт CSV (30 дней)
      </button>
    </div>
  )
}
