import { useParams } from 'react-router-dom'
import type { FamilyMember } from '../types'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { getTheme, radius, shadows } from '../theme'

function roleLabel(role: FamilyMember['role']) {
  if (role === 'admin') return 'Администратор'
  if (role === 'member') return 'Участник'
  return 'Наблюдатель'
}

function visibilityLabel(visibility: FamilyMember['visibility']) {
  if (visibility === 'full') return 'Полная'
  if (visibility === 'partial') return 'Частичная'
  return 'Приватная'
}

export function FamilyMemberScreen() {
  const { id } = useParams()
  const { family, settings } = useApp()
  const theme = getTheme(settings.theme)

  const member = family.find((m) => m.id === id)
  useSetPageHeader({ showBack: true, backTo: '/family' })

  if (!member) return null

  const rows = [
    ['Роль', roleLabel(member.role)],
    ['Видимость', visibilityLabel(member.visibility)],
  ] as const

  return (
    <div style={{ padding: '0 16px 24px' }}>
      <div
        style={{
          padding: '24px 16px 20px',
          marginBottom: 16,
          borderRadius: radius.lg,
          background: theme.surface,
          border: `2px solid ${theme.primary}45`,
          boxShadow: shadows.card,
          textAlign: 'center',
        }}
      >
        <span
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: 88,
            height: 88,
            borderRadius: 44,
            fontSize: 48,
            lineHeight: 1,
            background: `${theme.primary}10`,
            border: `2px solid ${theme.primary}45`,
            marginBottom: 12,
          }}
        >
          {member.avatar}
        </span>
        <p style={{ margin: 0, fontWeight: 700, fontSize: 20, color: theme.text }}>{member.name}</p>
        <p style={{ margin: '6px 0 0', fontSize: 14, color: theme.textMuted }}>{roleLabel(member.role)}</p>
      </div>

      <div
        style={{
          borderRadius: radius.lg,
          background: theme.surface,
          border: `2px solid ${theme.primary}45`,
          boxShadow: shadows.card,
          overflow: 'hidden',
        }}
      >
        {rows.map(([label, value], i) => (
          <div
            key={label}
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              gap: 12,
              padding: '14px 16px',
              borderBottom: i === rows.length - 1 ? 'none' : `1px solid ${theme.primary}20`,
            }}
          >
            <span style={{ color: theme.textMuted, fontSize: 14 }}>{label}</span>
            <span style={{ fontWeight: 600, fontSize: 15, color: theme.text, textAlign: 'right' }}>{value}</span>
          </div>
        ))}
      </div>
    </div>
  )
}
