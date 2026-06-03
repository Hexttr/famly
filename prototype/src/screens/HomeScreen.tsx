import { useState } from 'react'
import { Link } from 'react-router-dom'
import { CategoryIcon } from '../components/CategoryIcon'
import { ProgressBar } from '../components/ProgressBar'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import {
  formatMoney,
  formatPeriodLabel,
  formatShortDate,
  getBudgetTotal,
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

const INITIAL_RECENT = 5
const LOAD_MORE_STEP = 10

export function HomeScreen() {
  const { transactions, categories, openQuickAdd, settings } = useApp()
  const theme = getTheme(settings.theme)
  const [visibleRecent, setVisibleRecent] = useState(INITIAL_RECENT)

  useSetPageHeader({ leftSlot: 'notifications', rightSlot: 'quickAdd' })

  const spent = getTotalExpenses(transactions)
  const income = getTotalIncome(transactions)
  const budgetTotal = getBudgetTotal(categories)
  const remaining = budgetTotal - spent
  const net = income - spent
  const daysLeft = 26
  const dailySpend = getDailySafeSpend(Math.max(0, remaining), daysLeft)
  const periodLabel = formatPeriodLabel()
  const recent = transactions.slice(0, visibleRecent)
  const hasMore = visibleRecent < transactions.length

  const topCategoryIds = getTopExpenseCategoryIds(transactions)
  const quickCategories = topCategoryIds
    .map((id) => categories.find((c) => c.id === id))
    .filter(Boolean)
    .slice(0, 4)

  const fallbackQuick = categories.filter((c) => c.type === 'expense').slice(0, 4)
  const quickAddCategories = quickCategories.length >= 4 ? quickCategories : fallbackQuick

  return (
    <div style={{ paddingBottom: 24 }}>
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

      {quickAddCategories.length > 0 && (
        <div style={{ display: 'flex', gap: 8, margin: '0 16px 12px' }}>
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
                padding: '12px 4px',
                border: `2px solid ${theme.border}`,
                borderRadius: radius.md,
                background: theme.surface,
                boxShadow: shadows.sm,
                cursor: 'pointer',
              }}
            >
              <CategoryIcon iconId={cat!.iconId} size={22} variant="circle" emphasis />
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
      )}

      <div
        style={{
          margin: '0 16px 12px',
          padding: '14px 18px',
          borderRadius: radius.lg,
          background: `${theme.primary}14`,
          boxShadow: `0 2px 8px ${theme.primary}20`,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: 12,
        }}
      >
        <span style={{ fontSize: 15, fontWeight: 700, color: theme.primary }}>
          Текущая экономия
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
            const isLast = i === recent.length - 1 && !hasMore
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

        {hasMore && (
          <button
            type="button"
            onClick={() => setVisibleRecent((n) => Math.min(n + LOAD_MORE_STEP, transactions.length))}
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 8,
              width: '100%',
              marginTop: 12,
              marginBottom: 8,
              padding: '14px 18px',
              border: `1.5px solid ${theme.primary}30`,
              borderLeft: `4px solid ${theme.primary}`,
              borderRadius: radius.lg,
              background: theme.surface,
              color: theme.primary,
              fontSize: 15,
              fontWeight: 700,
              cursor: 'pointer',
              boxShadow: shadows.card,
            }}
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
              <path
                d="M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
            Показать ещё
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden>
              <path
                d="M6 9l6 6 6-6"
                stroke="currentColor"
                strokeWidth="2.5"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </button>
        )}
      </div>
    </div>
  )
}
