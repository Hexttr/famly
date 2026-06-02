import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

const slides = [
  {
    icon: '⚡',
    title: 'Запись за 3 секунды',
    text: 'Добавляйте расходы одним тапом. Без сложных форм и лишних экранов.',
  },
  {
    icon: '📊',
    title: 'Бюджет под вашу зарплату',
    text: 'Период с 28-го, safe-to-spend и лимиты по категориям — бесплатно.',
  },
  {
    icon: '👨‍👩‍👧',
    title: '7 дней Premium бесплатно',
    text: 'Семейный бюджет, синхронизация и split расходов — попробуйте без оплаты.',
  },
]

export function OnboardingScreen() {
  const [step, setStep] = useState(0)
  const { completeOnboarding, settings } = useApp()
  const theme = getTheme(settings.theme)
  const navigate = useNavigate()

  const finish = () => {
    completeOnboarding()
    navigate('/')
  }

  const slide = slides[step]

  return (
    <div
      style={{
        maxWidth: 375,
        margin: '0 auto',
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        background: theme.background,
        boxShadow: '0 0 40px rgba(0,0,0,0.08)',
      }}
    >
      <header
        style={{
          padding: '12px 16px',
          borderBottom: `1px solid ${theme.border}`,
          background: theme.surface,
        }}
      >
        <img src="/famly-logo.png" alt="Famly" style={{ height: 36, objectFit: 'contain' }} />
      </header>

      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', padding: 24 }}>
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', textAlign: 'center' }}>
          <div style={{ fontSize: 64, marginBottom: 24 }}>{slide.icon}</div>
          <h1 style={{ margin: '0 0 12px', fontSize: 24, color: theme.text }}>{slide.title}</h1>
          <p style={{ margin: 0, color: theme.textSecondary, fontSize: 16, lineHeight: 1.5, maxWidth: 280 }}>
            {slide.text}
          </p>
        </div>

        <div style={{ display: 'flex', justifyContent: 'center', gap: 8, marginBottom: 24 }}>
          {slides.map((_, i) => (
            <div
              key={i}
              style={{
                width: i === step ? 24 : 8,
                height: 8,
                borderRadius: 4,
                background: i === step ? theme.primary : theme.border,
                transition: 'width 0.2s',
              }}
            />
          ))}
        </div>

        <button
          type="button"
          onClick={() => (step < slides.length - 1 ? setStep(step + 1) : finish())}
          style={{
            width: '100%',
            padding: 16,
            border: 'none',
            borderRadius: 12,
            background: theme.primary,
            color: '#fff',
            fontSize: 16,
            fontWeight: 600,
            cursor: 'pointer',
          }}
        >
          {step < slides.length - 1 ? 'Далее' : 'Начать'}
        </button>

        {step < slides.length - 1 && (
          <button
            type="button"
            onClick={finish}
            style={{
              marginTop: 12,
              border: 'none',
              background: 'none',
              color: theme.textMuted,
              fontSize: 14,
              cursor: 'pointer',
            }}
          >
            Пропустить
          </button>
        )}
      </div>
    </div>
  )
}
