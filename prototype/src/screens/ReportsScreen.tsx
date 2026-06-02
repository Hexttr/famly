import { Header } from '../components/Header'
import { useApp } from '../context/AppContext'
import { formatMoney, getCategorySpent } from '../data/mockData'
import { getTheme } from '../theme'

export function ReportsScreen() {
  const { categories, transactions, settings } = useApp()
  const theme = getTheme(settings.theme)

  const expenseCategories = categories
    .filter((c) => c.type === 'expense')
    .map((c) => ({ ...c, spent: getCategorySpent(c.id, transactions) }))
    .sort((a, b) => b.spent - a.spent)

  const totalSpent = expenseCategories.reduce((s, c) => s + c.spent, 0)
  const top5 = expenseCategories.slice(0, 5)

  const colors = ['#E63946', '#457B9D', '#F4A261', '#6D597A', '#E76F51']

  return (
    <div>
      <Header title="Отчёты" backTo="/more" />
      <div style={{ padding: 16 }}>
        <p style={{ margin: '0 0 16px', color: theme.textSecondary, fontSize: 14 }}>
          Расходы за текущий период · {formatMoney(totalSpent)}
        </p>

        <div
          style={{
            width: 160,
            height: 160,
            borderRadius: '50%',
            margin: '0 auto 24px',
            background: `conic-gradient(${top5
              .map((_, i) => {
                const start = top5.slice(0, i).reduce((s, x) => s + x.spent, 0) / totalSpent
                const end = top5.slice(0, i + 1).reduce((s, x) => s + x.spent, 0) / totalSpent
                return `${colors[i]} ${start * 360}deg ${end * 360}deg`
              })
              .join(', ')})`,
          }}
        />

        <h3 style={{ margin: '0 0 12px', fontSize: 15, color: theme.text }}>Топ-5 категорий</h3>
        {top5.map((cat, i) => {
          const pct = totalSpent > 0 ? Math.round((cat.spent / totalSpent) * 100) : 0
          return (
            <div
              key={cat.id}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 12,
                padding: '12px 0',
                borderBottom: `1px solid ${theme.border}`,
              }}
            >
              <div
                style={{
                  width: 12,
                  height: 12,
                  borderRadius: 6,
                  background: colors[i],
                }}
              />
              <span style={{ fontSize: 20 }}>{cat.icon}</span>
              <span style={{ flex: 1, color: theme.text }}>{cat.name}</span>
              <span style={{ fontWeight: 600, color: theme.text }}>{formatMoney(cat.spent)}</span>
              <span style={{ fontSize: 12, color: theme.textMuted, width: 32, textAlign: 'right' }}>
                {pct}%
              </span>
            </div>
          )
        })}

        <div style={{ marginTop: 24, padding: 16, borderRadius: 12, background: theme.premiumBg }}>
          <p style={{ margin: 0, fontSize: 13, fontWeight: 600, color: theme.premium }}>
            Premium: расширенная аналитика
          </p>
          <p style={{ margin: '4px 0 0', fontSize: 12, color: theme.textSecondary }}>
            Сравнение 3/6/12 месяцев и тренды
          </p>
        </div>
      </div>
    </div>
  )
}
