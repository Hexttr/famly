import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

interface ProgressBarProps {
  spent: number
  limit: number
  color?: string
  trackColor?: string
  height?: number
  showLabel?: boolean
  labelColor?: string
  label?: string
  invertLabel?: boolean
}

export function ProgressBar({
  spent,
  limit,
  color,
  trackColor,
  height = 8,
  showLabel = false,
  labelColor,
  label,
  invertLabel = false,
}: ProgressBarProps) {
  const { settings } = useApp()
  const theme = getTheme(settings.theme)
  const pct = limit > 0 ? Math.min(100, (spent / limit) * 100) : 0
  const roundedPct = Math.round(pct)
  const barColor = pct >= 100 ? theme.expense : pct >= 90 ? theme.warning : (color ?? theme.primary)
  const remainingPct = 100 - roundedPct

  const autoLabel = invertLabel
    ? `Осталось ${remainingPct}% бюджета`
    : `Использовано ${roundedPct}% бюджета`

  const displayLabel = label ?? autoLabel

  return (
    <div>
      <div
        style={{
          height,
          borderRadius: height / 2,
          background: trackColor ?? theme.surfaceAlt,
          overflow: 'hidden',
        }}
      >
        <div
          style={{
            height: '100%',
            width: `${pct}%`,
            background: barColor,
            borderRadius: height / 2,
            transition: 'width 0.4s ease',
          }}
        />
      </div>
      {showLabel && (
        <p
          style={{
            margin: '8px 0 0',
            fontSize: 12,
            fontWeight: 500,
            color: labelColor ?? theme.textMuted,
            opacity: invertLabel ? 0.92 : 1,
          }}
        >
          {displayLabel}
        </p>
      )}
    </div>
  )
}
