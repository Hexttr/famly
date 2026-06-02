import { useState } from 'react'
import { Header } from '../components/Header'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

export function CategoriesScreen() {
  const { categories, addCategory, deleteCategory, settings } = useApp()
  const theme = getTheme(settings.theme)
  const [tab, setTab] = useState<'expense' | 'income'>('expense')
  const [newName, setNewName] = useState('')

  const filtered = categories.filter((c) => c.type === tab)

  const handleAdd = () => {
    if (!newName.trim()) return
    addCategory({
      name: newName.trim(),
      icon: tab === 'expense' ? '📦' : '💵',
      type: tab,
      color: tab === 'expense' ? '#457B9D' : '#2D6A4F',
    })
    setNewName('')
  }

  return (
    <div>
      <Header title="Категории" backTo="/budget" />
      <div style={{ padding: 16 }}>
        <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
          {(['expense', 'income'] as const).map((t) => (
            <button
              key={t}
              type="button"
              onClick={() => setTab(t)}
              style={{
                flex: 1,
                padding: 10,
                border: 'none',
                borderRadius: 10,
                cursor: 'pointer',
                fontWeight: 600,
                background: tab === t ? theme.primary : theme.surfaceAlt,
                color: tab === t ? '#fff' : theme.textSecondary,
              }}
            >
              {t === 'expense' ? 'Расходы' : 'Доходы'}
            </button>
          ))}
        </div>

        {filtered.map((cat) => (
          <div
            key={cat.id}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 12,
              padding: 14,
              marginBottom: 8,
              borderRadius: 12,
              background: theme.surface,
              border: `1px solid ${theme.border}`,
            }}
          >
            <span style={{ fontSize: 24 }}>{cat.icon}</span>
            <span style={{ flex: 1, fontWeight: 500, color: theme.text }}>{cat.name}</span>
            <button
              type="button"
              onClick={() => deleteCategory(cat.id)}
              style={{ border: 'none', background: 'none', color: theme.expense, cursor: 'pointer', fontSize: 18 }}
            >
              ✕
            </button>
          </div>
        ))}

        <div style={{ display: 'flex', gap: 8, marginTop: 16 }}>
          <input
            type="text"
            placeholder="Новая категория..."
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
            style={{
              flex: 1,
              padding: 12,
              borderRadius: 10,
              border: `1px solid ${theme.border}`,
              background: theme.surface,
              color: theme.text,
            }}
          />
          <button
            type="button"
            onClick={handleAdd}
            style={{
              padding: '12px 16px',
              border: 'none',
              borderRadius: 10,
              background: theme.primary,
              color: '#fff',
              fontWeight: 600,
              cursor: 'pointer',
            }}
          >
            +
          </button>
        </div>
      </div>
    </div>
  )
}
