import { useMemo } from 'react'
import type { IOUBalance } from '../types'
import { PremiumGate } from '../components/PremiumGate'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { formatMoney, netIOUBalances } from '../data/mockData'
import { getTheme, radius, shadows } from '../theme'

function IconCloseDebt() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path d="M5 12l5 5L20 7" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

function MemberAvatar({ avatar, size = 40 }: { avatar: string; size?: number }) {
  return (
    <span
      style={{
        width: size,
        height: size,
        borderRadius: size / 2,
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: Math.round(size * 0.52),
        lineHeight: 1,
        flexShrink: 0,
        background: '#fff',
        border: '2px solid rgba(45, 106, 79, 0.28)',
        boxShadow: shadows.sm,
      }}
    >
      {avatar}
    </span>
  )
}

function DebtCard({
  bal,
  getMember,
  theme,
}: {
  bal: IOUBalance
  getMember: (id: string) => { name: string; avatar: string } | undefined
  theme: ReturnType<typeof getTheme>
}) {
  const from = getMember(bal.fromId)
  const to = getMember(bal.toId)

  return (
    <div
      style={{
        padding: '16px',
        marginBottom: 10,
        borderRadius: radius.lg,
        background: theme.surface,
        border: `2px solid ${theme.primary}45`,
        boxShadow: shadows.card,
      }}
    >
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 10,
          marginBottom: 14,
        }}
      >
        <div style={{ textAlign: 'center', minWidth: 64 }}>
          <MemberAvatar avatar={from?.avatar ?? '👤'} size={44} />
          <p style={{ margin: '6px 0 0', fontSize: 12, fontWeight: 600, color: theme.text }}>
            {from?.name ?? '?'}
          </p>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4, flexShrink: 0 }}>
          <span style={{ fontSize: 11, fontWeight: 700, color: theme.textMuted, textTransform: 'uppercase' }}>
            должен
          </span>
          <svg width="28" height="14" viewBox="0 0 28 14" fill="none" aria-hidden>
            <path d="M2 7h20M16 2l6 5-6 5" stroke={theme.primary} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </div>

        <div style={{ textAlign: 'center', minWidth: 64 }}>
          <MemberAvatar avatar={to?.avatar ?? '👤'} size={44} />
          <p style={{ margin: '6px 0 0', fontSize: 12, fontWeight: 600, color: theme.text }}>
            {to?.name ?? '?'}
          </p>
        </div>
      </div>

      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          gap: 12,
          padding: '12px 14px',
          borderRadius: radius.md,
          background: `${theme.expense}10`,
          border: `2px solid ${theme.expense}35`,
          marginBottom: 12,
        }}
      >
        <span style={{ fontSize: 13, fontWeight: 600, color: theme.textSecondary }}>Сумма долга</span>
        <span style={{ fontSize: 22, fontWeight: 700, color: theme.expense }}>{formatMoney(bal.amount)}</span>
      </div>

      <button
        type="button"
        style={{
          width: '100%',
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 8,
          padding: '12px 16px',
          borderRadius: radius.md,
          border: 'none',
          background: theme.primary,
          color: '#fff',
          fontWeight: 700,
          fontSize: 14,
          cursor: 'pointer',
          boxShadow: '0 4px 14px rgba(45, 106, 79, 0.35)',
        }}
      >
        <IconCloseDebt />
        Закрыть долг
      </button>
    </div>
  )
}

export function BalancesScreen() {
  const { family, iouBalances, settings } = useApp()
  const theme = getTheme(settings.theme)

  useSetPageHeader({ showBack: true, backTo: '/more' })

  const netted = useMemo(() => netIOUBalances(iouBalances), [iouBalances])
  const totalDebt = netted.reduce((s, b) => s + b.amount, 0)

  const getMember = (id: string) => family.find((m) => m.id === id)

  return (
    <PremiumGate feature="Балансы IOU">
      <div style={{ padding: '0 16px 24px' }}>
        <div
          style={{
            position: 'relative',
            padding: '18px 20px 16px',
            borderRadius: radius.xl,
            background: `linear-gradient(145deg, ${theme.primary} 0%, ${theme.primaryDark} 100%)`,
            color: '#fff',
            boxShadow: shadows.hero,
            marginBottom: 16,
            overflow: 'hidden',
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
              background: 'rgba(255,255,255,0.06)',
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
                fontSize: 40,
                lineHeight: 1,
              }}
            >
              🤝
            </span>
            <div style={{ flex: 1, minWidth: 0 }}>
              <p style={{ margin: 0, fontSize: 13, opacity: 0.88, fontWeight: 500 }}>Открытые долги</p>
              <p style={{ margin: '4px 0 0', fontSize: 28, fontWeight: 700, letterSpacing: -0.5, lineHeight: 1.1 }}>
                {netted.length === 0 ? '0 ₽' : formatMoney(totalDebt)}
              </p>
              <p style={{ margin: '4px 0 0', fontSize: 12, opacity: 0.8 }}>
                {netted.length === 0
                  ? 'Все расчёты закрыты'
                  : `${netted.length} ${netted.length === 1 ? 'долг' : netted.length < 5 ? 'долга' : 'долгов'} · с учётом взаимных`}
              </p>
            </div>
          </div>
        </div>

        {netted.length === 0 ? (
          <div
            style={{
              padding: '32px 20px',
              borderRadius: radius.lg,
              background: theme.surface,
              border: `2px solid ${theme.primary}45`,
              boxShadow: shadows.card,
              textAlign: 'center',
            }}
          >
            <p style={{ margin: 0, fontSize: 40, lineHeight: 1 }}>✓</p>
            <p style={{ margin: '12px 0 4px', fontSize: 16, fontWeight: 700, color: theme.text }}>
              Все расчёты закрыты
            </p>
            <p style={{ margin: 0, fontSize: 14, color: theme.textMuted, lineHeight: 1.45 }}>
              Когда появятся долги между участниками семьи, они отобразятся здесь
            </p>
          </div>
        ) : (
          netted.map((bal) => (
            <DebtCard key={`${bal.fromId}-${bal.toId}`} bal={bal} getMember={getMember} theme={theme} />
          ))
        )}
      </div>
    </PremiumGate>
  )
}
