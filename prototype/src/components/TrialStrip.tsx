import { Link } from 'react-router-dom'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

/** Баннер trial — показывается в «Ещё», не на главной */
export function TrialBanner() {
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
        gap: 12,
        marginBottom: 12,
        padding: '14px 16px',
        borderRadius: 12,
        background: theme.premiumBg,
        border: `1px solid ${theme.premium}35`,
        textDecoration: 'none',
        color: theme.text,
      }}
    >
      <span style={{ fontSize: 28, lineHeight: 1 }}>⭐</span>
      <div style={{ flex: 1 }}>
        <p style={{ margin: 0, fontSize: 15, fontWeight: 700, color: theme.premium }}>
          Premium · {days} дн. бесплатно
        </p>
        <p style={{ margin: '4px 0 0', fontSize: 13, color: theme.textSecondary }}>
          Семья, аналитика и синхронизация
        </p>
      </div>
      <span style={{ color: theme.premium, fontSize: 18 }}>›</span>
    </Link>
  )
}
