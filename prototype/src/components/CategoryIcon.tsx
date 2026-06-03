import { getCategoryIcon, iconsForType, type CategoryIconDef } from '../data/categoryIcons'

interface CategoryIconProps {
  iconId: string
  size?: number
  color?: string
  background?: string
}

export function CategoryIcon({ iconId, size = 24, color, background }: CategoryIconProps) {
  const def = getCategoryIcon(iconId)
  const stroke = color ?? def.color

  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        width: size + 8,
        height: size + 8,
        borderRadius: 10,
        background: background ?? '#FFFFFF',
        border: `1.5px solid ${def.color}55`,
        flexShrink: 0,
      }}
    >
      <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden>
        {def.paths.map((d, i) => (
          <path
            key={i}
            d={d}
            stroke={stroke}
            strokeWidth={1.75}
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        ))}
      </svg>
    </span>
  )
}

interface IconPickerProps {
  type: 'expense' | 'income'
  selectedId: string
  onSelect: (icon: CategoryIconDef) => void
}

export function IconPicker({ type, selectedId, onSelect }: IconPickerProps) {
  const icons = iconsForType(type)

  return (
    <div
      style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(5, 1fr)',
        gap: 8,
        maxHeight: 220,
        overflowY: 'auto',
        padding: 2,
      }}
    >
      {icons.map((icon) => {
        const selected = icon.id === selectedId
        return (
          <button
            key={icon.id}
            type="button"
            title={icon.label}
            onClick={() => onSelect(icon)}
            style={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: 4,
              padding: '8px 4px',
              border: selected ? `2px solid ${icon.color}` : '1px solid #E2E8E5',
              borderRadius: 12,
              background: selected ? `${icon.color}12` : '#fff',
              cursor: 'pointer',
            }}
          >
            <CategoryIcon iconId={icon.id} size={20} />
            <span style={{ fontSize: 9, color: '#5C6560', lineHeight: 1.1, textAlign: 'center' }}>
              {icon.label}
            </span>
          </button>
        )
      })}
    </div>
  )
}
