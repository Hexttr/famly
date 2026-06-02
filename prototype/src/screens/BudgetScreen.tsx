import { Link } from 'react-router-dom'
import { Header } from '../components/Header'
import { ProgressBar } from '../components/ProgressBar'
import { useApp } from '../context/AppContext'
import { formatMoney, getCategorySpent, getBudgetTotal, getTotalExpenses } from '../data/mockData'
import { getTheme } from '../theme'

export function BudgetScreen() {
  const { categories, transactions, settings } = useApp()
  const theme = getTheme(settings.theme)

  const expenseCategories = categories.filter((c) => c.type === 'expense' && c.budgetLimit)
  const totalSpent = getTotalExpenses(transactions)
  const totalBudget = getBudgetTotal(categories)

  return (
    <div>
      <Header
        title="Бюджет"
        action={
          <Link to="/categories" style={{ fontSize: 13, color: theme.primary, textDecoration: 'none' }}>
            Категории
          </Link>
        }
      />

      <div style={{ margin: 16, padding: 16, borderRadius: 12, background: theme.surface, border: `1px solid ${theme.border}` }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
          <span style={{ color: theme.textMuted, fontSize: 13 }}>Общий бюджет периода</span>
          <span style={{ fontWeight: 600, color: theme.text }}>{formatMoney(totalBudget)}</span>
        </div>
        <ProgressBar spent={totalSpent} limit={totalBudget} />
        <p style={{ margin: '8px 0 0', fontSize: 13, color: theme.textSecondary }}>
          Потрачено {formatMoney(totalSpent)} из {formatMoney(totalBudget)}
        </p>
      </div>

      <div style={{ padding: '0 16px' }}>
        {expenseCategories.map((cat) => {
          const spent = getCategorySpent(cat.id, transactions)
          const limit = cat.budgetLimit ?? 0
          const pct = limit > 0 ? Math.round((spent / limit) * 100) : 0
          return (
            <Link
              key={cat.id}
              to={`/budget/${cat.id}`}
              style={{
                display: 'block',
                padding: 14,
                marginBottom: 8,
                borderRadius: 12,
                background: theme.surface,
                border: `1px solid ${theme.border}`,
                textDecoration: 'none',
                color: theme.text,
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 8 }}>
                <span style={{ fontSize: 22 }}>{cat.icon}</span>
                <span style={{ flex: 1, fontWeight: 600 }}>{cat.name}</span>
                <span style={{ fontSize: 13, color: pct >= 100 ? theme.expense : theme.textSecondary }}>
                  {pct}%
                </span>
              </div>
              <ProgressBar spent={spent} limit={limit} color={cat.color} />
              <p style={{ margin: '6px 0 0', fontSize: 12, color: theme.textMuted }}>
                {formatMoney(spent)} / {formatMoney(limit)}
              </p>
            </Link>
          )
        })}
      </div>
    </div>
  )
}
