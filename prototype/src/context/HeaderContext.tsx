import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'

export type HeaderLeftSlot = 'notifications'

export type HeaderRightSlot = 'categories' | 'settings' | 'search' | 'add' | 'export' | 'quickAdd'

export interface PageHeaderConfig {
  leftSlot?: HeaderLeftSlot
  rightSlot?: HeaderRightSlot
  showBack?: boolean
  backTo?: string
}

interface HeaderContextValue {
  config: PageHeaderConfig
  setPageHeader: (config: PageHeaderConfig) => void
}

const HeaderContext = createContext<HeaderContextValue | null>(null)

export function HeaderProvider({ children }: { children: ReactNode }) {
  const [config, setPageHeader] = useState<PageHeaderConfig>({})

  const value = useMemo(
    () => ({ config, setPageHeader }),
    [config],
  )

  return (
    <HeaderContext.Provider value={value}>
      {children}
    </HeaderContext.Provider>
  )
}

export function useHeader() {
  const ctx = useContext(HeaderContext)
  if (!ctx) throw new Error('useHeader must be used within HeaderProvider')
  return ctx
}

export function useSetPageHeader(config: PageHeaderConfig) {
  const { setPageHeader } = useHeader()
  const { showBack, backTo, leftSlot, rightSlot } = config

  useEffect(() => {
    setPageHeader({ showBack, backTo, leftSlot, rightSlot })
    return () => setPageHeader({})
  }, [showBack, backTo, leftSlot, rightSlot, setPageHeader])
}
