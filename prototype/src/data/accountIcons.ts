/** Иконки счетов — emoji, единый набор для циклического выбора */
export const ACCOUNT_ICONS = ['💵', '💳', '🏦', '💰', '🧾', '🏠'] as const

export const DEFAULT_ACCOUNT_ICON = '💳'

export function nextAccountIcon(current: string): string {
  const idx = ACCOUNT_ICONS.indexOf(current as (typeof ACCOUNT_ICONS)[number])
  if (idx === -1) return DEFAULT_ACCOUNT_ICON
  return ACCOUNT_ICONS[(idx + 1) % ACCOUNT_ICONS.length]
}
