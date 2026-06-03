import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { AppLogo } from '../components/AppLogo'
import {
  BudgetIllustration,
  FamilyIllustration,
  ONBOARDING_ART_MAX_WIDTH,
} from '../components/OnboardingIllustrations'
import { useApp } from '../context/AppContext'
import { getTheme, radius } from '../theme'

const LOGO_SIZE = 168

const slides = [
  {
    kind: 'logo' as const,
    title: 'Запись за 3 секунды',
    text: 'Добавляйте расходы одним тапом. Без сложных форм и лишних экранов.',
  },
  {
    kind: 'budget' as const,
    title: 'Бюджет под вашу зарплату',
    text: 'Настраиваемые периоды, лимиты по категориям. Все для Вашего удобства!',
  },
  {
    kind: 'family' as const,
    title: '7 дней бесплатно',
    text: 'Семейный бюджет, гибкая настройка — попробуйте без оплаты',
  },
]

function IconArrowRight() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

function IconSkip() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
    </svg>
  )
}

function IconStart() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M5 12l5 5L20 7"
        stroke="currentColor"
        strokeWidth="2.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

function SlideVisual({ kind }: { kind: (typeof slides)[number]['kind'] }) {
  if (kind === 'logo') {
    return <AppLogo size={LOGO_SIZE} />
  }
  if (kind === 'budget') {
    return <BudgetIllustration maxWidth={ONBOARDING_ART_MAX_WIDTH} />
  }
  return <FamilyIllustration maxWidth={ONBOARDING_ART_MAX_WIDTH} />
}

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
  const isLast = step === slides.length - 1

  return (
    <div
      style={{
        width: '100%',
        height: '100dvh',
        display: 'flex',
        flexDirection: 'column',
        background: theme.background,
        overflow: 'hidden',
      }}
    >
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', padding: '28px 20px 24px' }}>
        <div
          style={{
            flex: 1,
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            alignItems: 'center',
            textAlign: 'center',
            width: '100%',
          }}
        >
          <div
            style={{
              marginBottom: 28,
              width: '100%',
              maxWidth: ONBOARDING_ART_MAX_WIDTH,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              ...(slide.kind === 'logo'
                ? { width: LOGO_SIZE, height: LOGO_SIZE, maxWidth: LOGO_SIZE }
                : {}),
            }}
          >
            <SlideVisual kind={slide.kind} />
          </div>
          <h1
            style={{
              margin: '0 0 12px',
              fontSize: 24,
              fontWeight: 700,
              color: theme.text,
              maxWidth: ONBOARDING_ART_MAX_WIDTH,
              width: '100%',
            }}
          >
            {slide.title}
          </h1>
          <p
            style={{
              margin: 0,
              color: theme.textSecondary,
              fontSize: 16,
              lineHeight: 1.55,
              maxWidth: ONBOARDING_ART_MAX_WIDTH,
              width: '100%',
            }}
          >
            {slide.text}
          </p>
        </div>

        <div
          style={{
            width: '100%',
            maxWidth: ONBOARDING_ART_MAX_WIDTH,
            margin: '0 auto',
          }}
        >
        <div style={{ display: 'flex', justifyContent: 'center', gap: 10, marginBottom: 24 }}>
          {slides.map((_, i) => (
            <div
              key={i}
              style={{
                width: i === step ? 28 : 10,
                height: 10,
                borderRadius: 5,
                background: i === step ? theme.primary : 'transparent',
                border: `2px solid ${i === step ? theme.primary : `${theme.primary}50`}`,
                transition: 'all 0.2s ease',
              }}
            />
          ))}
        </div>

        <button
          type="button"
          onClick={() => (isLast ? finish() : setStep(step + 1))}
          style={{
            width: '100%',
            padding: 16,
            border: 'none',
            borderRadius: radius.md,
            background: theme.primary,
            color: '#fff',
            fontSize: 16,
            fontWeight: 600,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 8,
            boxShadow: '0 4px 14px rgba(45, 106, 79, 0.35)',
          }}
        >
          {isLast ? (
            <>
              Начать
              <IconStart />
            </>
          ) : (
            <>
              Далее
              <IconArrowRight />
            </>
          )}
        </button>

        {!isLast && (
          <button
            type="button"
            onClick={finish}
            style={{
              marginTop: 12,
              width: '100%',
              padding: '14px 16px',
              border: `2px solid ${theme.primary}50`,
              borderRadius: radius.md,
              background: theme.surface,
              color: theme.textSecondary,
              fontSize: 15,
              fontWeight: 600,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 8,
              boxShadow: '0 2px 10px rgba(45, 106, 79, 0.18)',
            }}
          >
            <IconSkip />
            Пропустить
          </button>
        )}
        </div>
      </div>
    </div>
  )
}
