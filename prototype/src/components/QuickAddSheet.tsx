import { useEffect, useState } from 'react'
import { CategoryIcon } from './CategoryIcon'
import { useApp } from '../context/AppContext'
import { getTheme, radius, shadows } from '../theme'

function IconNewOperation() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path d="M12 5v14M5 12h14" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
    </svg>
  )
}

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

function IconSave() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M5 12l5 5L20 7"
        stroke="currentColor"
        strokeWidth="2.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

export function QuickAddSheet() {
  const {
    quickAddOpen,
    setQuickAddOpen,
    quickAddPreset,
    categories,
    accounts,
    addTransaction,
    settings,
  } = useApp()
  const theme = getTheme(settings.theme)

  const expenseCategories = categories.filter((c) => c.type === 'expense')
  const [amount, setAmount] = useState('')
  const [type, setType] = useState<'expense' | 'income'>('expense')
  const [categoryId, setCategoryId] = useState(expenseCategories[0]?.id ?? '')
  const [accountId, setAccountId] = useState(accounts[0]?.id ?? '')
  const [note, setNote] = useState('')
  const [recurring, setRecurring] = useState(false)

  useEffect(() => {
    if (!quickAddOpen || !quickAddPreset) return
    if (quickAddPreset.type) setType(quickAddPreset.type)
    if (quickAddPreset.categoryId) setCategoryId(quickAddPreset.categoryId)
  }, [quickAddOpen, quickAddPreset])

  if (!quickAddOpen) return null

  const filteredCategories = categories.filter((c) => c.type === type)

  const handleSave = () => {
    const num = parseFloat(amount.replace(',', '.'))
    if (!num || !categoryId || !accountId) return
    addTransaction({
      amount: num,
      type,
      categoryId,
      accountId,
      date: new Date().toISOString().slice(0, 10),
      note: note || undefined,
      isRecurring: recurring,
    })
    setAmount('')
    setNote('')
    setRecurring(false)
    setQuickAddOpen(false)
  }

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        background: 'rgba(0,0,0,0.45)',
        zIndex: 200,
        display: 'flex',
        alignItems: 'flex-end',
        justifyContent: 'center',
      }}
      onClick={() => setQuickAddOpen(false)}
    >
      <div
        style={{
          width: '100%',
          background: theme.surface,
          borderRadius: '16px 16px 0 0',
          padding: 20,
          maxHeight: '85vh',
          overflow: 'auto',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <h2
            style={{
              margin: 0,
              fontSize: 18,
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
              }}
            >
              <IconNewOperation />
            </span>
            Новая операция
          </h2>
          <button
            type="button"
            onClick={() => setQuickAddOpen(false)}
            style={{ border: 'none', background: 'none', fontSize: 20, cursor: 'pointer', color: theme.textMuted }}
          >
            ✕
          </button>
        </div>

        <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
          {(['expense', 'income'] as const).map((t) => {
            const active = type === t
            const accent = t === 'expense' ? theme.expense : theme.income
            const Icon = t === 'expense' ? IconExpense : IconIncome

            return (
              <button
                key={t}
                type="button"
                onClick={() => {
                  setType(t)
                  const first = categories.find((c) => c.type === t)
                  if (first) setCategoryId(first.id)
                }}
                style={{
                  flex: 1,
                  display: 'inline-flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: 6,
                  padding: '10px 0',
                  border: `2px solid ${active ? accent : `${theme.primary}45`}`,
                  borderRadius: radius.md,
                  fontWeight: 600,
                  cursor: 'pointer',
                  background: active ? accent : theme.surface,
                  color: active ? '#fff' : theme.textSecondary,
                  boxShadow: active ? 'none' : shadows.sm,
                }}
              >
                <Icon />
                {t === 'expense' ? 'Расход' : 'Доход'}
              </button>
            )
          })}
        </div>

        <input
          type="text"
          inputMode="decimal"
          placeholder="0"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          style={{
            width: '100%',
            fontSize: 36,
            fontWeight: 700,
            textAlign: 'center',
            border: `2px solid ${theme.primary}45`,
            background: theme.surfaceAlt,
            borderRadius: radius.md,
            padding: 16,
            marginBottom: 16,
            color: theme.text,
            boxSizing: 'border-box',
            boxShadow: shadows.sm,
            outline: 'none',
          }}
        />

        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 16 }}>
          {filteredCategories.map((c) => (
            <button
              key={c.id}
              type="button"
              onClick={() => setCategoryId(c.id)}
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: 6,
                padding: '8px 12px',
                borderRadius: radius.full,
                border:
                  categoryId === c.id
                    ? `2px solid ${c.color}`
                    : `2px solid ${theme.primary}45`,
                background: categoryId === c.id ? `${c.color}18` : theme.surface,
                cursor: 'pointer',
                fontSize: 13,
                fontWeight: 600,
                color: theme.text,
                boxShadow: categoryId === c.id ? 'none' : shadows.sm,
              }}
            >
              <CategoryIcon iconId={c.iconId} size={16} />
              {c.name}
            </button>
          ))}
        </div>

        <select
          value={accountId}
          onChange={(e) => setAccountId(e.target.value)}
          style={{
            width: '100%',
            padding: 12,
            borderRadius: radius.md,
            border: `2px solid ${theme.primary}45`,
            marginBottom: 16,
            background: theme.surface,
            color: theme.text,
            fontSize: 15,
            boxShadow: shadows.sm,
          }}
        >
          {accounts.map((a) => (
            <option key={a.id} value={a.id}>
              {a.icon} {a.name}
            </option>
          ))}
        </select>

        <input
          type="text"
          placeholder="Заметка (необязательно)"
          value={note}
          onChange={(e) => setNote(e.target.value)}
          style={{
            width: '100%',
            padding: 12,
            borderRadius: radius.md,
            border: `2px solid ${theme.primary}45`,
            marginBottom: 12,
            boxSizing: 'border-box',
            background: theme.surface,
            color: theme.text,
            fontSize: 15,
            boxShadow: shadows.sm,
          }}
        />

        <label style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 20, color: theme.textSecondary, fontSize: 14 }}>
          <input type="checkbox" checked={recurring} onChange={(e) => setRecurring(e.target.checked)} />
          Повторять каждый месяц
        </label>

        <button
          type="button"
          onClick={handleSave}
          style={{
            width: '100%',
            padding: 16,
            border: 'none',
            borderRadius: radius.md,
            background: theme.primary,
            color: '#fff',
            fontSize: 16,
            fontWeight: 600,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 8,
            boxShadow: '0 4px 14px rgba(45, 106, 79, 0.35)',
          }}
        >
          Сохранить
          <IconSave />
        </button>
      </div>
    </div>
  )
}
