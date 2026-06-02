import { useParams } from 'react-router-dom'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

export function FamilyMemberScreen() {
  const { id } = useParams()
  const { family, settings } = useApp()
  const theme = getTheme(settings.theme)

  const member = family.find((m) => m.id === id)
  useSetPageHeader({ showBack: true, backTo: '/family' })

  if (!member) return null

  return (
    <div style={{ padding: 16 }}>
      <div style={{ textAlign: 'center', marginBottom: 16 }}>
        <span style={{ fontSize: 64 }}>{member.avatar}</span>
        <p style={{ margin: '8px 0 0', fontWeight: 600, fontSize: 18 }}>{member.name}</p>
      </div>

      {[
        ['Роль', member.role === 'admin' ? 'Администратор' : member.role === 'member' ? 'Участник' : 'Наблюдатель'],
        ['Видимость', member.visibility === 'full' ? 'Полная' : member.visibility === 'partial' ? 'Частичная' : 'Приватная'],
      ].map(([label, value]) => (
        <div
          key={label}
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            padding: '16px 0',
            borderBottom: `1px solid ${theme.border}`,
          }}
        >
          <span style={{ color: theme.textMuted }}>{label}</span>
          <span style={{ fontWeight: 500, color: theme.text }}>{value}</span>
        </div>
      ))}
    </div>
  )
}
