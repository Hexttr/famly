import { useState } from 'react'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { DEFAULT_ACCOUNT_ICON, nextAccountIcon } from '../data/accountIcons'
import { formatMoney } from '../data/mockData'
import { getTheme, radius, shadows } from '../theme'

function IconTotalBalance({ size = 22 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden>
      <ellipse cx="12" cy="8" rx="7" ry="2.5" stroke="currentColor" strokeWidth="2" />
      <path
        d="M5 8v3.5c0 1.4 3.1 2.5 7 2.5s7-1.1 7-2.5V8"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinejoin="round"
      />
      <path
        d="M5 11.5v3.5c0 1.4 3.1 2.5 7 2.5s7-1.1 7-2.5v-3.5"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinejoin="round"
      />
    </svg>
  )
}

function NewAccountIconButton({
  icon,
  onClick,
}: {
  icon: string
  onClick: () => void
}) {
  return (
    <button
      type="button"
      aria-label="Сменить иконку нового счёта"
      onClick={onClick}
      style={{
        border: 'none',
        background: 'transparent',
        fontSize: 22,
        lineHeight: 1,
        cursor: 'pointer',
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        flexShrink: 0,
        padding: 0,
      }}
    >
      {icon}
    </button>
  )
}

function AccountRowIconButton({
  icon,
  onClick,
  name,
  theme,
}: {
  icon: string
  onClick: () => void
  name: string
  theme: ReturnType<typeof getTheme>
}) {
  return (
    <button
      type="button"
      aria-label={`Сменить иконку счёта ${name}`}
      onClick={onClick}
      style={{
        width: 44,
        height: 44,
        borderRadius: radius.sm,
        border: `2px solid ${theme.primary}45`,
        background: `${theme.primary}10`,
        fontSize: 22,
        lineHeight: 1,
        cursor: 'pointer',
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        flexShrink: 0,
        padding: 0,
      }}
    >
      {icon}
    </button>
  )
}

export function AccountsScreen() {
  const { accounts, addAccount, deleteAccount, updateAccount, settings } = useApp()
  const theme = getTheme(settings.theme)
  const [newName, setNewName] = useState('')
  const [newIcon, setNewIcon] = useState(DEFAULT_ACCOUNT_ICON)

  useSetPageHeader({ showBack: true, backTo: '/more', rightSlot: 'add' })

  const total = accounts.reduce((s, a) => s + a.balance, 0)

  const handleAdd = () => {
    if (!newName.trim()) return
    addAccount({ name: newName.trim(), icon: newIcon, balance: 0, color: theme.primary })
    setNewName('')
    setNewIcon(DEFAULT_ACCOUNT_ICON)
  }

  const cycleAccountIcon = (accountId: string, currentIcon: string) => {
    updateAccount(accountId, { icon: nextAccountIcon(currentIcon) })
  }

  return (
    <div style={{ padding: '0 16px 16px' }}>
      <div
        style={{
          position: 'relative',
          padding: '20px 20px 18px',
          borderRadius: radius.xl,
          background: `linear-gradient(145deg, ${theme.primary} 0%, ${theme.primaryDark} 100%)`,
          color: '#fff',
          marginBottom: 16,
          boxShadow: shadows.hero,
          overflow: 'hidden',
        }}
      >
        <div
          aria-hidden
          style={{
            position: 'absolute',
            top: -36,
            right: -28,
            width: 120,
            height: 120,
            borderRadius: '50%',
            background: 'rgba(255,255,255,0.06)',
          }}
        />

        <div
          style={{
            position: 'relative',
            display: 'flex',
            alignItems: 'stretch',
            gap: 14,
          }}
        >
          <span
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: 54,
              alignSelf: 'stretch',
              minHeight: 54,
              borderRadius: radius.md,
              background: 'rgba(255,255,255,0.14)',
              border: '2px solid rgba(255,255,255,0.35)',
              flexShrink: 0,
              color: '#fff',
            }}
          >
            <IconTotalBalance size={34} />
          </span>
          <div style={{ flex: 1, minWidth: 0 }}>
            <p style={{ margin: 0, fontSize: 13, opacity: 0.88, fontWeight: 500 }}>Общий баланс</p>
            <p style={{ margin: '4px 0 0', fontSize: 28, fontWeight: 700, letterSpacing: -0.5, lineHeight: 1.1 }}>
              {formatMoney(total)}
            </p>
          </div>
        </div>
      </div>

      {accounts.map((acc) => (
        <div
          key={acc.id}
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
          <AccountRowIconButton
            icon={acc.icon}
            name={acc.name}
            theme={theme}
            onClick={() => cycleAccountIcon(acc.id, acc.icon)}
          />
          <div style={{ flex: 1, minWidth: 0 }}>
            <p style={{ margin: 0, fontWeight: 600, color: theme.text, fontSize: 15 }}>{acc.name}</p>
            <p style={{ margin: '2px 0 0', color: theme.textSecondary, fontSize: 14 }}>
              {formatMoney(acc.balance)}
            </p>
          </div>
          <button
            type="button"
            onClick={() => deleteAccount(acc.id)}
            aria-label={`Удалить ${acc.name}`}
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

      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 8,
          marginTop: 8,
        }}
      >
        <div
          style={{
            flex: 1,
            display: 'flex',
            alignItems: 'center',
            gap: 10,
            padding: '0 14px',
            borderRadius: radius.md,
            border: `2px solid ${theme.primary}45`,
            background: theme.surface,
            boxShadow: shadows.sm,
          }}
        >
          <NewAccountIconButton
            icon={newIcon}
            onClick={() => setNewIcon(nextAccountIcon(newIcon))}
          />
          <input
            id="account-add-input"
            type="text"
            placeholder="Новый счёт..."
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleAdd()}
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
        <button
          type="button"
          onClick={handleAdd}
          aria-label="Добавить счёт"
          style={{
            width: 48,
            height: 48,
            border: 'none',
            borderRadius: radius.md,
            background: theme.primary,
            color: '#fff',
            fontWeight: 700,
            fontSize: 22,
            cursor: 'pointer',
            boxShadow: '0 4px 14px rgba(45, 106, 79, 0.35)',
            flexShrink: 0,
          }}
        >
          +
        </button>
      </div>
    </div>
  )
}
