import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

interface ProgressBarProps {
  spent: number
  limit: number
  color?: string
  height?: number
}

export function ProgressBar({ spent, limit, color, height = 8 }: ProgressBarProps) {
  const { settings } = useApp()
  const theme = getTheme(settings.theme)
  const pct = limit > 0 ? Math.min(100, (spent / limit) * 100) : 0
  const barColor = pct >= 100 ? theme.expense : pct >= 90 ? theme.warning : (color ?? theme.primary)

  return (
    <div
      style={{
        height,
        borderRadius: height / 2,
        background: theme.surfaceAlt,
        overflow: 'hidden',
      }}
    >
      <div
        style={{
          height: '100%',
          width: `${pct}%`,
          background: barColor,
          borderRadius: height / 2,
          transition: 'width 0.3s ease',
        }}
      />
    </div>
  )
}
