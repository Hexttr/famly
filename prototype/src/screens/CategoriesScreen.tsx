import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { CategoryIcon, IconPicker } from '../components/CategoryIcon'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { DEFAULT_EXPENSE_ICON, DEFAULT_INCOME_ICON } from '../data/categoryIcons'
import { getTheme } from '../theme'

export function CategoriesScreen() {
  const { categories, addCategory, deleteCategory, settings } = useApp()
  const theme = getTheme(settings.theme)
  const [tab, setTab] = useState<'expense' | 'income'>('expense')
  const [newName, setNewName] = useState('')
  const [selectedIconId, setSelectedIconId] = useState<string>(
    tab === 'expense' ? DEFAULT_EXPENSE_ICON : DEFAULT_INCOME_ICON,
  )
  const [selectedColor, setSelectedColor] = useState('#457B9D')
  const [searchParams] = useSearchParams()
  const [showForm, setShowForm] = useState(false)

  useSetPageHeader({ showBack: true, backTo: '/budget', rightSlot: 'add' })

  useEffect(() => {
    if (searchParams.get('new') === '1') setShowForm(true)
  }, [searchParams])

  useEffect(() => {
    const open = () => setShowForm(true)
    window.addEventListener('header-add', open)
    return () => window.removeEventListener('header-add', open)
  }, [])

  const filtered = categories.filter((c) => c.type === tab)

  const handleAdd = () => {
    if (!newName.trim()) return
    addCategory({
      name: newName.trim(),
      iconId: selectedIconId,
      type: tab,
      color: selectedColor,
    })
    setNewName('')
    setShowForm(false)
    setSelectedIconId(tab === 'expense' ? DEFAULT_EXPENSE_ICON : DEFAULT_INCOME_ICON)
  }

  const switchTab = (t: 'expense' | 'income') => {
    setTab(t)
    setSelectedIconId(t === 'expense' ? DEFAULT_EXPENSE_ICON : DEFAULT_INCOME_ICON)
  }

  return (
    <div style={{ padding: '0 16px 16px' }}>
      <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
        {(['expense', 'income'] as const).map((t) => (
          <button
            key={t}
            type="button"
            onClick={() => switchTab(t)}
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
          <CategoryIcon iconId={cat.iconId} />
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

      {!showForm ? (
        <button
          type="button"
          onClick={() => setShowForm(true)}
          style={{
            width: '100%',
            marginTop: 8,
            padding: 14,
            borderRadius: 12,
            border: `2px dashed ${theme.primary}`,
            background: 'transparent',
            color: theme.primary,
            fontWeight: 600,
            cursor: 'pointer',
          }}
        >
          + Добавить категорию
        </button>
      ) : (
        <div
          style={{
            marginTop: 8,
            padding: 16,
            borderRadius: 16,
            background: theme.surface,
            border: `1px solid ${theme.border}`,
          }}
        >
          <p style={{ margin: '0 0 12px', fontWeight: 600, color: theme.text }}>Новая категория</p>
          <input
            type="text"
            placeholder="Название..."
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
            style={{
              width: '100%',
              padding: 12,
              borderRadius: 10,
              border: `1px solid ${theme.border}`,
              background: theme.surface,
              color: theme.text,
              marginBottom: 12,
              boxSizing: 'border-box',
            }}
          />
          <p style={{ margin: '0 0 8px', fontSize: 13, color: theme.textSecondary }}>Выберите иконку</p>
          <IconPicker
            type={tab}
            selectedId={selectedIconId}
            onSelect={(icon) => {
              setSelectedIconId(icon.id)
              setSelectedColor(icon.color)
            }}
          />
          <div style={{ display: 'flex', gap: 8, marginTop: 16 }}>
            <button
              type="button"
              onClick={() => setShowForm(false)}
              style={{
                flex: 1,
                padding: 12,
                borderRadius: 10,
                border: `1px solid ${theme.border}`,
                background: theme.surface,
                cursor: 'pointer',
                color: theme.textSecondary,
              }}
            >
              Отмена
            </button>
            <button
              type="button"
              onClick={handleAdd}
              style={{
                flex: 1,
                padding: 12,
                borderRadius: 10,
                border: 'none',
                background: theme.primary,
                color: '#fff',
                fontWeight: 600,
                cursor: 'pointer',
              }}
            >
              Сохранить
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
