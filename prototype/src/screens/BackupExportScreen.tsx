import { Header } from '../components/Header'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

export function BackupExportScreen() {
  const { accounts, categories, transactions, settings } = useApp()
  const theme = getTheme(settings.theme)

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
    <div>
      <Header title="Backup и экспорт" backTo="/more" />
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
          <span style={{ fontSize: 20, marginRight: 10 }}>📦</span>
          Сохранить backup (JSON)
          <p style={{ margin: '6px 0 0 32px', fontSize: 12, color: theme.textMuted, fontWeight: 400 }}>
            Полная копия всех данных
          </p>
        </button>

        <button
          type="button"
          onClick={handleCsvExport}
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
          <span style={{ fontSize: 20, marginRight: 10 }}>📊</span>
          Экспорт CSV (30 дней)
          <p style={{ margin: '6px 0 0 32px', fontSize: 12, color: theme.textMuted, fontWeight: 400 }}>
            Бесплатно: последние 30 дней
          </p>
        </button>

        <div style={{ padding: 16, borderRadius: 12, background: theme.premiumBg, marginTop: 8 }}>
          <p style={{ margin: 0, fontSize: 13, fontWeight: 600, color: theme.premium }}>
            Premium: экспорт CSV без ограничений
          </p>
        </div>
      </div>
    </div>
  )
}
