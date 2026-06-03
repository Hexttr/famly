import { HeaderProvider } from '../context/HeaderContext'
import { AppHeader } from './AppHeader'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'
import type { ReactNode } from 'react'

export function PageShell({ children }: { children: ReactNode }) {
  const { settings } = useApp()
  const theme = getTheme(settings.theme)

  return (
    <HeaderProvider>
      <div
        style={{
          height: '100dvh',
          width: '100%',
          background: theme.background,
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
        }}
      >
        <AppHeader />
        <main style={{ flex: 1, minHeight: 0, overflowY: 'auto' }}>{children}</main>
      </div>
    </HeaderProvider>
  )
}
