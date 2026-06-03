/** Famly Design System — контракт для Jetpack Compose */
export const colors = {
  primary: '#2D6A4F',
  primaryLight: '#40916C',
  primaryDark: '#1B4332',
  accent: '#52B788',
  background: '#F8FAF9',
  surface: '#FFFFFF',
  surfaceAlt: '#F1F5F3',
  text: '#1A1D1A',
  textSecondary: '#5C6560',
  textMuted: '#8A9390',
  border: '#E2E8E5',
  expense: '#E63946',
  income: '#2D6A4F',
  warning: '#F4A261',
  premium: '#D4A017',
  premiumBg: '#FFF8E7',
} as const

export const darkColors = {
  primary: '#52B788',
  primaryLight: '#74C69D',
  primaryDark: '#40916C',
  accent: '#95D5B2',
  background: '#121614',
  surface: '#1E2421',
  surfaceAlt: '#2A322E',
  text: '#F0F4F2',
  textSecondary: '#A8B5AE',
  textMuted: '#6B7872',
  border: '#3A4540',
  expense: '#FF6B6B',
  income: '#52B788',
  warning: '#F4A261',
  premium: '#FFD166',
  premiumBg: '#2A2510',
} as const

export const spacing = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 48,
} as const

/** Шапка приложения — единые отступы и размер логотипа */
export const headerLayout = {
  inset: spacing.md,
  logoSize: 108,
  sideWidth: 48,
} as const

export const radius = {
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  full: 9999,
} as const

export const shadows = {
  sm: '0 1px 3px rgba(0, 0, 0, 0.06)',
  md: '0 4px 14px rgba(0, 0, 0, 0.07)',
  card: '0 2px 8px rgba(27, 67, 50, 0.06)',
  hero: '0 8px 28px rgba(27, 67, 50, 0.28)',
} as const

export const typography = {
  hero: { fontSize: 32, fontWeight: 700, lineHeight: 1.2 },
  h1: { fontSize: 24, fontWeight: 700, lineHeight: 1.3 },
  h2: { fontSize: 20, fontWeight: 600, lineHeight: 1.35 },
  h3: { fontSize: 17, fontWeight: 600, lineHeight: 1.4 },
  body: { fontSize: 15, fontWeight: 400, lineHeight: 1.5 },
  bodyBold: { fontSize: 15, fontWeight: 600, lineHeight: 1.5 },
  caption: { fontSize: 13, fontWeight: 400, lineHeight: 1.4 },
  small: { fontSize: 11, fontWeight: 500, lineHeight: 1.3 },
} as const

export type ThemeMode = 'light' | 'dark'

export function getTheme(mode: ThemeMode) {
  return mode === 'dark' ? darkColors : colors
}
