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

  const inactiveColor = 'rgba(255, 255, 255, 0.55)'
  const activeColor = '#FFFFFF'

  return (
    <nav
      aria-label="Основная навигация"
      style={{
        position: 'fixed',
        left: 0,
        right: 0,
        bottom: 0,
        zIndex: 90,
        display: 'flex',
        background: theme.primaryDark,
        paddingBottom: 'env(safe-area-inset-bottom, 0)',
        boxShadow: '0 -4px 24px rgba(27, 67, 50, 0.35)',
      }}
    >
      {tabs.map((tab) => (
        <NavLink
          key={tab.to}
          to={tab.to}
          end={tab.to === '/'}
          aria-label={tab.label}
          title={tab.label}
          style={{
            flex: 1,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            minHeight: 52,
            padding: '8px 4px',
            textDecoration: 'none',
            WebkitTapHighlightColor: 'transparent',
          }}
        >
          {({ isActive }) => (
            <span
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                width: 44,
                height: 44,
                borderRadius: 22,
                background: isActive ? 'rgba(255, 255, 255, 0.18)' : 'transparent',
                transition: 'background 0.2s ease',
              }}
            >
              <tab.Icon size={24} color={isActive ? activeColor : inactiveColor} />
            </span>
          )}
        </NavLink>
      ))}
    </nav>
  )
}
