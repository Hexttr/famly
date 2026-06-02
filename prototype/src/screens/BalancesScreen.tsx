import { PremiumGate } from '../components/PremiumGate'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { formatMoney } from '../data/mockData'
import { getTheme } from '../theme'

export function BalancesScreen() {
  const { family, iouBalances, settings } = useApp()
  const theme = getTheme(settings.theme)

  useSetPageHeader({ showBack: true, backTo: '/more' })

  const getName = (id: string) => family.find((m) => m.id === id)?.name ?? '?'

  return (
    <PremiumGate feature="Балансы IOU">
      <div style={{ padding: 16 }}>
        {iouBalances.length === 0 ? (
          <p style={{ textAlign: 'center', color: theme.textMuted, padding: 32 }}>
            Все расчёты закрыты
          </p>
        ) : (
          iouBalances.map((bal, i) => (
            <div
              key={i}
              style={{
                padding: 16,
                marginBottom: 12,
                borderRadius: 12,
                background: theme.surface,
                border: `1px solid ${theme.border}`,
              }}
            >
              <p style={{ margin: '0 0 8px', color: theme.text }}>
                <strong>{getName(bal.fromId)}</strong> должен{' '}
                <strong>{getName(bal.toId)}</strong>
              </p>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: 20, fontWeight: 700, color: theme.expense }}>
                  {formatMoney(bal.amount)}
                </span>
                <button
                  type="button"
                  style={{
                    padding: '8px 16px',
                    borderRadius: 8,
                    border: 'none',
                    background: theme.primary,
                    color: '#fff',
                    fontWeight: 600,
                    cursor: 'pointer',
                    fontSize: 13,
                  }}
                >
                  Закрыть долг
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </PremiumGate>
  )
}
