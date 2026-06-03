import { useEffect, useRef } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { AppHeader } from './AppHeader'
import { BottomNav } from './BottomNav'
import { Fab } from './Fab'
import { QuickAddSheet } from './QuickAddSheet'
import { HeaderProvider } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import { getTheme } from '../theme'

const hideNavRoutes = ['/onboarding', '/premium']
const BOTTOM_NAV_HEIGHT = 56

export function Layout() {
  const { settings } = useApp()
  const theme = getTheme(settings.theme)
  const location = useLocation()
  const mainRef = useRef<HTMLElement>(null)
  const showNav = !hideNavRoutes.some((r) => location.pathname.startsWith(r))

  useEffect(() => {
    mainRef.current?.scrollTo(0, 0)
  }, [location.pathname])

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
          position: 'relative',
        }}
      >
        {showNav && <AppHeader />}
        <main
          ref={mainRef}
          style={{
            flex: 1,
            minHeight: 0,
            overflowY: 'auto',
            overflowX: 'hidden',
            paddingBottom: showNav ? BOTTOM_NAV_HEIGHT : 0,
            WebkitOverflowScrolling: 'touch',
          }}
        >
          <Outlet key={location.pathname} />
        </main>
        {showNav && <Fab />}
        {showNav && <BottomNav />}
        <QuickAddSheet />
      </div>
    </HeaderProvider>
  )
}
