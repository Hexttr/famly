import { Header } from '../components/Header'
import { PremiumGate } from '../components/PremiumGate'
import { useApp } from '../context/AppContext'
import { formatMoney, getCategorySpent } from '../data/mockData'
import { getTheme } from '../theme'

const months = [
  { label: 'Март', factor: 0.85 },
  { label: 'Апрель', factor: 0.92 },
  { label: 'Май', factor: 1.0 },
]

export function AdvancedReportsScreen() {
  const { categories, transactions, settings } = useApp()
  const theme = getTheme(settings.theme)

  const currentSpent = categories
    .filter((c) => c.type === 'expense')
    .reduce((s, c) => s + getCategorySpent(c.id, transactions), 0)

  const maxSpent = currentSpent

  return (
    <PremiumGate feature="Расширенная аналитика">
      <div>
        <Header title="Аналитика" backTo="/more" />
        <div style={{ padding: 16 }}>
          <h3 style={{ margin: '0 0 16px', fontSize: 15, color: theme.text }}>Тренд расходов (3 мес.)</h3>
          <div style={{ display: 'flex', alignItems: 'flex-end', gap: 16, height: 120, marginBottom: 24 }}>
            {months.map((m) => {
              const spent = Math.round(currentSpent * m.factor)
              const height = maxSpent > 0 ? (spent / maxSpent) * 100 : 0
              return (
                <div key={m.label} style={{ flex: 1, textAlign: 'center' }}>
                  <div
                    style={{
                      height: `${height}%`,
                      minHeight: 4,
                      background: theme.primary,
                      borderRadius: '4px 4px 0 0',
                      marginBottom: 8,
                    }}
                  />
                  <p style={{ margin: 0, fontSize: 11, color: theme.textMuted }}>{m.label}</p>
                  <p style={{ margin: '2px 0 0', fontSize: 11, fontWeight: 600, color: theme.text }}>
                    {formatMoney(spent)}
                  </p>
                </div>
              )
            })}
          </div>

          <h3 style={{ margin: '0 0 12px', fontSize: 15, color: theme.text }}>Сравнение периодов</h3>
          <div style={{ padding: 16, borderRadius: 12, background: theme.surface, border: `1px solid ${theme.border}` }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
              <span style={{ color: theme.textMuted }}>Текущий период</span>
              <span style={{ fontWeight: 600, color: theme.text }}>{formatMoney(currentSpent)}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: theme.textMuted }}>Прошлый период</span>
              <span style={{ fontWeight: 600, color: theme.textSecondary }}>
                {formatMoney(Math.round(currentSpent * 0.92))}
              </span>
            </div>
            <p style={{ margin: '12px 0 0', fontSize: 13, color: theme.expense }}>
              +8% к прошлому периоду
            </p>
          </div>
        </div>
      </div>
    </PremiumGate>
  )
}
