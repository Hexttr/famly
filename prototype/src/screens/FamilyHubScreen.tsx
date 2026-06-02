import { Link } from 'react-router-dom'
import { Header } from '../components/Header'
import { PremiumGate } from '../components/PremiumGate'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

export function FamilyHubScreen() {
  const { family, settings } = useApp()
  const theme = getTheme(settings.theme)

  return (
    <PremiumGate feature="Семейный бюджет">
      <div>
        <Header title="Семья" backTo="/more" />
        <div style={{ padding: 16 }}>
          <div
            style={{
              padding: 20,
              borderRadius: 16,
              background: theme.primary,
              color: '#fff',
              textAlign: 'center',
              marginBottom: 16,
            }}
          >
            <p style={{ margin: 0, fontSize: 14, opacity: 0.9 }}>Участников</p>
            <p style={{ margin: '8px 0 0', fontSize: 32, fontWeight: 700 }}>{family.length} / 6</p>
          </div>

          {family.map((member) => (
            <Link
              key={member.id}
              to={`/family/${member.id}`}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 14,
                padding: 16,
                marginBottom: 8,
                borderRadius: 12,
                background: theme.surface,
                border: `1px solid ${theme.border}`,
                textDecoration: 'none',
                color: theme.text,
              }}
            >
              <span style={{ fontSize: 32 }}>{member.avatar}</span>
              <div style={{ flex: 1 }}>
                <p style={{ margin: 0, fontWeight: 600 }}>{member.name}</p>
                <p style={{ margin: '2px 0 0', fontSize: 12, color: theme.textMuted }}>
                  {member.role === 'admin' ? 'Админ' : member.role === 'member' ? 'Участник' : 'Наблюдатель'}
                  {' · '}
                  {member.visibility === 'full' ? 'Видит всё' : member.visibility === 'partial' ? 'Частично' : 'Приватно'}
                </p>
              </div>
              <span style={{ color: theme.textMuted }}>›</span>
            </Link>
          ))}

          <button
            type="button"
            style={{
              width: '100%',
              padding: 16,
              marginTop: 8,
              borderRadius: 12,
              border: `2px dashed ${theme.primary}`,
              background: 'transparent',
              color: theme.primary,
              fontWeight: 600,
              cursor: 'pointer',
            }}
          >
            + Пригласить по ссылке / QR
          </button>
        </div>
      </div>
    </PremiumGate>
  )
}
