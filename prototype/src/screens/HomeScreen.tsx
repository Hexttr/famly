import { Link } from 'react-router-dom'
import type { ReactNode } from 'react'
import { CategoryIcon } from '../components/CategoryIcon'
import { PremiumBadge } from '../components/PremiumGate'
import { ProgressBar } from '../components/ProgressBar'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import {
  formatMoney,
  getBudgetTotal,
  getTotalExpenses,
  getTotalIncome,
} from '../data/mockData'
import { getTheme } from '../theme'

function StatIcon({ color, children }: { color: string; children: ReactNode }) {
  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        width: 36,
        height: 36,
        borderRadius: 10,
        background: `${color}18`,
        border: `1.5px solid ${color}40`,
        flexShrink: 0,
      }}
    >
      {children}
    </span>
  )
}

export function HomeScreen() {
  const { transactions, categories, settings } = useApp()
  const theme = getTheme(settings.theme)
  const cardBorder = '#C5D0CB'

  useSetPageHeader({ right: <PremiumBadge /> })

  const spent = getTotalExpenses(transactions)
  const income = getTotalIncome(transactions)
  const budgetTotal = getBudgetTotal(categories)
  const remaining = budgetTotal - spent
  const daysLeft = 26

  const recent = transactions.slice(0, 5)

  return (
    <div>
      <div
        style={{
          margin: 16,
          marginBottom: 16,
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
        <div
          style={{
            flex: 1,
            display: 'flex',
            alignItems: 'center',
            gap: 12,
            padding: 14,
            borderRadius: 12,
            background: theme.surface,
            border: `1.5px solid ${cardBorder}`,
          }}
        >
          <StatIcon color={theme.income}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
              <path
                d="M12 19V5M5 12l7-7 7 7"
                stroke={theme.income}
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </StatIcon>
          <div>
            <p style={{ margin: 0, fontSize: 12, color: theme.textMuted }}>Доходы</p>
            <p style={{ margin: '4px 0 0', fontSize: 17, fontWeight: 600, color: theme.income }}>
              {formatMoney(income)}
            </p>
          </div>
        </div>
        <div
          style={{
            flex: 1,
            display: 'flex',
            alignItems: 'center',
            gap: 12,
            padding: 14,
            borderRadius: 12,
            background: theme.surface,
            border: `1.5px solid ${cardBorder}`,
          }}
        >
          <StatIcon color={theme.expense}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
              <path
                d="M12 5v14M5 12l7 7 7-7"
                stroke={theme.expense}
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </StatIcon>
          <div>
            <p style={{ margin: 0, fontSize: 12, color: theme.textMuted }}>Расходы</p>
            <p style={{ margin: '4px 0 0', fontSize: 17, fontWeight: 600, color: theme.expense }}>
              {formatMoney(spent)}
            </p>
          </div>
        </div>
      </div>

      <div style={{ padding: '0 16px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <StatIcon color={theme.primary}>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
                <path
                  d="M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01"
                  stroke={theme.primary}
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              </svg>
            </StatIcon>
            <h2 style={{ margin: 0, fontSize: 16, color: theme.text }}>Последние операции</h2>
          </div>
          <Link
            to="/operations"
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: 4,
              padding: '6px 12px',
              borderRadius: 20,
              border: `1.5px solid ${theme.primary}40`,
              background: `${theme.primary}10`,
              fontSize: 13,
              fontWeight: 600,
              color: theme.primary,
              textDecoration: 'none',
            }}
          >
            Все
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden>
              <path
                d="M9 18l6-6-6-6"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
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
              {cat ? <CategoryIcon iconId={cat.iconId} size={20} /> : <span>📝</span>}
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
