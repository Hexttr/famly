import { Link } from 'react-router-dom'
import { CategoryIcon } from '../components/CategoryIcon'
import { ProgressBar } from '../components/ProgressBar'
import { TrialStrip } from '../components/TrialStrip'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import {
  formatMoney,
  formatPeriodLabel,
  formatShortDate,
  getBudgetTotal,
  getBudgetWarnings,
  getDailySafeSpend,
  getTopExpenseCategoryIds,
  getTotalExpenses,
  getTotalIncome,
} from '../data/mockData'
import { getTheme, radius, shadows } from '../theme'

function HeroChevron() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M9 18l6-6-6-6"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

export function HomeScreen() {
  const { transactions, categories, openQuickAdd, settings } = useApp()
  const theme = getTheme(settings.theme)

  useSetPageHeader({ leftSlot: 'operations', rightSlot: 'quickAdd' })

  const spent = getTotalExpenses(transactions)
  const income = getTotalIncome(transactions)
  const budgetTotal = getBudgetTotal(categories)
  const remaining = budgetTotal - spent
  const net = income - spent
  const daysLeft = 26
  const dailySpend = getDailySafeSpend(Math.max(0, remaining), daysLeft)
  const periodLabel = formatPeriodLabel()
  const warnings = getBudgetWarnings(categories, transactions)
  const recent = transactions.slice(0, 3)

  const topCategoryIds = getTopExpenseCategoryIds(transactions)
  const quickCategories = topCategoryIds
    .map((id) => categories.find((c) => c.id === id))
    .filter(Boolean)
    .slice(0, 4)

  const fallbackQuick = categories.filter((c) => c.type === 'expense').slice(0, 4)
  const quickAddCategories = quickCategories.length >= 4 ? quickCategories : fallbackQuick

  return (
    <div style={{ paddingBottom: 8 }}>
      <Link
        to="/budget"
        style={{
          position: 'relative',
          display: 'block',
          margin: '0 16px 10px',
          padding: '18px 18px 16px',
          borderRadius: radius.xl,
          background: `linear-gradient(145deg, ${theme.primary} 0%, ${theme.primaryDark} 100%)`,
          color: '#fff',
          boxShadow: shadows.hero,
          overflow: 'hidden',
          textDecoration: 'none',
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
          <span
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: 2,
              fontSize: 12,
              fontWeight: 600,
              opacity: 0.85,
            }}
          >
            Бюджет
            <HeroChevron />
          </span>
        </div>

        <p style={{ position: 'relative', margin: '0 0 4px', fontSize: 13, opacity: 0.88, fontWeight: 500 }}>
          Можно тратить
        </p>
        <p
          style={{
            position: 'relative',
            margin: '0 0 4px',
            fontSize: 38,
            fontWeight: 700,
            letterSpacing: -1,
            lineHeight: 1.1,
          }}
        >
          {formatMoney(Math.max(0, remaining))}
        </p>
        <p
          style={{
            position: 'relative',
            margin: '0 0 14px',
            fontSize: 14,
            fontWeight: 600,
            color: '#B7E4C7',
          }}
        >
          около {formatMoney(dailySpend)} / день · {daysLeft} дн.
        </p>

        <ProgressBar
          spent={spent}
          limit={budgetTotal}
          color="#fff"
          trackColor="rgba(255,255,255,0.22)"
          height={6}
          showLabel
          label={`Потрачено ${formatMoney(spent)} из ${formatMoney(budgetTotal)}`}
          labelColor="rgba(255,255,255,0.85)"
        />
      </Link>

      <TrialStrip />

      {quickAddCategories.length > 0 && (
        <div
          style={{
            margin: '0 16px 12px',
            padding: '14px 12px 12px',
            borderRadius: radius.lg,
            background: `${theme.primary}0A`,
            border: `1px solid ${theme.primary}18`,
          }}
        >
          <p
            style={{
              margin: '0 0 10px',
              paddingLeft: 4,
              fontSize: 13,
              fontWeight: 700,
              color: theme.primary,
            }}
          >
            Быстрый расход
          </p>
          <div style={{ display: 'flex', gap: 8 }}>
            {quickAddCategories.map((cat) => (
              <button
                key={cat!.id}
                type="button"
                onClick={() => openQuickAdd({ categoryId: cat!.id, type: 'expense' })}
                style={{
                  flex: 1,
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: 8,
                  padding: '10px 4px',
                  border: 'none',
                  borderRadius: radius.md,
                  background: theme.surface,
                  boxShadow: shadows.sm,
                  cursor: 'pointer',
                }}
              >
                <CategoryIcon iconId={cat!.iconId} size={22} variant="circle" />
                <span
                  style={{
                    fontSize: 11,
                    fontWeight: 600,
                    color: theme.text,
                    textAlign: 'center',
                    lineHeight: 1.2,
                    maxWidth: '100%',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap',
                  }}
                >
                  {cat!.name}
                </span>
              </button>
            ))}
          </div>
        </div>
      )}

      {warnings.length > 0 && (
        <div style={{ padding: '0 16px', marginBottom: 12 }}>
          <p
            style={{
              margin: '0 0 8px',
              fontSize: 13,
              fontWeight: 600,
              color: theme.textSecondary,
            }}
          >
            Внимание
          </p>
          <div
            style={{
              display: 'flex',
              gap: 8,
              overflowX: 'auto',
              paddingBottom: 2,
              WebkitOverflowScrolling: 'touch',
            }}
          >
            {warnings.map((w) => {
              const cat = categories.find((c) => c.id === w.categoryId)
              if (!cat) return null
              const pct = Math.round(w.percent * 100)
              const isOver = pct >= 100
              return (
                <Link
                  key={w.categoryId}
                  to={`/budget/${w.categoryId}`}
                  style={{
                    flexShrink: 0,
                    display: 'flex',
                    alignItems: 'center',
                    gap: 8,
                    padding: '10px 14px',
                    borderRadius: radius.lg,
                    background: isOver ? `${theme.expense}12` : `${theme.warning}14`,
                    border: `1px solid ${isOver ? theme.expense : theme.warning}35`,
                    textDecoration: 'none',
                    color: theme.text,
                  }}
                >
                  <CategoryIcon iconId={cat.iconId} size={16} variant="circle" />
                  <div>
                    <p style={{ margin: 0, fontSize: 13, fontWeight: 600 }}>{cat.name}</p>
                    <p style={{ margin: '2px 0 0', fontSize: 11, color: theme.textMuted }}>
                      {pct}% {isOver ? '· превышен' : '· лимит'}
                    </p>
                  </div>
                </Link>
              )
            })}
          </div>
        </div>
      )}

      <div
        style={{
          margin: '0 16px 12px',
          padding: '12px 16px',
          borderRadius: radius.md,
          background: theme.surfaceAlt,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: 12,
        }}
      >
        <span style={{ fontSize: 13, fontWeight: 600, color: theme.textSecondary }}>
          Итог месяца
        </span>
        <div style={{ textAlign: 'right' }}>
          <p
            style={{
              margin: 0,
              fontSize: 17,
              fontWeight: 700,
              color: net >= 0 ? theme.income : theme.expense,
            }}
          >
            {net >= 0 ? '+' : '−'}
            {formatMoney(Math.abs(net))}
          </p>
          <p style={{ margin: '2px 0 0', fontSize: 11, color: theme.textMuted }}>
            {formatMoney(income)} − {formatMoney(spent)}
          </p>
        </div>
      </div>

      <div style={{ padding: '0 16px' }}>
        <h2 style={{ margin: '0 0 10px', fontSize: 17, fontWeight: 700, color: theme.text }}>
          Последние операции
        </h2>

        <div
          style={{
            borderRadius: radius.lg,
            background: theme.surface,
            boxShadow: shadows.card,
            overflow: 'hidden',
          }}
        >
          {recent.map((tx, i) => {
            const cat = categories.find((c) => c.id === tx.categoryId)
            const isLast = i === recent.length - 1
            const subtitle = tx.note
              ? `${tx.note} · ${formatShortDate(tx.date)}`
              : formatShortDate(tx.date)

            return (
              <Link
                key={tx.id}
                to={`/operations/${tx.id}`}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 12,
                  padding: '12px 14px',
                  borderBottom: isLast ? 'none' : `1px solid ${theme.border}`,
                  textDecoration: 'none',
                  color: theme.text,
                }}
              >
                {cat ? (
                  <CategoryIcon iconId={cat.iconId} size={20} variant="circle" />
                ) : (
                  <span>📝</span>
                )}
                <div style={{ flex: 1, minWidth: 0 }}>
                  <p
                    style={{
                      margin: 0,
                      fontSize: 15,
                      fontWeight: 600,
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    {cat?.name ?? '—'}
                  </p>
                  <p
                    style={{
                      margin: '2px 0 0',
                      fontSize: 12,
                      color: theme.textMuted,
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    {subtitle}
                  </p>
                </div>
                <span
                  style={{
                    flexShrink: 0,
                    fontSize: 15,
                    fontWeight: 700,
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
    </div>
  )
}
