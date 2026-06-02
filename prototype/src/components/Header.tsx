import { Link } from 'react-router-dom'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

interface HeaderProps {
  title: string
  backTo?: string
  action?: React.ReactNode
}

export function Header({ title, backTo, action }: HeaderProps) {
  const { settings } = useApp()
  const theme = getTheme(settings.theme)

  return (
    <header
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '12px 16px',
        background: theme.surface,
        borderBottom: `1px solid ${theme.border}`,
        minHeight: 52,
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, flex: 1 }}>
        {backTo && (
          <Link
            to={backTo}
            style={{ color: theme.primary, textDecoration: 'none', fontSize: 22, lineHeight: 1 }}
          >
            ←
          </Link>
        )}
        <h1 style={{ margin: 0, fontSize: 17, fontWeight: 600, color: theme.text }}>{title}</h1>
      </div>
      {action}
    </header>
  )
}
