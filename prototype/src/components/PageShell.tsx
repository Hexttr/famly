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
          maxWidth: 375,
          margin: '0 auto',
          minHeight: '100vh',
          background: theme.background,
          display: 'flex',
          flexDirection: 'column',
          boxShadow: '0 0 40px rgba(0,0,0,0.08)',
        }}
      >
        <AppHeader />
        <main style={{ flex: 1, overflow: 'auto' }}>{children}</main>
      </div>
    </HeaderProvider>
  )
}
