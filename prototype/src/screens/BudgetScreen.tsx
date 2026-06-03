import { Link } from 'react-router-dom'
import { CategoryIcon } from '../components/CategoryIcon'
import { useSetPageHeader } from '../context/HeaderContext'
import { ProgressBar } from '../components/ProgressBar'
import { useApp } from '../context/AppContext'
import {
  formatMoney,
  formatPeriodLabel,
  getCategorySpent,
  getBudgetTotal,
  getTotalExpenses,
} from '../data/mockData'
import { getTheme, radius, shadows } from '../theme'

export function BudgetScreen() {
  const { categories, transactions, settings } = useApp()
  const theme = getTheme(settings.theme)

  useSetPageHeader({ leftSlot: 'notifications', rightSlot: 'add' })

  const expenseCategories = categories.filter((c) => c.type === 'expense' && c.budgetLimit)
  const totalSpent = getTotalExpenses(transactions)
  const totalBudget = getBudgetTotal(categories)
  const periodLabel = formatPeriodLabel()
  const pct = totalBudget > 0 ? Math.round((totalSpent / totalBudget) * 100) : 0

  return (
    <div style={{ padding: '0 16px 16px' }}>
      <div
        style={{
          position: 'relative',
          marginBottom: 16,
          padding: '18px 20px 16px',
          borderRadius: radius.xl,
          background: `linear-gradient(145deg, ${theme.primary} 0%, ${theme.primaryDark} 100%)`,
          color: '#fff',
          boxShadow: shadows.hero,
          overflow: 'hidden',
        }}
      >
        <div
          aria-hidden
          style={{
            position: 'absolute',
            top: -40,
            right: -30,
            width: 140,
            height: 140,
            borderRadius: '50%',
            background: 'rgba(255,255,255,0.06)',
          }}
        />

        <div
          style={{
            position: 'relative',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            marginBottom: 12,
          }}
        >
          <span
            style={{
              display: 'inline-flex',
              padding: '4px 10px',
              borderRadius: radius.full,
              background: 'rgba(255,255,255,0.14)',
              fontSize: 12,
              fontWeight: 600,
            }}
          >
            {periodLabel}
          </span>
          <Link
            to="/categories"
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: 4,
              fontSize: 12,
              fontWeight: 600,
              color: '#fff',
              opacity: 0.9,
              textDecoration: 'none',
            }}
          >
            Категории
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

        <p style={{ position: 'relative', margin: '0 0 4px', fontSize: 13, opacity: 0.88, fontWeight: 500 }}>
          Общий бюджет периода
        </p>
        <p
          style={{
            position: 'relative',
            margin: '0 0 14px',
            fontSize: 34,
            fontWeight: 700,
            letterSpacing: -0.5,
            lineHeight: 1.1,
          }}
        >
          {formatMoney(totalBudget)}
        </p>

        <ProgressBar
          spent={totalSpent}
          limit={totalBudget}
          color="#fff"
          trackColor="rgba(255,255,255,0.22)"
          height={6}
          showLabel
          label={`Потрачено ${formatMoney(totalSpent)} из ${formatMoney(totalBudget)} · ${pct}%`}
          labelColor="rgba(255,255,255,0.85)"
        />
      </div>

      {expenseCategories.map((cat) => {
        const spent = getCategorySpent(cat.id, transactions)
        const limit = cat.budgetLimit ?? 0
        const catPct = limit > 0 ? Math.round((spent / limit) * 100) : 0
        return (
          <Link
            key={cat.id}
            to={`/budget/${cat.id}`}
            style={{
              display: 'block',
              padding: '14px 16px',
              marginBottom: 10,
              borderRadius: radius.md,
              background: theme.surface,
              border: `2px solid ${theme.primary}45`,
              boxShadow: shadows.card,
              textDecoration: 'none',
              color: theme.text,
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 8 }}>
              <CategoryIcon iconId={cat.iconId} size={20} variant="circle" emphasis />
              <span style={{ flex: 1, fontWeight: 600, fontSize: 15 }}>{cat.name}</span>
              <span
                style={{
                  fontSize: 13,
                  fontWeight: 700,
                  color: catPct >= 100 ? theme.expense : theme.textSecondary,
                }}
              >
                {catPct}%
              </span>
            </div>
            <ProgressBar spent={spent} limit={limit} color={cat.color} height={6} />
            <p style={{ margin: '8px 0 0', fontSize: 12, color: theme.textMuted }}>
              {formatMoney(spent)} / {formatMoney(limit)}
            </p>
          </Link>
        )
      })}
    </div>
  )
}
