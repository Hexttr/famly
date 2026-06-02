import { useParams } from 'react-router-dom'
import { Header } from '../components/Header'
import { ProgressBar } from '../components/ProgressBar'
import { useApp } from '../context/AppContext'
import { formatMoney, getCategorySpent } from '../data/mockData'
import { getTheme } from '../theme'

export function CategoryBudgetEditScreen() {
  const { id } = useParams()
  const { categories, transactions, updateCategory, settings } = useApp()
  const theme = getTheme(settings.theme)

  const cat = categories.find((c) => c.id === id)
  if (!cat) return null

  const spent = getCategorySpent(cat.id, transactions)

  return (
    <div>
      <Header title={cat.name} backTo="/budget" />
      <div style={{ padding: 24, textAlign: 'center' }}>
        <span style={{ fontSize: 48 }}>{cat.icon}</span>
      </div>

      <div style={{ padding: '0 16px' }}>
        <label style={{ display: 'block', marginBottom: 8, fontSize: 13, color: theme.textSecondary }}>
          Лимит бюджета (₽)
        </label>
        <input
          type="number"
          value={cat.budgetLimit ?? ''}
          onChange={(e) => updateCategory(cat.id, { budgetLimit: Number(e.target.value) || 0 })}
          style={{
            width: '100%',
            padding: 14,
            borderRadius: 10,
            border: `1px solid ${theme.border}`,
            fontSize: 18,
            boxSizing: 'border-box',
            background: theme.surface,
            color: theme.text,
          }}
        />

        <div style={{ marginTop: 20 }}>
          <p style={{ fontSize: 13, color: theme.textMuted, marginBottom: 8 }}>Потрачено в периоде</p>
          <ProgressBar spent={spent} limit={cat.budgetLimit ?? 0} color={cat.color} height={10} />
          <p style={{ marginTop: 8, color: theme.textSecondary }}>
            {formatMoney(spent)} из {formatMoney(cat.budgetLimit ?? 0)}
          </p>
        </div>

        <div style={{ marginTop: 24, padding: 16, borderRadius: 12, background: theme.premiumBg }}>
          <p style={{ margin: 0, fontSize: 13, color: theme.premium, fontWeight: 600 }}>
            Premium: Rollover
          </p>
          <p style={{ margin: '4px 0 0', fontSize: 12, color: theme.textSecondary }}>
            Перенос неиспользованного бюджета на следующий период
          </p>
        </div>
      </div>
    </div>
  )
}
