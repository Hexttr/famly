import { NavLink } from 'react-router-dom'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

const tabs = [
  { to: '/', label: 'Главная', icon: '🏠' },
  { to: '/operations', label: 'Операции', icon: '📋' },
  { to: '/budget', label: 'Бюджет', icon: '📊' },
  { to: '/more', label: 'Ещё', icon: '☰' },
]

export function BottomNav() {
  const { settings } = useApp()
  const theme = getTheme(settings.theme)

  return (
    <nav
      style={{
        display: 'flex',
        borderTop: `1px solid ${theme.border}`,
        background: theme.surface,
        paddingBottom: 'env(safe-area-inset-bottom, 0)',
      }}
    >
      {tabs.map((tab) => (
        <NavLink
          key={tab.to}
          to={tab.to}
          end={tab.to === '/'}
          style={({ isActive }) => ({
            flex: 1,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            padding: '8px 4px',
            textDecoration: 'none',
            color: isActive ? theme.primary : theme.textMuted,
            fontSize: 11,
            fontWeight: isActive ? 600 : 500,
          })}
        >
          <span style={{ fontSize: 20, marginBottom: 2 }}>{tab.icon}</span>
          {tab.label}
        </NavLink>
      ))}
    </nav>
  )
}
