import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

export function SettingsScreen() {
  const { settings, updateSettings } = useApp()
  const theme = getTheme(settings.theme)

  useSetPageHeader({ showBack: true, backTo: '/more' })

  return (
    <div style={{ padding: 16 }}>
      <section style={{ marginBottom: 24 }}>
        <h3 style={{ margin: '0 0 12px', fontSize: 13, color: theme.textMuted, textTransform: 'uppercase' }}>
          Бюджет
        </h3>
        <div style={{ borderRadius: 12, background: theme.surface, border: `1px solid ${theme.border}`, overflow: 'hidden' }}>
          <label style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: 16 }}>
            <span style={{ color: theme.text }}>Начало периода (день)</span>
            <select
              value={settings.budgetPeriod.startDay}
              onChange={(e) =>
                updateSettings({
                  budgetPeriod: { ...settings.budgetPeriod, startDay: Number(e.target.value) },
                })
              }
              style={{ padding: '6px 10px', borderRadius: 8, border: `1px solid ${theme.border}`, background: theme.surface, color: theme.text }}
            >
              {Array.from({ length: 28 }, (_, i) => i + 1).map((d) => (
                <option key={d} value={d}>{d}</option>
              ))}
            </select>
          </label>
          <label style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: 16, borderTop: `1px solid ${theme.border}` }}>
            <span style={{ color: theme.text }}>Валюта</span>
            <span style={{ color: theme.textSecondary }}>{settings.currency}</span>
          </label>
        </div>
      </section>

      <section style={{ marginBottom: 24 }}>
        <h3 style={{ margin: '0 0 12px', fontSize: 13, color: theme.textMuted, textTransform: 'uppercase' }}>
          Оформление
        </h3>
        <div style={{ display: 'flex', gap: 8 }}>
          {(['light', 'dark'] as const).map((mode) => (
            <button
              key={mode}
              type="button"
              onClick={() => updateSettings({ theme: mode })}
              style={{
                flex: 1,
                padding: 14,
                borderRadius: 12,
                border: settings.theme === mode ? `2px solid ${theme.primary}` : `1px solid ${theme.border}`,
                background: theme.surface,
                cursor: 'pointer',
                fontWeight: 600,
                color: theme.text,
              }}
            >
              {mode === 'light' ? '☀️ Светлая' : '🌙 Тёмная'}
            </button>
          ))}
        </div>
      </section>
    </div>
  )
}
