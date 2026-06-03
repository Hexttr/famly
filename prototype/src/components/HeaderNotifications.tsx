import { useEffect, useRef, useState, type ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { CategoryIcon } from './CategoryIcon'
import { useApp } from '../context/AppContext'
import { formatMoney, getBudgetWarnings } from '../data/mockData'
import { getTheme, headerLayout, radius, shadows } from '../theme'

function IconBell() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M18 8a6 6 0 10-12 0c0 7-3 9-3 9h18s-3-2-3-9z"
        stroke="currentColor"
        strokeWidth="1.75"
        strokeLinejoin="round"
      />
      <path d="M13.73 21a2 2 0 01-3.46 0" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" />
    </svg>
  )
}

function HeaderIconButton({
  label,
  onClick,
  children,
  active,
}: {
  label: string
  onClick: () => void
  children: ReactNode
  active?: boolean
}) {
  const { settings } = useApp()
  const theme = getTheme(settings.theme)
  const size = headerLayout.buttonSize

  return (
    <button
      type="button"
      aria-label={label}
      aria-expanded={active}
      onClick={onClick}
      style={{
        position: 'relative',
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        width: size,
        height: size,
        borderRadius: size / 2,
        border: active ? `2px solid ${theme.primary}` : `2px solid ${theme.primary}50`,
        background: active ? `${theme.primary}18` : theme.surface,
        color: theme.primaryDark,
        cursor: 'pointer',
        flexShrink: 0,
        boxShadow: '0 2px 10px rgba(45, 106, 79, 0.18)',
      }}
    >
      {children}
    </button>
  )
}

export function HeaderNotifications() {
  const { transactions, categories, settings } = useApp()
  const theme = getTheme(settings.theme)
  const [open, setOpen] = useState(false)
  const rootRef = useRef<HTMLDivElement>(null)

  const warnings = getBudgetWarnings(categories, transactions)
  const count = warnings.length

  useEffect(() => {
    if (!open) return
    const onPointerDown = (e: PointerEvent) => {
      if (!rootRef.current?.contains(e.target as Node)) setOpen(false)
    }
    document.addEventListener('pointerdown', onPointerDown)
    return () => document.removeEventListener('pointerdown', onPointerDown)
  }, [open])

  return (
    <div ref={rootRef} style={{ position: 'relative' }}>
      <HeaderIconButton
        label="Уведомления"
        active={open}
        onClick={() => setOpen((v) => !v)}
      >
        <IconBell />
        {count > 0 && (
          <span
            style={{
              position: 'absolute',
              top: 2,
              right: 2,
              minWidth: 18,
              height: 18,
              padding: '0 5px',
              borderRadius: 9,
              background: theme.expense,
              color: '#fff',
              fontSize: 10,
              fontWeight: 700,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              border: `2px solid ${theme.background}`,
            }}
          >
            {count}
          </span>
        )}
      </HeaderIconButton>

      {open && (
        <>
          <div
            aria-hidden
            style={{
              position: 'fixed',
              inset: 0,
              top: headerLayout.height,
              background: 'rgba(0, 0, 0, 0.18)',
              zIndex: 60,
            }}
            onClick={() => setOpen(false)}
          />
          <div
            role="dialog"
            aria-label="Уведомления"
            style={{
              position: 'fixed',
              top: headerLayout.height + 4,
              left: 12,
              right: 12,
              zIndex: 70,
              maxHeight: 'min(360px, 55vh)',
              overflowY: 'auto',
              borderRadius: radius.lg,
              background: theme.surface,
              boxShadow: shadows.md,
              border: `1px solid ${theme.border}`,
            }}
          >
            <div
              style={{
                padding: '14px 16px 10px',
                borderBottom: `1px solid ${theme.border}`,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
              }}
            >
              <p style={{ margin: 0, fontSize: 16, fontWeight: 700, color: theme.text }}>
                Уведомления
              </p>
              {count > 0 && (
                <span
                  style={{
                    fontSize: 12,
                    fontWeight: 600,
                    color: theme.expense,
                    background: `${theme.expense}12`,
                    padding: '3px 8px',
                    borderRadius: radius.full,
                  }}
                >
                  {count}
                </span>
              )}
            </div>

            {count === 0 ? (
              <div style={{ padding: '28px 16px', textAlign: 'center' }}>
                <p style={{ margin: 0, fontSize: 32, lineHeight: 1 }}>🔔</p>
                <p style={{ margin: '10px 0 4px', fontSize: 15, fontWeight: 600, color: theme.text }}>
                  Всё в порядке
                </p>
                <p style={{ margin: 0, fontSize: 13, color: theme.textMuted }}>
                  Нет превышений по бюджету
                </p>
              </div>
            ) : (
              warnings.map((w, i) => {
                const cat = categories.find((c) => c.id === w.categoryId)
                if (!cat) return null
                const pct = Math.round(w.percent * 100)
                const isOver = pct >= 100
                const isLast = i === warnings.length - 1

                return (
                  <Link
                    key={w.categoryId}
                    to={`/budget/${w.categoryId}`}
                    onClick={() => setOpen(false)}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 12,
                      padding: '14px 16px',
                      borderBottom: isLast ? 'none' : `1px solid ${theme.border}`,
                      textDecoration: 'none',
                      color: theme.text,
                      background: isOver ? `${theme.expense}06` : `${theme.warning}08`,
                    }}
                  >
                    <CategoryIcon iconId={cat.iconId} size={18} variant="circle" emphasis />
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <p style={{ margin: 0, fontSize: 14, fontWeight: 600 }}>{cat.name}</p>
                      <p style={{ margin: '3px 0 0', fontSize: 12, color: theme.textMuted }}>
                        {isOver ? 'Лимит превышен' : 'Приближается к лимиту'} · {formatMoney(w.spent)} из{' '}
                        {formatMoney(w.limit)}
                      </p>
                    </div>
                    <span
                      style={{
                        flexShrink: 0,
                        fontSize: 13,
                        fontWeight: 700,
                        color: isOver ? theme.expense : theme.warning,
                      }}
                    >
                      {pct}%
                    </span>
                  </Link>
                )
              })
            )}
          </div>
        </>
      )}
    </div>
  )
}
