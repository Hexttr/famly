import { Link } from 'react-router-dom'
import { useHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

export function AppHeader() {
  const { config } = useHeader()
  const { settings } = useApp()
  const theme = getTheme(settings.theme)

  return (
    <header
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0 16px',
        background: theme.surface,
        borderBottom: `1px solid ${theme.border}`,
        height: 56,
        flexShrink: 0,
        position: 'sticky',
        top: 0,
        zIndex: 50,
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, minWidth: 0 }}>
        {config.showBack && config.onBack && (
          <button
            type="button"
            onClick={config.onBack}
            aria-label="Назад"
            style={{
              border: 'none',
              background: theme.surfaceAlt,
              borderRadius: 10,
              width: 36,
              height: 36,
              cursor: 'pointer',
              color: theme.primary,
              fontSize: 18,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0,
            }}
          >
            ←
          </button>
        )}
        <Link
          to="/"
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 10,
            textDecoration: 'none',
            minWidth: 0,
          }}
        >
          <img
            src="/famly-logo.png"
            alt="Famly"
            style={{
              height: 56,
              width: 'auto',
              maxWidth: 'min(220px, 55vw)',
              objectFit: 'contain',
              objectPosition: 'left center',
              flexShrink: 0,
              display: 'block',
            }}
          />
        </Link>
      </div>

      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 8,
          flexShrink: 0,
          marginLeft: 12,
        }}
      >
        {config.right}
      </div>
    </header>
  )
}
