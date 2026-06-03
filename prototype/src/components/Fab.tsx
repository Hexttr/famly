import { useLocation } from 'react-router-dom'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

export function Fab() {
  const location = useLocation()
  const { openQuickAdd, settings } = useApp()
  const theme = getTheme(settings.theme)

  if (location.pathname === '/') return null

  return (
    <button
      type="button"
      onClick={() => openQuickAdd()}
      aria-label="Добавить операцию"
      style={{
        position: 'fixed',
        bottom: 72,
        right: 20,
        width: 56,
        height: 56,
        borderRadius: 28,
        border: 'none',
        background: theme.primary,
        color: '#fff',
        cursor: 'pointer',
        boxShadow: '0 4px 12px rgba(45,106,79,0.35)',
        zIndex: 100,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 0,
      }}
    >
      <svg width="26" height="26" viewBox="0 0 24 24" fill="none" aria-hidden>
        <path
          d="M12 5v14M5 12h14"
          stroke="#fff"
          strokeWidth="2.5"
          strokeLinecap="round"
        />
      </svg>
    </button>
  )
}
