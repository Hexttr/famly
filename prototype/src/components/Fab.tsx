import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

export function Fab() {
  const { setQuickAddOpen, settings } = useApp()
  const theme = getTheme(settings.theme)

  return (
    <button
      type="button"
      onClick={() => setQuickAddOpen(true)}
      aria-label="Добавить операцию"
      style={{
        position: 'fixed',
        bottom: 72,
        right: 20,
        width: 56,
        height: 56,
        borderRadius: 28,
        border: 'none',
        background: theme.primary,
        color: '#fff',
        fontSize: 28,
        cursor: 'pointer',
        boxShadow: '0 4px 12px rgba(45,106,79,0.35)',
        zIndex: 100,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      +
    </button>
  )
}
