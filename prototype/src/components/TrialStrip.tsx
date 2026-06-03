import { Link } from 'react-router-dom'
import { useApp } from '../context/AppContext'
import { getTheme, radius, shadows } from '../theme'

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
        borderRadius: radius.md,
        background: theme.premiumBg,
        border: `2px solid ${theme.premium}55`,
        boxShadow: shadows.card,
        textDecoration: 'none',
        color: theme.text,
      }}
    >
      <span
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          width: 40,
          height: 40,
          borderRadius: 20,
          background: `${theme.premium}20`,
          border: `2px solid ${theme.premium}55`,
          flexShrink: 0,
        }}
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden>
          <path
            d="M12 2l2.4 7.4H22l-6 4.6 2.3 7L12 16.8 5.7 21l2.3-7-6-4.6h7.6L12 2z"
            fill={theme.premium}
          />
        </svg>
      </span>
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
