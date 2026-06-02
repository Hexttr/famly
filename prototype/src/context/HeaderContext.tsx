import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'

export interface PageHeaderConfig {
  right?: ReactNode
  showBack?: boolean
  backTo?: string
  onBack?: () => void
}

interface HeaderContextValue {
  config: PageHeaderConfig
  setPageHeader: (config: PageHeaderConfig) => void
}

const HeaderContext = createContext<HeaderContextValue | null>(null)

export function HeaderProvider({ children }: { children: ReactNode }) {
  const [config, setPageHeader] = useState<PageHeaderConfig>({})

  return (
    <HeaderContext.Provider value={{ config, setPageHeader }}>
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
  const navigate = useNavigate()

  useEffect(() => {
    setPageHeader({
      ...config,
      onBack: config.showBack
        ? () => {
            if (config.onBack) config.onBack()
            else if (config.backTo) navigate(config.backTo)
            else navigate(-1)
          }
        : undefined,
    })
    return () => setPageHeader({})
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [config.showBack, config.backTo, config.right, setPageHeader, navigate])
}
