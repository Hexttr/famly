import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { CategoryIcon, IconPicker } from '../components/CategoryIcon'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { DEFAULT_EXPENSE_ICON, DEFAULT_INCOME_ICON } from '../data/categoryIcons'
import { getTheme, radius, shadows } from '../theme'

function IconExpense() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path d="M12 5v14M5 12l7 7 7-7" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

function IconIncome() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path d="M12 19V5M5 12l7-7 7 7" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

function IconNewCategory() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path d="M4 7h7M4 12h16M4 17h11" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <circle cx="17" cy="7" r="2" stroke="currentColor" strokeWidth="2" />
      <circle cx="19" cy="17" r="2" stroke="currentColor" strokeWidth="2" />
    </svg>
  )
}

function IconName() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path d="M12 20h9M16.5 3.5a2.12 2.12 0 013 3L7 19l-4 1 1-4L16.5 3.5z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

function IconCancel() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
    </svg>
  )
}

function IconSave() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path d="M5 12l5 5L20 7" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

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
        {(['expense', 'income'] as const).map((t) => {
          const active = tab === t
          const accent = t === 'expense' ? theme.expense : theme.income
          const Icon = t === 'expense' ? IconExpense : IconIncome

          return (
            <button
              key={t}
              type="button"
              onClick={() => switchTab(t)}
              style={{
                flex: 1,
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 6,
                padding: '10px 0',
                border: `2px solid ${active ? accent : `${theme.primary}45`}`,
                borderRadius: radius.md,
                cursor: 'pointer',
                fontWeight: 600,
                background: active ? accent : theme.surface,
                color: active ? '#fff' : theme.textSecondary,
                boxShadow: active ? 'none' : shadows.sm,
              }}
            >
              <Icon />
              {t === 'expense' ? 'Расходы' : 'Доходы'}
            </button>
          )
        })}
      </div>

      {filtered.map((cat) => (
        <div
          key={cat.id}
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
          }}
        >
          <CategoryIcon iconId={cat.iconId} variant="circle" emphasis />
          <span style={{ flex: 1, fontWeight: 600, color: theme.text }}>{cat.name}</span>
          <button
            type="button"
            onClick={() => deleteCategory(cat.id)}
            aria-label={`Удалить ${cat.name}`}
            style={{
              border: `2px solid ${theme.expense}40`,
              background: `${theme.expense}10`,
              borderRadius: radius.sm,
              width: 32,
              height: 32,
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: theme.expense,
              cursor: 'pointer',
              fontSize: 16,
              fontWeight: 700,
              flexShrink: 0,
            }}
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
            borderRadius: radius.md,
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
            borderRadius: radius.lg,
            background: theme.surface,
            border: `2px solid ${theme.primary}45`,
            boxShadow: shadows.card,
          }}
        >
          <p
            style={{
              margin: '0 0 14px',
              fontWeight: 700,
              fontSize: 16,
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
              }}
            >
              <IconNewCategory />
            </span>
            Новая категория
          </p>

          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 10,
              padding: '0 14px',
              marginBottom: 14,
              borderRadius: radius.md,
              border: `2px solid ${theme.primary}45`,
              background: theme.surface,
              boxShadow: shadows.sm,
            }}
          >
            <span style={{ color: theme.textMuted, display: 'flex', flexShrink: 0 }}>
              <IconName />
            </span>
            <input
              type="text"
              placeholder="Название..."
              value={newName}
              onChange={(e) => setNewName(e.target.value)}
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
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 6,
                padding: 12,
                borderRadius: radius.md,
                border: `2px solid ${theme.primary}45`,
                background: theme.surface,
                cursor: 'pointer',
                color: theme.textSecondary,
                fontWeight: 600,
                boxShadow: shadows.sm,
              }}
            >
              <IconCancel />
              Отмена
            </button>
            <button
              type="button"
              onClick={handleAdd}
              style={{
                flex: 1,
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 6,
                padding: 12,
                borderRadius: radius.md,
                border: 'none',
                background: theme.primary,
                color: '#fff',
                fontWeight: 600,
                cursor: 'pointer',
                boxShadow: '0 4px 14px rgba(45, 106, 79, 0.35)',
              }}
            >
              Сохранить
              <IconSave />
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
