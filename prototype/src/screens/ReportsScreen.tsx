import { useMemo, useState } from 'react'
import { CategoryIcon } from '../components/CategoryIcon'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import {
  REPORT_PERIODS,
  filterTransactionsByPeriod,
  formatMoney,
  getCategorySpent,
  getReportPeriodDescription,
  type ReportPeriod,
} from '../data/mockData'
import { getTheme, radius, shadows } from '../theme'

function IconTopCategories() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path d="M8 6h13M8 12h13M8 18h9" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
      <path d="M4 6h.01M4 12h.01M4 18h.01" stroke="currentColor" strokeWidth="3" strokeLinecap="round" />
    </svg>
  )
}

function IconChart() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path d="M4 19V5M4 19h16" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <path d="M8 17V11M12 17V7M16 17v-4" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
    </svg>
  )
}

function buildDonutGradient(
  slices: { spent: number; color: string }[],
  total: number,
): string {
  if (total <= 0) return '#E2E8E5'
  let angle = 0
  return slices
    .filter((s) => s.spent > 0)
    .map((slice) => {
      const sweep = (slice.spent / total) * 360
      const start = angle
      angle += sweep
      return `${slice.color} ${start}deg ${angle}deg`
    })
    .join(', ')
}

export function ReportsScreen() {
  const { categories, transactions, settings } = useApp()
  const theme = getTheme(settings.theme)
  const [period, setPeriod] = useState<ReportPeriod>('month')

  useSetPageHeader({ showBack: true, backTo: '/more', rightSlot: 'export' })

  const filteredTx = useMemo(
    () => filterTransactionsByPeriod(transactions, period),
    [transactions, period],
  )

  const expenseCategories = useMemo(
    () =>
      categories
        .filter((c) => c.type === 'expense')
        .map((c) => ({ ...c, spent: getCategorySpent(c.id, filteredTx) }))
        .filter((c) => c.spent > 0)
        .sort((a, b) => b.spent - a.spent),
    [categories, filteredTx],
  )

  const totalSpent = expenseCategories.reduce((s, c) => s + c.spent, 0)
  const top5 = expenseCategories.slice(0, 5)
  const periodDescription = getReportPeriodDescription(period)
  const donutGradient = buildDonutGradient(top5, totalSpent)
  const topShare =
    totalSpent > 0 && top5[0] ? Math.round((top5[0].spent / totalSpent) * 100) : 0

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

      <div
        style={{
          position: 'relative',
          padding: '18px 20px 20px',
          borderRadius: radius.xl,
          background: `linear-gradient(145deg, ${theme.primary} 0%, ${theme.primaryDark} 100%)`,
          color: '#fff',
          boxShadow: shadows.hero,
          marginBottom: 16,
          overflow: 'hidden',
        }}
      >
        <div
          aria-hidden
          style={{
            position: 'absolute',
            top: -40,
            right: -30,
            width: 130,
            height: 130,
            borderRadius: '50%',
            background: 'rgba(255,255,255,0.06)',
          }}
        />

        <div style={{ position: 'relative', display: 'flex', alignItems: 'flex-start', gap: 12 }}>
          <span
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: 40,
              height: 40,
              borderRadius: 20,
              background: 'rgba(255,255,255,0.14)',
              border: '2px solid rgba(255,255,255,0.35)',
              flexShrink: 0,
              color: '#fff',
            }}
          >
            <IconChart />
          </span>
          <div style={{ flex: 1, minWidth: 0 }}>
            <p style={{ margin: 0, fontSize: 13, opacity: 0.88, fontWeight: 500 }}>Расходы за период</p>
            <p style={{ margin: '2px 0 8px', fontSize: 12, opacity: 0.75 }}>{periodDescription}</p>
            <p style={{ margin: 0, fontSize: 30, fontWeight: 700, letterSpacing: -0.5, lineHeight: 1.1 }}>
              {formatMoney(totalSpent)}
            </p>
          </div>
        </div>
      </div>

      <div
        style={{
          padding: '20px 16px',
          borderRadius: radius.lg,
          background: theme.surface,
          border: `2px solid ${theme.primary}45`,
          boxShadow: shadows.card,
          marginBottom: 16,
        }}
      >
        <div
          style={{
            position: 'relative',
            width: 200,
            height: 200,
            margin: '0 auto 8px',
          }}
        >
          <div
            style={{
              width: '100%',
              height: '100%',
              borderRadius: '50%',
              background: totalSpent > 0 ? `conic-gradient(${donutGradient})` : theme.surfaceAlt,
              boxShadow: 'inset 0 0 0 1px rgba(0,0,0,0.04)',
            }}
          />
          <div
            style={{
              position: 'absolute',
              inset: '22%',
              borderRadius: '50%',
              background: theme.surface,
              border: `2px solid ${theme.primary}20`,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              textAlign: 'center',
              padding: 8,
            }}
          >
            <p style={{ margin: 0, fontSize: 11, color: theme.textMuted, fontWeight: 600 }}>
              {top5[0]?.name ?? '—'}
            </p>
            <p style={{ margin: '2px 0 0', fontSize: 20, fontWeight: 700, color: theme.text }}>
              {topShare}%
            </p>
          </div>
        </div>

        {totalSpent > 0 && (
          <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '8px 12px' }}>
            {top5.map((cat) => (
              <span
                key={cat.id}
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: 6,
                  fontSize: 11,
                  color: theme.textSecondary,
                  fontWeight: 600,
                }}
              >
                <span
                  style={{
                    width: 8,
                    height: 8,
                    borderRadius: 4,
                    background: cat.color,
                    flexShrink: 0,
                  }}
                />
                {cat.name}
              </span>
            ))}
          </div>
        )}
      </div>

      <h3
        style={{
          margin: '0 0 10px',
          fontSize: 16,
          fontWeight: 700,
          color: theme.text,
          display: 'flex',
          alignItems: 'center',
          gap: 8,
        }}
      >
        <span
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: 32,
            height: 32,
            borderRadius: 16,
            background: `${theme.primary}14`,
            color: theme.primary,
            border: `2px solid ${theme.primary}45`,
            flexShrink: 0,
          }}
        >
          <IconTopCategories />
        </span>
        Топ-5 категорий
      </h3>

      {top5.length === 0 ? (
        <div
          style={{
            padding: '28px 16px',
            borderRadius: radius.lg,
            background: theme.surface,
            border: `2px solid ${theme.primary}45`,
            textAlign: 'center',
            color: theme.textMuted,
            fontSize: 14,
          }}
        >
          Нет расходов за выбранный период
        </div>
      ) : (
        <div
          style={{
            borderRadius: radius.lg,
            background: theme.surface,
            border: `2px solid ${theme.primary}45`,
            boxShadow: shadows.card,
            overflow: 'hidden',
          }}
        >
          {top5.map((cat, i) => {
            const pct = totalSpent > 0 ? Math.round((cat.spent / totalSpent) * 100) : 0
            const isLast = i === top5.length - 1
            return (
              <div
                key={cat.id}
                style={{
                  padding: '14px 16px',
                  borderBottom: isLast ? 'none' : `1px solid ${theme.primary}20`,
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 8 }}>
                  <CategoryIcon iconId={cat.iconId} size={20} variant="circle" emphasis />
                  <span style={{ flex: 1, fontWeight: 600, fontSize: 15, color: theme.text }}>
                    {cat.name}
                  </span>
                  <span style={{ fontWeight: 700, fontSize: 15, color: theme.text }}>
                    {formatMoney(cat.spent)}
                  </span>
                  <span
                    style={{
                      fontSize: 13,
                      fontWeight: 700,
                      color: theme.textSecondary,
                      minWidth: 36,
                      textAlign: 'right',
                    }}
                  >
                    {pct}%
                  </span>
                </div>
                <div
                  style={{
                    height: 6,
                    borderRadius: 3,
                    background: theme.surfaceAlt,
                    overflow: 'hidden',
                  }}
                >
                  <div
                    style={{
                      width: `${pct}%`,
                      height: '100%',
                      background: cat.color,
                      borderRadius: 3,
                      transition: 'width 0.35s ease',
                    }}
                  />
                </div>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
