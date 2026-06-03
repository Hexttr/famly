import { Link, useNavigate } from 'react-router-dom'
import { AppLogo } from './AppLogo'
import { HeaderLeft, HeaderRight } from './HeaderActions'
import { useHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { getTheme, getHeaderButtonTop, headerLayout } from '../theme'

export function AppHeader() {
  const { config } = useHeader()
  const { settings } = useApp()
  const theme = getTheme(settings.theme)
  const navigate = useNavigate()
  const buttonTop = getHeaderButtonTop()

  const handleBack = () => {
    if (config.backTo) navigate(config.backTo)
    else navigate(-1)
  }

  const sideStyle = {
    position: 'absolute' as const,
    top: buttonTop,
  }

  const backSize = headerLayout.buttonSize

  const leftContent = config.showBack ? (
    <button
      type="button"
      onClick={handleBack}
      aria-label="Назад"
      style={{
        ...sideStyle,
        left: '11%',
        transform: 'translateX(-50%)',
        border: `2px solid ${theme.primary}50`,
        background: theme.surface,
        boxShadow: '0 2px 10px rgba(45, 106, 79, 0.18)',
        borderRadius: backSize / 2,
        width: backSize,
        height: backSize,
        cursor: 'pointer',
        color: theme.primary,
        fontSize: 20,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      ←
    </button>
  ) : (
    <div style={{ ...sideStyle, left: '11%', transform: 'translateX(-50%)' }}>
      <HeaderLeft slot={config.leftSlot} />
    </div>
  )

  return (
    <header
      style={{
        position: 'sticky',
        top: 0,
        zIndex: 50,
        height: headerLayout.height,
        padding: 0,
        background: theme.background,
        flexShrink: 0,
      }}
    >
      <Link
        to="/"
        style={{
          position: 'absolute',
          left: '50%',
          top: headerLayout.inset,
          transform: 'translateX(-50%)',
          display: 'flex',
          textDecoration: 'none',
        }}
      >
        <AppLogo variant="header" />
      </Link>

      {leftContent}

      <div style={{ ...sideStyle, right: '11%', transform: 'translateX(50%)' }}>
        <HeaderRight slot={config.rightSlot} />
      </div>
    </header>
  )
}
