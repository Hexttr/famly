import { useParams } from 'react-router-dom'
import { Header } from '../components/Header'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

export function FamilyMemberScreen() {
  const { id } = useParams()
  const { family, settings } = useApp()
  const theme = getTheme(settings.theme)

  const member = family.find((m) => m.id === id)
  if (!member) return null

  return (
    <div>
      <Header title={member.name} backTo="/family" />
      <div style={{ padding: 24, textAlign: 'center' }}>
        <span style={{ fontSize: 64 }}>{member.avatar}</span>
      </div>

      <div style={{ padding: '0 16px' }}>
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

        <div style={{ marginTop: 24 }}>
          <label style={{ display: 'block', marginBottom: 8, fontSize: 13, color: theme.textSecondary }}>Роль</label>
          <select
            defaultValue={member.role}
            style={{
              width: '100%',
              padding: 12,
              borderRadius: 10,
              border: `1px solid ${theme.border}`,
              background: theme.surface,
              color: theme.text,
            }}
          >
            <option value="admin">Администратор</option>
            <option value="member">Участник</option>
            <option value="viewer">Наблюдатель (дети)</option>
          </select>
        </div>

        <div style={{ marginTop: 16 }}>
          <label style={{ display: 'block', marginBottom: 8, fontSize: 13, color: theme.textSecondary }}>Видимость трат</label>
          <select
            defaultValue={member.visibility}
            style={{
              width: '100%',
              padding: 12,
              borderRadius: 10,
              border: `1px solid ${theme.border}`,
              background: theme.surface,
              color: theme.text,
            }}
          >
            <option value="full">Видит все траты</option>
            <option value="partial">Только общие</option>
            <option value="private">Только свои</option>
          </select>
        </div>
      </div>
    </div>
  )
}
