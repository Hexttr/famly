import { Link } from 'react-router-dom'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

/** Компактный trial-баннер — только на главной, под hero-карточкой */
export function TrialStrip() {
  const { settings, daysLeftInTrial } = useApp()
  const theme = getTheme(settings.theme)

  if (settings.isPremium) return null

  const days = daysLeftInTrial()
  if (days <= 0) return null

  return (
    <Link
      to="/premium"
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 5,
        margin: '0 16px 12px',
        padding: '6px 12px',
        borderRadius: 10,
        background: theme.premiumBg,
        color: theme.premium,
        fontSize: 11,
        fontWeight: 600,
        textDecoration: 'none',
      }}
    >
      <span>⭐</span>
      <span>Premium · {days} дн. бесплатно</span>
      <span style={{ opacity: 0.6, fontSize: 10 }}>→</span>
    </Link>
  )
}
