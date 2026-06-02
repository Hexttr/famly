import { useState } from 'react'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

export function QuickAddSheet() {
  const {
    quickAddOpen,
    setQuickAddOpen,
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
        background: 'rgba(0,0,0,0.4)',
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
          maxWidth: 375,
          background: theme.surface,
          borderRadius: '16px 16px 0 0',
          padding: 20,
          maxHeight: '85vh',
          overflow: 'auto',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
          <h2 style={{ margin: 0, fontSize: 18, color: theme.text }}>Новая операция</h2>
          <button
            type="button"
            onClick={() => setQuickAddOpen(false)}
            style={{ border: 'none', background: 'none', fontSize: 20, cursor: 'pointer', color: theme.textMuted }}
          >
            ✕
          </button>
        </div>

        <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
          {(['expense', 'income'] as const).map((t) => (
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
                padding: '10px 0',
                border: 'none',
                borderRadius: 10,
                fontWeight: 600,
                cursor: 'pointer',
                background: type === t ? (t === 'expense' ? theme.expense : theme.income) : theme.surfaceAlt,
                color: type === t ? '#fff' : theme.textSecondary,
              }}
            >
              {t === 'expense' ? 'Расход' : 'Доход'}
            </button>
          ))}
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
            border: 'none',
            background: theme.surfaceAlt,
            borderRadius: 12,
            padding: 16,
            marginBottom: 16,
            color: theme.text,
            boxSizing: 'border-box',
          }}
        />

        <label style={{ display: 'block', marginBottom: 8, fontSize: 13, color: theme.textSecondary }}>
          Категория
        </label>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 16 }}>
          {filteredCategories.map((c) => (
            <button
              key={c.id}
              type="button"
              onClick={() => setCategoryId(c.id)}
              style={{
                padding: '8px 12px',
                borderRadius: 20,
                border: categoryId === c.id ? `2px solid ${c.color}` : `1px solid ${theme.border}`,
                background: categoryId === c.id ? `${c.color}18` : theme.surface,
                cursor: 'pointer',
                fontSize: 13,
              }}
            >
              {c.icon} {c.name}
            </button>
          ))}
        </div>

        <label style={{ display: 'block', marginBottom: 8, fontSize: 13, color: theme.textSecondary }}>
          Счёт
        </label>
        <select
          value={accountId}
          onChange={(e) => setAccountId(e.target.value)}
          style={{
            width: '100%',
            padding: 12,
            borderRadius: 10,
            border: `1px solid ${theme.border}`,
            marginBottom: 16,
            background: theme.surface,
            color: theme.text,
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
            borderRadius: 10,
            border: `1px solid ${theme.border}`,
            marginBottom: 12,
            boxSizing: 'border-box',
            background: theme.surface,
            color: theme.text,
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
            borderRadius: 12,
            background: theme.primary,
            color: '#fff',
            fontSize: 16,
            fontWeight: 600,
            cursor: 'pointer',
          }}
        >
          Сохранить
        </button>
      </div>
    </div>
  )
}
