import { NavLink } from 'react-router-dom'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'
import { NavIconBudget, NavIconHome, NavIconMore, NavIconOperations } from './NavIcons'

const tabs = [
  { to: '/', label: 'Главная', Icon: NavIconHome },
  { to: '/operations', label: 'Операции', Icon: NavIconOperations },
  { to: '/budget', label: 'Бюджет', Icon: NavIconBudget },
  { to: '/more', label: 'Ещё', Icon: NavIconMore },
]

export function BottomNav() {
  const { settings } = useApp()
  const theme = getTheme(settings.theme)

  return (
    <nav
      style={{
        position: 'fixed',
        left: 0,
        right: 0,
        bottom: 0,
        zIndex: 90,
        display: 'flex',
        borderTop: `1px solid ${theme.border}`,
        background: theme.surface,
        paddingBottom: 'env(safe-area-inset-bottom, 0)',
        boxShadow: '0 -4px 20px rgba(0, 0, 0, 0.04)',
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
            justifyContent: 'center',
            minHeight: 56,
            padding: '6px 4px',
            textDecoration: 'none',
            color: isActive ? theme.primary : theme.textMuted,
            fontSize: 11,
            fontWeight: isActive ? 600 : 500,
            WebkitTapHighlightColor: 'transparent',
          })}
        >
          {({ isActive }) => (
            <>
              <span
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  width: 48,
                  height: 28,
                  borderRadius: 14,
                  marginBottom: 2,
                  background: isActive ? `${theme.primary}14` : 'transparent',
                  transition: 'background 0.2s ease',
                }}
              >
                <tab.Icon size={22} color={isActive ? theme.primary : theme.textMuted} />
              </span>
              {tab.label}
            </>
          )}
        </NavLink>
      ))}
    </nav>
  )
}
