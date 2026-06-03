import type { ReactNode } from 'react'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { getTheme, radius, shadows } from '../theme'

function IconSectionBudget() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path d="M4 19V5M4 19h16" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <path d="M8 17V11M12 17V7M16 17v-4" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
    </svg>
  )
}

function IconSectionTheme() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden>
      <circle cx="12" cy="12" r="4" stroke="currentColor" strokeWidth="2" />
      <path
        d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.41-1.41M17.66 6.34l1.41-1.41"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
      />
    </svg>
  )
}

function IconCalendar() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
      <rect x="3" y="5" width="18" height="16" rx="2" stroke="currentColor" strokeWidth="2" />
      <path d="M3 10h18M8 3v4M16 3v4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  )
}

function IconCurrency() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
      <circle cx="12" cy="12" r="8" stroke="currentColor" strokeWidth="2" />
      <path d="M9 9.5c0-1.1 1.3-2 3-2s3 .9 3 2-1.3 2-3 2-3 .9-3 2 1.3 2 3 2 3 .9 3 2" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <path d="M12 6.5v11" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  )
}

function IconSun() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden>
      <circle cx="12" cy="12" r="4" stroke="currentColor" strokeWidth="2" />
      <path
        d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.41-1.41M17.66 6.34l1.41-1.41"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
      />
    </svg>
  )
}

function IconMoon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M20 14.5A8.5 8.5 0 1111.5 6 7 7 0 0020 14.5z"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinejoin="round"
      />
    </svg>
  )
}

function SectionTitle({
  icon,
  label,
  theme,
}: {
  icon: ReactNode
  label: string
  theme: ReturnType<typeof getTheme>
}) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 10 }}>
      <span
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          width: 28,
          height: 28,
          borderRadius: 14,
          background: `${theme.primary}14`,
          color: theme.primary,
          border: `2px solid ${theme.primary}45`,
          flexShrink: 0,
        }}
      >
        {icon}
      </span>
      <h3
        style={{
          margin: 0,
          fontSize: 13,
          fontWeight: 700,
          color: theme.textMuted,
          textTransform: 'uppercase',
          letterSpacing: 0.6,
        }}
      >
        {label}
      </h3>
    </div>
  )
}

function SettingRow({
  icon,
  label,
  hint,
  children,
  theme,
  isLast,
}: {
  icon: ReactNode
  label: string
  hint?: string
  children: ReactNode
  theme: ReturnType<typeof getTheme>
  isLast?: boolean
}) {
  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 12,
        padding: '14px 16px',
        borderBottom: isLast ? 'none' : `1px solid ${theme.primary}20`,
      }}
    >
      <span
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          width: 36,
          height: 36,
          borderRadius: radius.sm,
          background: `${theme.primary}10`,
          color: theme.primary,
          border: `2px solid ${theme.primary}45`,
          flexShrink: 0,
        }}
      >
        {icon}
      </span>
      <div style={{ flex: 1, minWidth: 0 }}>
        <p style={{ margin: 0, fontWeight: 600, fontSize: 15, color: theme.text }}>{label}</p>
        {hint && (
          <p style={{ margin: '2px 0 0', fontSize: 12, color: theme.textMuted, lineHeight: 1.35 }}>
            {hint}
          </p>
        )}
      </div>
      {children}
    </div>
  )
}

export function SettingsScreen() {
  const { settings, updateSettings } = useApp()
  const theme = getTheme(settings.theme)

  useSetPageHeader({ showBack: true, backTo: '/more' })

  return (
    <div style={{ padding: '0 16px 24px' }}>
      <section style={{ marginBottom: 22 }}>
        <SectionTitle icon={<IconSectionBudget />} label="Бюджет" theme={theme} />
        <div
          style={{
            borderRadius: radius.lg,
            background: theme.surface,
            border: `2px solid ${theme.primary}45`,
            boxShadow: shadows.card,
            overflow: 'hidden',
          }}
        >
          <SettingRow
            icon={<IconCalendar />}
            label="Начало периода"
            hint="День месяца, с которого начинается бюджет"
            theme={theme}
          >
            <select
              value={settings.budgetPeriod.startDay}
              onChange={(e) =>
                updateSettings({
                  budgetPeriod: { ...settings.budgetPeriod, startDay: Number(e.target.value) },
                })
              }
              style={{
                padding: '8px 12px',
                borderRadius: radius.sm,
                border: `2px solid ${theme.primary}45`,
                background: theme.surface,
                color: theme.text,
                fontSize: 15,
                fontWeight: 700,
                cursor: 'pointer',
                minWidth: 64,
                textAlign: 'center',
              }}
            >
              {Array.from({ length: 28 }, (_, i) => i + 1).map((d) => (
                <option key={d} value={d}>
                  {d}
                </option>
              ))}
            </select>
          </SettingRow>

          <SettingRow
            icon={<IconCurrency />}
            label="Валюта"
            hint="Отображение сумм в приложении"
            theme={theme}
            isLast
          >
            <span
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                minWidth: 48,
                padding: '8px 14px',
                borderRadius: radius.sm,
                border: `2px solid ${theme.primary}45`,
                background: `${theme.primary}10`,
                color: theme.primary,
                fontSize: 16,
                fontWeight: 700,
              }}
            >
              {settings.currency}
            </span>
          </SettingRow>
        </div>
      </section>

      <section>
        <SectionTitle icon={<IconSectionTheme />} label="Оформление" theme={theme} />
        <div style={{ display: 'flex', gap: 8 }}>
          {(
            [
              { mode: 'light' as const, label: 'Светлая', Icon: IconSun },
              { mode: 'dark' as const, label: 'Тёмная', Icon: IconMoon },
            ] as const
          ).map(({ mode, label, Icon }) => {
            const active = settings.theme === mode
            return (
              <button
                key={mode}
                type="button"
                onClick={() => updateSettings({ theme: mode })}
                style={{
                  flex: 1,
                  display: 'inline-flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: 8,
                  padding: '14px 12px',
                  borderRadius: radius.md,
                  border: `2px solid ${active ? theme.primary : `${theme.primary}45`}`,
                  background: active ? theme.primary : theme.surface,
                  cursor: 'pointer',
                  fontWeight: 700,
                  fontSize: 15,
                  color: active ? '#fff' : theme.textSecondary,
                  boxShadow: active ? '0 4px 14px rgba(45, 106, 79, 0.35)' : shadows.sm,
                }}
              >
                <Icon />
                {label}
              </button>
            )
          })}
        </div>
      </section>
    </div>
  )
}
