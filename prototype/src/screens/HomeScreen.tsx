import { Link } from 'react-router-dom'
import { PremiumBadge } from '../components/PremiumGate'
import { ProgressBar } from '../components/ProgressBar'
import { useApp } from '../context/AppContext'
import {
  formatMoney,
  getBudgetTotal,
  getTotalExpenses,
  getTotalIncome,
} from '../data/mockData'
import { getTheme } from '../theme'

export function HomeScreen() {
  const { transactions, categories, settings } = useApp()
  const theme = getTheme(settings.theme)

  const spent = getTotalExpenses(transactions)
  const income = getTotalIncome(transactions)
  const budgetTotal = getBudgetTotal(categories)
  const remaining = budgetTotal - spent
  const daysLeft = 26

  const recent = transactions.slice(0, 5)

  return (
    <div>
      <div style={{ padding: '16px 16px 8px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <p style={{ margin: 0, fontSize: 13, color: theme.textMuted }}>Famly</p>
          <h1 style={{ margin: 0, fontSize: 22, color: theme.text }}>Привет! 👋</h1>
        </div>
        <PremiumBadge />
      </div>

      <div
        style={{
          margin: 16,
          padding: 20,
          borderRadius: 16,
          background: theme.primary,
          color: '#fff',
        }}
      >
        <p style={{ margin: '0 0 4px', fontSize: 13, opacity: 0.85 }}>Можно тратить</p>
        <p style={{ margin: '0 0 16px', fontSize: 36, fontWeight: 700 }}>
          {formatMoney(Math.max(0, remaining))}
        </p>
        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, opacity: 0.9 }}>
          <span>Потрачено: {formatMoney(spent)}</span>
          <span>До конца: {daysLeft} дн.</span>
        </div>
        <div style={{ marginTop: 12 }}>
          <ProgressBar spent={spent} limit={budgetTotal} color="#fff" />
        </div>
      </div>

      <div style={{ display: 'flex', gap: 12, padding: '0 16px', marginBottom: 16 }}>
        <div style={{ flex: 1, padding: 14, borderRadius: 12, background: theme.surface, border: `1px solid ${theme.border}` }}>
          <p style={{ margin: 0, fontSize: 12, color: theme.textMuted }}>Доходы</p>
          <p style={{ margin: '4px 0 0', fontSize: 17, fontWeight: 600, color: theme.income }}>{formatMoney(income)}</p>
        </div>
        <div style={{ flex: 1, padding: 14, borderRadius: 12, background: theme.surface, border: `1px solid ${theme.border}` }}>
          <p style={{ margin: 0, fontSize: 12, color: theme.textMuted }}>Расходы</p>
          <p style={{ margin: '4px 0 0', fontSize: 17, fontWeight: 600, color: theme.expense }}>{formatMoney(spent)}</p>
        </div>
      </div>

      <div style={{ padding: '0 16px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
          <h2 style={{ margin: 0, fontSize: 16, color: theme.text }}>Последние операции</h2>
          <Link to="/operations" style={{ fontSize: 13, color: theme.primary, textDecoration: 'none' }}>
            Все →
          </Link>
        </div>

        {recent.map((tx) => {
          const cat = categories.find((c) => c.id === tx.categoryId)
          return (
            <Link
              key={tx.id}
              to={`/operations/${tx.id}`}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 12,
                padding: '12px 0',
                borderBottom: `1px solid ${theme.border}`,
                textDecoration: 'none',
                color: theme.text,
              }}
            >
              <span style={{ fontSize: 24 }}>{cat?.icon ?? '📝'}</span>
              <div style={{ flex: 1 }}>
                <p style={{ margin: 0, fontSize: 15, fontWeight: 500 }}>{cat?.name ?? '—'}</p>
                <p style={{ margin: 0, fontSize: 12, color: theme.textMuted }}>{tx.note ?? tx.date}</p>
              </div>
              <span
                style={{
                  fontWeight: 600,
                  color: tx.type === 'expense' ? theme.expense : theme.income,
                }}
              >
                {tx.type === 'expense' ? '−' : '+'}
                {formatMoney(tx.amount)}
              </span>
            </Link>
          )
        })}
      </div>
    </div>
  )
}
