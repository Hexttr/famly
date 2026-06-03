import { useEffect, useRef, useState, type ReactNode } from 'react'
import { createPortal } from 'react-dom'
import { Link } from 'react-router-dom'
import { CategoryIcon } from './CategoryIcon'
import { useApp } from '../context/AppContext'
import { formatMoney, getBudgetWarnings } from '../data/mockData'
import { getTheme, headerLayout, radius } from '../theme'

const PANEL_INSET = 16

function IconBell({ size = 22, filled = false }: { size?: number; filled?: boolean }) {
  if (filled) {
    return (
      <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden>
        <path
          d="M18 8a6 6 0 00-12 0c0 7-3 9-3 9h18s-3-2-3-9z"
          fill="currentColor"
        />
        <path
          d="M13.73 21a2 2 0 01-3.46 0"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
        />
      </svg>
    )
  }

  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden>
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
  buttonRef,
}: {
  label: string
  onClick: () => void
  children: ReactNode
  active?: boolean
  buttonRef?: React.RefObject<HTMLButtonElement | null>
}) {
  const { settings } = useApp()
  const theme = getTheme(settings.theme)
  const size = headerLayout.buttonSize

  return (
    <button
      ref={buttonRef}
      type="button"
      aria-label={label}
      aria-expanded={active}
      aria-haspopup="dialog"
      onClick={onClick}
      style={{
        position: 'relative',
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        width: size,
        height: size,
        borderRadius: size / 2,
        border: `2px solid ${theme.expense}50`,
        background: active ? `${theme.expense}14` : theme.surface,
        color: theme.expense,
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
  const [panelPos, setPanelPos] = useState<{ top: number; left: number; width: number } | null>(null)
  const buttonRef = useRef<HTMLButtonElement>(null)
  const panelRef = useRef<HTMLDivElement>(null)

  const warnings = getBudgetWarnings(categories, transactions)
  const count = warnings.length

  const updatePanelPos = () => {
    const rect = buttonRef.current?.getBoundingClientRect()
    if (!rect) return

    const width = window.innerWidth - PANEL_INSET * 2

    setPanelPos({
      top: rect.bottom + 8,
      left: PANEL_INSET,
      width,
    })
  }

  const toggle = () => {
    setOpen((prev) => {
      const next = !prev
      if (next) {
        requestAnimationFrame(updatePanelPos)
      } else {
        setPanelPos(null)
      }
      return next
    })
  }

  useEffect(() => {
    if (!open) return

    const onResize = () => updatePanelPos()
    window.addEventListener('resize', onResize)
    window.addEventListener('scroll', onResize, true)

    return () => {
      window.removeEventListener('resize', onResize)
      window.removeEventListener('scroll', onResize, true)
    }
  }, [open])

  useEffect(() => {
    if (!open) return

    const onPointerDown = (e: PointerEvent) => {
      const target = e.target as Node
      if (buttonRef.current?.contains(target)) return
      if (panelRef.current?.contains(target)) return
      setOpen(false)
      setPanelPos(null)
    }

    const timer = window.setTimeout(() => {
      document.addEventListener('pointerdown', onPointerDown)
    }, 0)

    return () => {
      window.clearTimeout(timer)
      document.removeEventListener('pointerdown', onPointerDown)
    }
  }, [open])

  useEffect(() => {
    if (!open) return
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        setOpen(false)
        setPanelPos(null)
      }
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [open])

  const dropdown =
    open && panelPos
      ? createPortal(
          <>
            <div
              aria-hidden
              style={{
                position: 'fixed',
                inset: 0,
                background: 'rgba(0, 0, 0, 0.45)',
                zIndex: 200,
              }}
              onClick={() => {
                setOpen(false)
                setPanelPos(null)
              }}
            />
            <div
              ref={panelRef}
              role="dialog"
              aria-label="Уведомления"
              style={{
                position: 'fixed',
                top: panelPos.top,
                left: panelPos.left,
                width: panelPos.width,
                zIndex: 210,
                maxHeight: 'min(400px, calc(100vh - 120px))',
                display: 'flex',
                flexDirection: 'column',
                overflow: 'hidden',
                borderRadius: radius.lg,
                background: theme.surface,
                boxShadow: '0 8px 32px rgba(27, 67, 50, 0.22)',
                border: `1px solid ${theme.border}`,
              }}
            >
              <div
                style={{
                  padding: '14px 16px',
                  background: `linear-gradient(135deg, ${theme.primary}18 0%, ${theme.primary}08 100%)`,
                  borderBottom: `2px solid ${theme.primary}30`,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  flexShrink: 0,
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                  <span
                    style={{
                      display: 'inline-flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      width: 32,
                      height: 32,
                      borderRadius: 16,
                      background: theme.surface,
                      color: theme.expense,
                      border: `2px solid ${theme.expense}50`,
                      boxShadow: '0 2px 10px rgba(45, 106, 79, 0.18)',
                    }}
                  >
                    <IconBell size={18} filled />
                  </span>
                  <p style={{ margin: 0, fontSize: 16, fontWeight: 700, color: theme.primaryDark }}>
                    Уведомления
                  </p>
                </div>
                {count > 0 && (
                  <span
                    style={{
                      minWidth: 24,
                      height: 24,
                      padding: '0 7px',
                      borderRadius: 12,
                      background: theme.expense,
                      color: '#fff',
                      fontSize: 13,
                      fontWeight: 700,
                      display: 'inline-flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}
                  >
                    {count}
                  </span>
                )}
              </div>

              <div style={{ overflowY: 'auto', flex: 1, minHeight: 0 }}>
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
                      onClick={() => {
                        setOpen(false)
                        setPanelPos(null)
                      }}
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
            </div>
          </>,
          document.body,
        )
      : null

  return (
    <>
      <HeaderIconButton
        label="Уведомления"
        active={open}
        buttonRef={buttonRef}
        onClick={toggle}
      >
        <IconBell filled />
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
              border: `2px solid ${theme.surface}`,
            }}
          >
            {count}
          </span>
        )}
      </HeaderIconButton>
      {dropdown}
    </>
  )
}
