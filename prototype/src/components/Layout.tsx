import { Outlet, useLocation } from 'react-router-dom'
import { BottomNav } from './BottomNav'
import { Fab } from './Fab'
import { QuickAddSheet } from './QuickAddSheet'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

const hideNavRoutes = ['/onboarding', '/premium']

export function Layout() {
  const { settings } = useApp()
  const theme = getTheme(settings.theme)
  const location = useLocation()
  const showNav = !hideNavRoutes.some((r) => location.pathname.startsWith(r))

  return (
    <div
      style={{
        maxWidth: 375,
        margin: '0 auto',
        minHeight: '100vh',
        background: theme.background,
        display: 'flex',
        flexDirection: 'column',
        position: 'relative',
        boxShadow: '0 0 40px rgba(0,0,0,0.08)',
      }}
    >
      <main style={{ flex: 1, overflow: 'auto', paddingBottom: showNav ? 64 : 0 }}>
        <Outlet />
      </main>
      {showNav && <Fab />}
      {showNav && <BottomNav />}
      <QuickAddSheet />
    </div>
  )
}
