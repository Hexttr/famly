import { useState } from 'react'
import { Link } from 'react-router-dom'
import { CategoryIcon } from '../components/CategoryIcon'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { formatMoney, formatShortDate } from '../data/mockData'
import { getTheme, radius, shadows } from '../theme'

function IconSearch() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
      <circle cx="11" cy="11" r="7" stroke="currentColor" strokeWidth="2" />
      <path d="M20 20l-3-3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  )
}

function IconFilterAll() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path d="M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
    </svg>
  )
}

function IconFilterExpense() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path d="M12 5v14M5 12l7 7 7-7" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

function IconFilterIncome() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path d="M12 19V5M5 12l7-7 7 7" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

const FILTERS = [
  { id: 'all' as const, label: 'Все', Icon: IconFilterAll },
  { id: 'expense' as const, label: 'Расходы', Icon: IconFilterExpense },
  { id: 'income' as const, label: 'Доходы', Icon: IconFilterIncome },
]

export function OperationsScreen() {
  const { transactions, categories, settings } = useApp()
  const theme = getTheme(settings.theme)
  const [search, setSearch] = useState('')
  const [filterType, setFilterType] = useState<'all' | 'expense' | 'income'>('all')

  useSetPageHeader({ leftSlot: 'notifications', rightSlot: 'quickAdd' })

  const filtered = transactions.filter((tx) => {
    if (filterType !== 'all' && tx.type !== filterType) return false
    if (!search) return true
    const cat = categories.find((c) => c.id === tx.categoryId)
    const q = search.toLowerCase()
    return (
      cat?.name.toLowerCase().includes(q) ||
      tx.note?.toLowerCase().includes(q) ||
      String(tx.amount).includes(q)
    )
  })

  return (
    <div style={{ padding: '0 16px 16px' }}>
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 10,
          padding: '0 14px',
          marginBottom: 12,
          borderRadius: radius.md,
          border: `2px solid ${theme.primary}45`,
          background: theme.surface,
          boxShadow: shadows.card,
        }}
      >
        <span style={{ color: theme.textMuted, display: 'flex', flexShrink: 0 }}>
          <IconSearch />
        </span>
        <input
          id="ops-search"
          type="search"
          placeholder="Поиск..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          style={{
            flex: 1,
            minWidth: 0,
            padding: '12px 0',
            border: 'none',
            outline: 'none',
            background: 'transparent',
            color: theme.text,
            fontSize: 15,
          }}
        />
      </div>

      <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
        {FILTERS.map(({ id, label, Icon }) => {
          const active = filterType === id
          const accent =
            id === 'expense' ? theme.expense : id === 'income' ? theme.income : theme.primary

          return (
            <button
              key={id}
              type="button"
              onClick={() => setFilterType(id)}
              style={{
                flex: 1,
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 6,
                padding: '8px 10px',
                borderRadius: radius.full,
                border: `2px solid ${active ? accent : `${theme.primary}45`}`,
                cursor: 'pointer',
                fontSize: 13,
                fontWeight: 600,
                background: active ? accent : theme.surface,
                color: active ? '#fff' : theme.textSecondary,
                boxShadow: active ? 'none' : shadows.sm,
              }}
            >
              <Icon />
              {label}
            </button>
          )
        })}
      </div>

      {filtered.map((tx) => {
        const cat = categories.find((c) => c.id === tx.categoryId)
        return (
          <Link
            key={tx.id}
            to={`/operations/${tx.id}`}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 12,
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
            {cat ? (
              <CategoryIcon iconId={cat.iconId} size={22} variant="circle" />
            ) : (
              <span>📝</span>
            )}
            <div style={{ flex: 1, minWidth: 0 }}>
              <p style={{ margin: 0, fontWeight: 600, fontSize: 15 }}>{cat?.name}</p>
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
                {formatShortDate(tx.date)} {tx.note ? `· ${tx.note}` : ''}
                {tx.isRecurring ? ' · 🔄' : ''}
              </p>
            </div>
            <span
              style={{
                flexShrink: 0,
                fontWeight: 700,
                fontSize: 15,
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
  )
}
