import { useParams } from 'react-router-dom'
import { Header } from '../components/Header'
import { PremiumGate } from '../components/PremiumGate'
import { useApp } from '../context/AppContext'
import { formatMoney } from '../data/mockData'
import { getTheme } from '../theme'

export function SplitExpenseScreen() {
  const { id } = useParams()
  const { transactions, categories, family, settings } = useApp()
  const theme = getTheme(settings.theme)

  const tx = transactions.find((t) => t.id === id)
  const cat = tx ? categories.find((c) => c.id === tx.categoryId) : null

  return (
    <PremiumGate feature="Split расходов">
      <div>
        <Header title="Разделить расход" backTo={tx ? `/operations/${tx.id}` : '/operations'} />
        {tx && (
          <div style={{ padding: 16 }}>
            <div style={{ textAlign: 'center', marginBottom: 24 }}>
              <span style={{ fontSize: 40 }}>{cat?.icon}</span>
              <p style={{ margin: '8px 0', fontSize: 24, fontWeight: 700, color: theme.expense }}>
                {formatMoney(tx.amount)}
              </p>
              <p style={{ margin: 0, color: theme.textSecondary }}>{cat?.name}</p>
            </div>

            <p style={{ fontSize: 13, color: theme.textMuted, marginBottom: 12 }}>Кто участвует?</p>
            {family.map((member) => (
              <label
                key={member.id}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 12,
                  padding: 14,
                  marginBottom: 8,
                  borderRadius: 12,
                  background: theme.surface,
                  border: `1px solid ${theme.border}`,
                  cursor: 'pointer',
                }}
              >
                <input type="checkbox" defaultChecked />
                <span style={{ fontSize: 24 }}>{member.avatar}</span>
                <span style={{ flex: 1, fontWeight: 500, color: theme.text }}>{member.name}</span>
                <span style={{ color: theme.textSecondary }}>
                  {formatMoney(Math.round(tx.amount / family.length))}
                </span>
              </label>
            ))}

            <button
              type="button"
              style={{
                width: '100%',
                marginTop: 16,
                padding: 16,
                border: 'none',
                borderRadius: 12,
                background: theme.primary,
                color: '#fff',
                fontWeight: 600,
                cursor: 'pointer',
              }}
            >
              Сохранить split
            </button>
          </div>
        )}
      </div>
    </PremiumGate>
  )
}
