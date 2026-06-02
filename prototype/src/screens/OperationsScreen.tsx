import { useState } from 'react'
import { Link } from 'react-router-dom'
import { CategoryIcon } from '../components/CategoryIcon'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { formatMoney } from '../data/mockData'
import { getTheme } from '../theme'

export function OperationsScreen() {
  const { transactions, categories, settings } = useApp()
  const theme = getTheme(settings.theme)
  const [search, setSearch] = useState('')
  const [filterType, setFilterType] = useState<'all' | 'expense' | 'income'>('all')

  useSetPageHeader({})

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
    <div style={{ padding: 16 }}>
      <input
        type="search"
        placeholder="Поиск..."
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        style={{
          width: '100%',
          padding: 12,
          borderRadius: 10,
          border: `1px solid ${theme.border}`,
          marginBottom: 12,
          boxSizing: 'border-box',
          background: theme.surface,
          color: theme.text,
        }}
      />
      <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
        {(['all', 'expense', 'income'] as const).map((f) => (
          <button
            key={f}
            type="button"
            onClick={() => setFilterType(f)}
            style={{
              padding: '6px 14px',
              borderRadius: 20,
              border: 'none',
              cursor: 'pointer',
              fontSize: 13,
              fontWeight: 500,
              background: filterType === f ? theme.primary : theme.surfaceAlt,
              color: filterType === f ? '#fff' : theme.textSecondary,
            }}
          >
            {f === 'all' ? 'Все' : f === 'expense' ? 'Расходы' : 'Доходы'}
          </button>
        ))}
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
              padding: 14,
              marginBottom: 8,
              borderRadius: 12,
              background: theme.surface,
              border: `1px solid ${theme.border}`,
              textDecoration: 'none',
              color: theme.text,
            }}
          >
            {cat ? <CategoryIcon iconId={cat.iconId} size={22} /> : <span>📝</span>}
            <div style={{ flex: 1 }}>
              <p style={{ margin: 0, fontWeight: 600 }}>{cat?.name}</p>
              <p style={{ margin: '2px 0 0', fontSize: 12, color: theme.textMuted }}>
                {tx.date} {tx.note ? `· ${tx.note}` : ''}
                {tx.isRecurring ? ' · 🔄' : ''}
              </p>
            </div>
            <span style={{ fontWeight: 700, color: tx.type === 'expense' ? theme.expense : theme.income }}>
              {tx.type === 'expense' ? '−' : '+'}
              {formatMoney(tx.amount)}
            </span>
          </Link>
        )
      })}
    </div>
  )
}
