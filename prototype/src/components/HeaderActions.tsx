import type { CSSProperties, ReactNode } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useApp } from '../context/AppContext'
import { getTheme, headerLayout } from '../theme'
import type { HeaderLeftSlot, HeaderRightSlot } from '../context/HeaderContext'
import { HeaderNotifications } from './HeaderNotifications'

function HeaderIconButton({
  label,
  onClick,
  to,
  tone = 'secondary',
  children,
}: {
  label: string
  onClick?: () => void
  to?: string
  tone?: 'primary' | 'secondary'
  children: ReactNode
}) {
  const { settings } = useApp()
  const theme = getTheme(settings.theme)
  const isPrimary = tone === 'primary'
  const size = isPrimary ? headerLayout.buttonSize : headerLayout.buttonSize

  const style: CSSProperties = {
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: size,
    height: size,
    borderRadius: size / 2,
    border: isPrimary ? 'none' : `2px solid ${theme.primary}50`,
    background: isPrimary ? theme.primary : theme.surface,
    color: isPrimary ? '#fff' : theme.primaryDark,
    cursor: 'pointer',
    boxShadow: isPrimary ? '0 2px 8px rgba(45, 106, 79, 0.35)' : '0 2px 10px rgba(45, 106, 79, 0.18)',
    textDecoration: 'none',
    flexShrink: 0,
  }

  if (to) {
    return (
      <Link to={to} aria-label={label} style={style}>
        {children}
      </Link>
    )
  }

  return (
    <button type="button" aria-label={label} onClick={onClick} style={style}>
      {children}
    </button>
  )
}

function IconSearch() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden>
      <circle cx="11" cy="11" r="7" stroke="currentColor" strokeWidth="2" />
      <path d="M20 20l-3-3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  )
}

function IconSettings() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M12 15a3 3 0 100-6 3 3 0 000 6z"
        stroke="currentColor"
        strokeWidth="2"
      />
      <path
        d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 01-2.83 2.83l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z"
        stroke="currentColor"
        strokeWidth="1.5"
      />
    </svg>
  )
}

function IconPlus() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path d="M12 5v14M5 12h14" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
    </svg>
  )
}

function IconExport() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path d="M12 3v12M7 10l5 5 5-5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M5 21h14" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  )
}

export function HeaderLeft({ slot }: { slot?: HeaderLeftSlot }) {
  if (slot === 'notifications') {
    return <HeaderNotifications />
  }

  return null
}

export function HeaderRight({ slot }: { slot?: HeaderRightSlot }) {
  const navigate = useNavigate()
  const { openQuickAdd } = useApp()

  if (slot === 'quickAdd') {
    return (
      <HeaderIconButton label="Добавить операцию" onClick={() => openQuickAdd()}>
        <IconPlus />
      </HeaderIconButton>
    )
  }

  if (slot === 'categories') {
    return (
      <HeaderIconButton label="Категории" to="/categories">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden>
          <path d="M4 7h7M4 12h16M4 17h11" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
          <circle cx="17" cy="7" r="2" stroke="currentColor" strokeWidth="2" />
          <circle cx="19" cy="17" r="2" stroke="currentColor" strokeWidth="2" />
        </svg>
      </HeaderIconButton>
    )
  }

  if (slot === 'settings') {
    return (
      <HeaderIconButton label="Настройки" to="/settings">
        <IconSettings />
      </HeaderIconButton>
    )
  }

  if (slot === 'search') {
    return (
      <HeaderIconButton
        label="Поиск"
        onClick={() => {
          document.getElementById('ops-search')?.focus()
          document.getElementById('ops-search')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
        }}
      >
        <IconSearch />
      </HeaderIconButton>
    )
  }

  if (slot === 'add') {
    return (
      <HeaderIconButton
        label="Добавить"
        onClick={() => {
          const path = window.location.pathname
          if (path.startsWith('/categories')) {
            navigate('/categories?new=1')
            window.dispatchEvent(new CustomEvent('header-add'))
            return
          }
          document.getElementById('account-add-input')?.focus()
        }}
      >
        <IconPlus />
      </HeaderIconButton>
    )
  }

  if (slot === 'export') {
    return (
      <HeaderIconButton label="Экспорт" to="/backup">
        <IconExport />
      </HeaderIconButton>
    )
  }

  return null
}
