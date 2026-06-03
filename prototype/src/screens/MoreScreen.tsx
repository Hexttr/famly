import { Link } from 'react-router-dom'
import { TrialBanner } from '../components/TrialStrip'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

const menuItems = [
  { to: '/accounts', icon: '💳', label: 'Счета' },
  { to: '/reports', icon: '📈', label: 'Отчёты' },
  { to: '/settings', icon: '⚙️', label: 'Настройки' },
  { to: '/backup', icon: '💾', label: 'Backup и экспорт' },
  { to: '/family', icon: '👨‍👩‍👧', label: 'Семья', premium: true },
  { to: '/balances', icon: '⚖️', label: 'Балансы (IOU)', premium: true },
  { to: '/analytics', icon: '📉', label: 'Аналитика', premium: true },
  { to: '/premium', icon: '⭐', label: 'Premium' },
]

export function MoreScreen() {
  const { settings } = useApp()
  const theme = getTheme(settings.theme)

  useSetPageHeader({ rightSlot: 'settings' })

  return (
    <div style={{ padding: '0 16px 16px' }}>
      <TrialBanner />
      {menuItems.map((item) => (
        <Link
          key={item.to}
          to={item.to}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 14,
            padding: 16,
            marginBottom: 8,
            borderRadius: 12,
            background: theme.surface,
            border: `1px solid ${theme.border}`,
            textDecoration: 'none',
            color: theme.text,
          }}
        >
          <span style={{ fontSize: 24 }}>{item.icon}</span>
          <span style={{ flex: 1, fontWeight: 500 }}>{item.label}</span>
          {item.premium && (
            <span style={{ fontSize: 11, color: theme.premium, fontWeight: 600 }}>Premium</span>
          )}
          <span style={{ color: theme.textMuted }}>›</span>
        </Link>
      ))}

      <p style={{ textAlign: 'center', fontSize: 12, color: theme.textMuted, padding: 16 }}>
        Мой (Наш) Бюджет v0.1.0 · Сделано в России
      </p>
    </div>
  )
}
