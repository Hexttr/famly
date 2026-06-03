import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { PageShell } from '../components/PageShell'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { getTheme, radius, shadows } from '../theme'

const freeFeatures = [
  'Неограниченный ввод операций',
  'Бюджет по категориям',
  'Дневной лимит трат',
  'Свой бюджетный период',
  'Резервная копия JSON и CSV за 30 дней',
]

const premiumFeatures = [
  'Семья до 6 человек',
  'Облачная синхронизация',
  'Роли и приватность',
  'Делить расходы и учёт долгов',
  'Перенос остатка бюджета',
  'Расширенная аналитика',
  'Выгрузка CSV без ограничений',
  'Виджет быстрого ввода',
]

function FeatureList({ items, theme }: { items: string[]; theme: ReturnType<typeof getTheme> }) {
  return (
    <ul style={{ margin: 0, padding: 0, listStyle: 'none' }}>
      {items.map((f) => (
        <li
          key={f}
          style={{
            display: 'flex',
            alignItems: 'flex-start',
            gap: 8,
            marginBottom: 8,
            fontSize: 12,
            lineHeight: 1.45,
            color: theme.textSecondary,
          }}
        >
          <span style={{ color: theme.primary, fontWeight: 700, flexShrink: 0 }}>✓</span>
          <span>{f}</span>
        </li>
      ))}
    </ul>
  )
}

function PlanCard({
  active,
  label,
  badge,
  onClick,
  theme,
}: {
  active: boolean
  label: string
  badge?: string
  onClick: () => void
  theme: ReturnType<typeof getTheme>
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      style={{
        flex: 1,
        padding: '14px 10px',
        borderRadius: radius.lg,
        border: `2px solid ${active ? theme.premium : `${theme.primary}45`}`,
        background: active ? theme.premiumBg : theme.surface,
        cursor: 'pointer',
        fontWeight: 700,
        fontSize: 15,
        color: theme.text,
        boxShadow: active ? shadows.card : shadows.sm,
      }}
    >
      {label}
      {badge && (
        <div style={{ fontSize: 12, color: theme.premium, marginTop: 6, fontWeight: 700 }}>{badge}</div>
      )}
    </button>
  )
}

function TierCard({
  icon,
  title,
  features,
  variant,
  theme,
}: {
  icon: string
  title: string
  features: string[]
  variant: 'free' | 'premium'
  theme: ReturnType<typeof getTheme>
}) {
  const isPremium = variant === 'premium'

  return (
    <div
      style={{
        padding: '14px 12px',
        borderRadius: radius.lg,
        background: isPremium ? theme.premiumBg : theme.surface,
        border: `2px solid ${isPremium ? `${theme.premium}55` : `${theme.primary}45`}`,
        boxShadow: shadows.card,
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
        <span style={{ fontSize: 20, lineHeight: 1 }}>{icon}</span>
        <h3
          style={{
            margin: 0,
            fontSize: 15,
            fontWeight: 700,
            color: isPremium ? theme.premium : theme.text,
          }}
        >
          {title}
        </h3>
      </div>
      <FeatureList items={features} theme={theme} />
    </div>
  )
}

function PremiumContent() {
  const { activatePremium, daysLeftInTrial, settings } = useApp()
  const theme = getTheme(settings.theme)
  const navigate = useNavigate()
  const [plan, setPlan] = useState<'monthly' | 'yearly'>('yearly')

  useSetPageHeader({ showBack: true, backTo: '/more' })

  const trialDays = daysLeftInTrial()

  const handleSubscribe = () => {
    activatePremium()
    navigate(-1)
  }

  const priceLabel = plan === 'monthly' ? '199 ₽/мес' : '1500 ₽/год'

  return (
    <div style={{ padding: '0 16px 24px' }}>
      <div
        style={{
          position: 'relative',
          padding: '20px 20px 18px',
          borderRadius: radius.xl,
          background: `linear-gradient(145deg, ${theme.premium} 0%, #A67C00 100%)`,
          color: '#fff',
          boxShadow: shadows.hero,
          marginBottom: 16,
          overflow: 'hidden',
          textAlign: 'left',
        }}
      >
        <div
          aria-hidden
          style={{
            position: 'absolute',
            top: -36,
            right: -28,
            width: 120,
            height: 120,
            borderRadius: '50%',
            background: 'rgba(255,255,255,0.08)',
          }}
        />

        <div style={{ position: 'relative', display: 'flex', alignItems: 'stretch', gap: 14 }}>
          <span
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: 58,
              alignSelf: 'stretch',
              minHeight: 58,
              flexShrink: 0,
              fontSize: 38,
              lineHeight: 1,
            }}
          >
            ⭐
          </span>
          <div style={{ flex: 1, minWidth: 0 }}>
            <p style={{ margin: 0, fontSize: 13, opacity: 0.9, fontWeight: 500 }}>Подписка</p>
            <p style={{ margin: '4px 0 0', fontSize: 24, fontWeight: 700, letterSpacing: -0.3, lineHeight: 1.15 }}>
              Премиум — наш бюджет
            </p>
            {trialDays > 0 && (
              <p style={{ margin: '8px 0 0', fontSize: 13, fontWeight: 600, opacity: 0.92 }}>
                Пробный период: осталось {trialDays} дн.
              </p>
            )}
          </div>
        </div>
      </div>

      <div style={{ display: 'flex', gap: 10, marginBottom: 16 }}>
        <PlanCard
          active={plan === 'monthly'}
          label="199 ₽/мес"
          onClick={() => setPlan('monthly')}
          theme={theme}
        />
        <PlanCard
          active={plan === 'yearly'}
          label="1500 ₽/год"
          badge="−37%"
          onClick={() => setPlan('yearly')}
          theme={theme}
        />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, marginBottom: 16 }}>
        <TierCard icon="📋" title="Бесплатно" features={freeFeatures} variant="free" theme={theme} />
        <TierCard icon="⭐" title="Премиум" features={premiumFeatures} variant="premium" theme={theme} />
      </div>

      <button
        type="button"
        onClick={handleSubscribe}
        style={{
          width: '100%',
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 10,
          padding: '16px 20px',
          border: 'none',
          borderRadius: radius.lg,
          background: theme.premium,
          color: '#fff',
          fontSize: 16,
          fontWeight: 700,
          cursor: 'pointer',
          boxShadow: '0 4px 18px rgba(212, 160, 23, 0.45)',
        }}
      >
        <span style={{ fontSize: 20, lineHeight: 1 }}>⭐</span>
        {trialDays > 0 ? 'Подключить Премиум' : `Оформить подписку — ${priceLabel}`}
      </button>

      <p style={{ textAlign: 'center', fontSize: 12, color: theme.textMuted, marginTop: 14, lineHeight: 1.45 }}>
        Оплата через RuStore · Отмена в любой момент
      </p>
    </div>
  )
}

export function PremiumPaywallScreen() {
  return (
    <PageShell>
      <PremiumContent />
    </PageShell>
  )
}
