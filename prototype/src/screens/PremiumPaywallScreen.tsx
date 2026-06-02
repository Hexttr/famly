import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Header } from '../components/Header'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

const freeFeatures = [
  'Неограниченный ввод операций',
  'Бюджет по категориям',
  'Safe-to-spend',
  'Свой бюджетный период',
  'Backup JSON + CSV 30 дней',
]

const premiumFeatures = [
  'Семья до 6 человек',
  'Облачная синхронизация',
  'Роли и приватность',
  'Split расходов + IOU',
  'Rollover бюджета',
  'Расширенная аналитика',
  'CSV без ограничений',
  'Виджет быстрого ввода',
]

export function PremiumPaywallScreen() {
  const { activatePremium, daysLeftInTrial, settings } = useApp()
  const theme = getTheme(settings.theme)
  const navigate = useNavigate()
  const [plan, setPlan] = useState<'monthly' | 'yearly'>('yearly')

  const trialDays = daysLeftInTrial()

  const handleSubscribe = () => {
    activatePremium()
    navigate(-1)
  }

  return (
    <div style={{ minHeight: '100vh', background: theme.background }}>
      <Header title="Premium" backTo="/more" />

      <div style={{ padding: 24, textAlign: 'center' }}>
        <div style={{ fontSize: 48, marginBottom: 8 }}>⭐</div>
        <h2 style={{ margin: '0 0 8px', color: theme.text }}>Famly Premium</h2>
        {trialDays > 0 && (
          <p style={{ margin: 0, color: theme.premium, fontWeight: 600 }}>
            Trial: осталось {trialDays} дн.
          </p>
        )}
      </div>

      <div style={{ display: 'flex', gap: 8, padding: '0 16px', marginBottom: 16 }}>
        {([
          ['monthly', '199 ₽/мес'],
          ['yearly', '1500 ₽/год'],
        ] as const).map(([key, label]) => (
          <button
            key={key}
            type="button"
            onClick={() => setPlan(key)}
            style={{
              flex: 1,
              padding: 14,
              borderRadius: 12,
              border: plan === key ? `2px solid ${theme.premium}` : `1px solid ${theme.border}`,
              background: plan === key ? theme.premiumBg : theme.surface,
              cursor: 'pointer',
              fontWeight: 600,
              color: theme.text,
            }}
          >
            {label}
            {key === 'yearly' && (
              <div style={{ fontSize: 11, color: theme.premium, marginTop: 4 }}>−37%</div>
            )}
          </button>
        ))}
      </div>

      <div style={{ padding: '0 16px', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
        <div style={{ padding: 16, borderRadius: 12, background: theme.surface, border: `1px solid ${theme.border}` }}>
          <h3 style={{ margin: '0 0 12px', fontSize: 14, color: theme.text }}>Free</h3>
          {freeFeatures.map((f) => (
            <p key={f} style={{ margin: '0 0 6px', fontSize: 12, color: theme.textSecondary }}>✓ {f}</p>
          ))}
        </div>
        <div style={{ padding: 16, borderRadius: 12, background: theme.premiumBg, border: `1px solid ${theme.premium}40` }}>
          <h3 style={{ margin: '0 0 12px', fontSize: 14, color: theme.premium }}>Premium</h3>
          {premiumFeatures.map((f) => (
            <p key={f} style={{ margin: '0 0 6px', fontSize: 12, color: theme.text }}>✓ {f}</p>
          ))}
        </div>
      </div>

      <div style={{ padding: 16, marginTop: 8 }}>
        <button
          type="button"
          onClick={handleSubscribe}
          style={{
            width: '100%',
            padding: 16,
            border: 'none',
            borderRadius: 12,
            background: theme.premium,
            color: '#fff',
            fontSize: 16,
            fontWeight: 700,
            cursor: 'pointer',
          }}
        >
          {trialDays > 0 ? 'Продолжить Premium' : `Оформить — ${plan === 'monthly' ? '199 ₽/мес' : '1500 ₽/год'}`}
        </button>
        <p style={{ textAlign: 'center', fontSize: 11, color: theme.textMuted, marginTop: 12 }}>
          Оплата через RuStore · Отмена в любой момент
        </p>
      </div>
    </div>
  )
}
