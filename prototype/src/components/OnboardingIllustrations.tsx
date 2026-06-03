import { CategoryIcon } from './CategoryIcon'

interface IllustrationProps {
  /** Ширина контейнера — обычно 100% от родителя */
  maxWidth?: number
}

const DEFAULT_MAX = 340

/** Экран 2 — мини-версия hero-карточки и быстрых категорий с главной */
export function BudgetIllustration({ maxWidth = DEFAULT_MAX }: IllustrationProps) {
  return (
    <div
      style={{
        width: '100%',
        maxWidth,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'stretch',
        gap: 10,
        margin: '0 auto',
      }}
    >
      <div
        style={{
          width: '100%',
          padding: '18px 20px 16px',
          borderRadius: 20,
          background: 'linear-gradient(145deg, #2D6A4F 0%, #1B4332 100%)',
          color: '#fff',
          boxShadow: '0 8px 28px rgba(27, 67, 50, 0.28)',
          textAlign: 'left',
        }}
      >
        <div
          style={{
            display: 'inline-flex',
            padding: '4px 10px',
            borderRadius: 999,
            background: 'rgba(255,255,255,0.14)',
            fontSize: 11,
            fontWeight: 600,
            marginBottom: 10,
          }}
        >
          Июнь 2026
        </div>
        <p style={{ margin: '0 0 4px', fontSize: 12, opacity: 0.88, fontWeight: 500 }}>
          Можно тратить
        </p>
        <p
          style={{
            margin: '0 0 12px',
            fontSize: 32,
            fontWeight: 700,
            letterSpacing: -0.5,
            lineHeight: 1.1,
          }}
        >
          47 114 ₽
        </p>
        <div
          style={{
            height: 6,
            borderRadius: 3,
            background: 'rgba(255,255,255,0.22)',
            overflow: 'hidden',
          }}
        >
          <div style={{ width: '18%', height: '100%', background: '#fff', borderRadius: 3 }} />
        </div>
        <p style={{ margin: '8px 0 0', fontSize: 11, opacity: 0.85 }}>
          Потрачено 8 886 ₽ из 56 000 ₽
        </p>
      </div>

      <div style={{ display: 'flex', gap: 8, width: '100%' }}>
        {(
          [
            { id: 'groceries', label: 'Продукты' },
            { id: 'transport', label: 'Транспорт' },
            { id: 'cafe', label: 'Кафе' },
            { id: 'home', label: 'ЖКХ' },
          ] as const
        ).map((cat) => (
          <div
            key={cat.id}
            style={{
              flex: 1,
              minWidth: 0,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: 6,
              padding: '10px 4px',
              borderRadius: 12,
              background: '#fff',
              border: '2px solid #E2E8E5',
              boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
            }}
          >
            <CategoryIcon iconId={cat.id} size={18} variant="circle" emphasis />
            <span
              style={{
                fontSize: 10,
                fontWeight: 600,
                color: '#1A1D1A',
                lineHeight: 1.15,
                textAlign: 'center',
              }}
            >
              {cat.label}
            </span>
          </div>
        ))}
      </div>
    </div>
  )
}

/** Экран 3 — семейный бюджет: аватары + общая сумма */
export function FamilyIllustration({ maxWidth = DEFAULT_MAX }: IllustrationProps) {
  const avatars = [
    { letter: 'А', bg: '#2D6A4F' },
    { letter: 'М', bg: '#40916C' },
    { letter: 'С', bg: '#52B788' },
  ]

  return (
    <div
      style={{
        width: '100%',
        maxWidth,
        margin: '0 auto',
      }}
    >
      <div
        style={{
          width: '100%',
          padding: '24px 20px 22px',
          borderRadius: 20,
          background: 'linear-gradient(160deg, #FFF8E7 0%, #FFFFFF 60%)',
          border: '2px solid rgba(212, 160, 23, 0.35)',
          boxShadow: '0 6px 24px rgba(212, 160, 23, 0.14)',
          textAlign: 'center',
        }}
      >
        <div
          style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            marginBottom: 16,
          }}
        >
          {avatars.map((a, i) => (
            <div
              key={a.letter}
              style={{
                width: 52,
                height: 52,
                borderRadius: 26,
                background: a.bg,
                border: '3px solid #fff',
                marginLeft: i > 0 ? -12 : 0,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#fff',
                fontSize: 18,
                fontWeight: 700,
                boxShadow: '0 2px 10px rgba(27, 67, 50, 0.22)',
                zIndex: avatars.length - i,
                position: 'relative',
              }}
            >
              {a.letter}
            </div>
          ))}
        </div>

        <div
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: 6,
            padding: '6px 14px',
            borderRadius: 999,
            background: '#D4A017',
            color: '#fff',
            fontSize: 12,
            fontWeight: 700,
            marginBottom: 14,
            boxShadow: '0 2px 8px rgba(212, 160, 23, 0.35)',
          }}
        >
          <svg width="13" height="13" viewBox="0 0 24 24" fill="#fff" aria-hidden>
            <path d="M12 2l2.4 7.4H22l-6 4.6 2.3 7L12 16.8 5.7 21l2.3-7-6-4.6h7.6L12 2z" />
          </svg>
          7 дней бесплатно
        </div>

        <p style={{ margin: '0 0 6px', fontSize: 14, fontWeight: 700, color: '#1A1D1A' }}>
          Общий бюджет семьи
        </p>
        <p style={{ margin: 0, fontSize: 28, fontWeight: 700, color: '#2D6A4F', letterSpacing: -0.5 }}>
          156 800 ₽
        </p>
        <p
          style={{
            margin: '10px 0 0',
            fontSize: 12,
            color: '#8A9390',
            lineHeight: 1.45,
          }}
        >
          Синхронизация · деление трат · долги
        </p>
      </div>
    </div>
  )
}

export const ONBOARDING_ART_MAX_WIDTH = DEFAULT_MAX
