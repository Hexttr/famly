import { Link, useNavigate } from 'react-router-dom'
import { AppLogo } from './AppLogo'
import { HeaderLeft, HeaderRight } from './HeaderActions'
import { useHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { getTheme, headerLayout } from '../theme'

export function AppHeader() {
  const { config } = useHeader()
  const { settings } = useApp()
  const theme = getTheme(settings.theme)
  const navigate = useNavigate()

  const handleBack = () => {
    if (config.backTo) navigate(config.backTo)
    else navigate(-1)
  }

  const leftContent = config.showBack ? (
    <button
      type="button"
      onClick={handleBack}
      aria-label="Назад"
      style={{
        border: 'none',
        background: theme.surface,
        borderRadius: 22,
        width: 44,
        height: 44,
        cursor: 'pointer',
        color: theme.primary,
        fontSize: 18,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
      }}
    >
      ←
    </button>
  ) : (
    <HeaderLeft slot={config.leftSlot} />
  )

  return (
    <header
      style={{
        position: 'sticky',
        top: 0,
        zIndex: 50,
        display: 'grid',
        gridTemplateColumns: `${headerLayout.sideWidth}px 1fr ${headerLayout.sideWidth}px`,
        alignItems: 'center',
        columnGap: 8,
        padding: `${headerLayout.inset}px 12px`,
        background: theme.background,
        flexShrink: 0,
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'flex-start', alignItems: 'center' }}>
        {leftContent}
      </div>

      <Link
        to="/"
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          textDecoration: 'none',
        }}
      >
        <AppLogo variant="header" />
      </Link>

      <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center' }}>
        <HeaderRight slot={config.rightSlot} />
      </div>
    </header>
  )
}
