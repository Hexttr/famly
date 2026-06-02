import { useState } from 'react'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { formatMoney } from '../data/mockData'
import { getTheme } from '../theme'

export function AccountsScreen() {
  const { accounts, addAccount, deleteAccount, settings } = useApp()
  const theme = getTheme(settings.theme)
  const [newName, setNewName] = useState('')

  useSetPageHeader({ showBack: true, backTo: '/more' })

  const total = accounts.reduce((s, a) => s + a.balance, 0)

  const handleAdd = () => {
    if (!newName.trim()) return
    addAccount({ name: newName.trim(), icon: '💳', balance: 0, color: theme.primary })
    setNewName('')
  }

  return (
    <div style={{ padding: 16 }}>
      <div
        style={{
          padding: 20,
          borderRadius: 16,
          background: theme.primary,
          color: '#fff',
          marginBottom: 16,
          textAlign: 'center',
        }}
      >
        <p style={{ margin: 0, fontSize: 13, opacity: 0.85 }}>Общий баланс</p>
        <p style={{ margin: '8px 0 0', fontSize: 28, fontWeight: 700 }}>{formatMoney(total)}</p>
      </div>

      {accounts.map((acc) => (
        <div
          key={acc.id}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 12,
            padding: 16,
            marginBottom: 8,
            borderRadius: 12,
            background: theme.surface,
            border: `1px solid ${theme.border}`,
          }}
        >
          <span style={{ fontSize: 22 }}>{acc.icon}</span>
          <div style={{ flex: 1 }}>
            <p style={{ margin: 0, fontWeight: 600, color: theme.text }}>{acc.name}</p>
            <p style={{ margin: '2px 0 0', color: theme.textSecondary }}>{formatMoney(acc.balance)}</p>
          </div>
          <button
            type="button"
            onClick={() => deleteAccount(acc.id)}
            style={{ border: 'none', background: 'none', color: theme.textMuted, cursor: 'pointer' }}
          >
            ✕
          </button>
        </div>
      ))}

      <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
        <input
          type="text"
          placeholder="Новый счёт..."
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
  )
}
