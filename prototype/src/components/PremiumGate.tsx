import { Link } from 'react-router-dom'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

interface PremiumGateProps {
  children: React.ReactNode
  feature?: string
}

export function PremiumGate({ children, feature }: PremiumGateProps) {
  const { settings } = useApp()
  const theme = getTheme(settings.theme)
  const hasAccess =
    settings.isPremium ||
    (settings.trialEndsAt != null && new Date(settings.trialEndsAt) > new Date())

  if (hasAccess) return <>{children}</>

  return (
    <div
      style={{
        padding: 24,
        textAlign: 'center',
        background: theme.premiumBg,
        borderRadius: 16,
        margin: 16,
      }}
    >
      <div style={{ fontSize: 32, marginBottom: 8 }}>⭐</div>
      <h3 style={{ margin: '0 0 8px', color: theme.text }}>Premium</h3>
      <p style={{ margin: '0 0 16px', color: theme.textSecondary, fontSize: 14 }}>
        {feature ?? 'Эта функция'} доступна в Premium. 7 дней бесплатно!
      </p>
      <Link
        to="/premium"
        style={{
          display: 'inline-block',
          padding: '12px 24px',
          background: theme.premium,
          color: '#fff',
          borderRadius: 12,
          textDecoration: 'none',
          fontWeight: 600,
        }}
      >
        Попробовать Premium
      </Link>
    </div>
  )
}

export function PremiumBadge({ variant = 'default' }: { variant?: 'default' | 'onDark' }) {
  const { settings, daysLeftInTrial } = useApp()
  const theme = getTheme(settings.theme)

  const onDark = variant === 'onDark'

  if (settings.isPremium) {
    return (
      <span
        style={{
          fontSize: 11,
          fontWeight: 600,
          padding: onDark ? '5px 10px' : '2px 8px',
          borderRadius: onDark ? 20 : 8,
          background: onDark ? 'rgba(255,255,255,0.18)' : theme.premiumBg,
          color: onDark ? '#FFF8E7' : theme.premium,
          border: onDark ? '1px solid rgba(255,255,255,0.28)' : 'none',
          backdropFilter: onDark ? 'blur(4px)' : undefined,
        }}
      >
        Premium
      </span>
    )
  }

  const days = daysLeftInTrial()
  if (days > 0) {
    return (
      <span
        style={{
          fontSize: 11,
          fontWeight: 600,
          padding: onDark ? '5px 10px' : '2px 8px',
          borderRadius: onDark ? 20 : 8,
          background: onDark ? 'rgba(255,255,255,0.18)' : theme.premiumBg,
          color: onDark ? '#FFF8E7' : theme.premium,
          border: onDark ? '1px solid rgba(255,255,255,0.28)' : 'none',
          backdropFilter: onDark ? 'blur(4px)' : undefined,
        }}
      >
        Trial · {days} дн.
      </span>
    )
  }

  return null
}
