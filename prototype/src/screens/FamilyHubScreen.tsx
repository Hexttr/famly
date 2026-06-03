import { Link } from 'react-router-dom'
import type { FamilyMember } from '../types'
import { PremiumGate } from '../components/PremiumGate'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { getTheme, radius, shadows } from '../theme'

function roleLabel(role: FamilyMember['role']) {
  if (role === 'admin') return 'Админ'
  if (role === 'member') return 'Участник'
  return 'Наблюдатель'
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
        fontSize: Math.round(size * 0.55),
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

export function FamilyHubScreen() {
  const { family, settings } = useApp()
  const theme = getTheme(settings.theme)

  useSetPageHeader({ showBack: true, backTo: '/more' })

  return (
    <PremiumGate feature="Семейный бюджет">
      <div style={{ padding: '0 16px 24px' }}>
        <div
          style={{
            position: 'relative',
            padding: '20px 20px 18px',
            borderRadius: radius.xl,
            background: `linear-gradient(145deg, ${theme.primary} 0%, ${theme.primaryDark} 100%)`,
            color: '#fff',
            boxShadow: shadows.hero,
            marginBottom: 16,
            overflow: 'hidden',
            textAlign: 'center',
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

          <p style={{ position: 'relative', margin: 0, fontSize: 13, opacity: 0.88, fontWeight: 500 }}>
            Участников семьи
          </p>
          <p
            style={{
              position: 'relative',
              margin: '6px 0 14px',
              fontSize: 34,
              fontWeight: 700,
              letterSpacing: -0.5,
              lineHeight: 1.1,
            }}
          >
            {family.length} / 6
          </p>

          <div
            style={{
              position: 'relative',
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              flexWrap: 'wrap',
              gap: 4,
            }}
          >
            {family.map((member, i) => (
              <span
                key={member.id}
                title={member.name}
                style={{
                  width: 44,
                  height: 44,
                  borderRadius: 22,
                  display: 'inline-flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: 24,
                  lineHeight: 1,
                  background: 'rgba(255,255,255,0.16)',
                  border: '2px solid rgba(255,255,255,0.45)',
                  marginLeft: i > 0 ? -6 : 0,
                  zIndex: family.length - i,
                  position: 'relative',
                }}
              >
                {member.avatar}
              </span>
            ))}
          </div>
        </div>

        {family.map((member) => (
          <Link
            key={member.id}
            to={`/family/${member.id}`}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 14,
              padding: '14px 16px',
              marginBottom: 10,
              borderRadius: radius.md,
              background: theme.surface,
              border: `2px solid ${theme.primary}45`,
              boxShadow: shadows.card,
              textDecoration: 'none',
              color: theme.text,
            }}
          >
            <MemberAvatar avatar={member.avatar} size={44} />
            <div style={{ flex: 1, minWidth: 0 }}>
              <p style={{ margin: 0, fontWeight: 600, fontSize: 15 }}>{member.name}</p>
              <p style={{ margin: '2px 0 0', fontSize: 13, color: theme.textMuted }}>
                {roleLabel(member.role)}
              </p>
            </div>
            <span style={{ color: theme.textMuted, fontSize: 18, flexShrink: 0 }}>›</span>
          </Link>
        ))}

        <button
          type="button"
          style={{
            width: '100%',
            padding: 14,
            marginTop: 4,
            borderRadius: radius.md,
            border: `2px dashed ${theme.primary}`,
            background: 'transparent',
            color: theme.primary,
            fontWeight: 700,
            fontSize: 15,
            cursor: 'pointer',
          }}
        >
          + Пригласить по ссылке / QR
        </button>
      </div>
    </PremiumGate>
  )
}
