import { headerLayout } from '../theme'

type AppLogoVariant = 'header' | 'hero' | 'icon'

interface AppLogoProps {
  variant?: AppLogoVariant
  size?: number
}

const defaultSizes: Record<AppLogoVariant, number> = {
  header: headerLayout.logoSize,
  hero: 128,
  icon: 72,
}

export function AppLogo({ variant = 'header', size }: AppLogoProps) {
  const px = size ?? defaultSizes[variant]

  return (
    <img
      src="/logo.png"
      alt="Мой (Наш) Бюджет"
      style={{
        display: 'block',
        width: px,
        height: px,
        flexShrink: 0,
        objectFit: 'contain',
      }}
    />
  )
}
