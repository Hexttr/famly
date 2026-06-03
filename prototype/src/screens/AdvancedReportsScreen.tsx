import { useMemo, useState } from 'react'
import { CategoryIcon } from '../components/CategoryIcon'
import { PremiumGate } from '../components/PremiumGate'
import { useSetPageHeader } from '../context/HeaderContext'
import { useApp } from '../context/AppContext'
import {
  REPORT_PERIODS,
  formatMoney,
  getAverageDailyExpense,
  getBudgetWarnings,
  getCategoryExpenseTrends,
  getMonthlyTotals,
  getMonthsCountForPeriod,
  getPeriodExpenseComparison,
  getReportPeriodDescription,
  getTotalExpenses,
  getTotalIncome,
  filterTransactionsByPeriod,
  type ReportPeriod,
} from '../data/mockData'
import { getTheme, radius, shadows } from '../theme'

function SectionHeading({
  icon,
  title,
  theme,
}: {
  icon: string
  title: string
  theme: ReturnType<typeof getTheme>
}) {
  return (
    <h3
      style={{
        margin: '0 0 10px',
        fontSize: 16,
        fontWeight: 700,
        color: theme.text,
        display: 'flex',
        alignItems: 'center',
        gap: 8,
      }}
    >
      <span
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          width: 32,
          height: 32,
          borderRadius: 16,
          background: `${theme.primary}14`,
          border: `2px solid ${theme.primary}45`,
          flexShrink: 0,
          fontSize: 18,
          lineHeight: 1,
        }}
      >
        {icon}
      </span>
      {title}
    </h3>
  )
}

function FlowCard({
  label,
  amount,
  icon,
  accentColor,
  theme,
}: {
  label: string
  amount: string
  icon: string
  accentColor: string
  theme: ReturnType<typeof getTheme>
}) {
  return (
    <div
      style={{
        position: 'relative',
        padding: '16px 14px',
        borderRadius: radius.lg,
        background: `linear-gradient(155deg, ${accentColor}16 0%, ${theme.surface} 55%)`,
        border: `2px solid ${accentColor}45`,
        boxShadow: shadows.card,
        overflow: 'hidden',
      }}
    >
      <div
        aria-hidden
        style={{
          position: 'absolute',
          top: -18,
          right: -18,
          width: 72,
          height: 72,
          borderRadius: '50%',
          background: `${accentColor}12`,
        }}
      />
      <div style={{ position: 'relative', display: 'flex', alignItems: 'center', gap: 10, marginBottom: 12 }}>
        <span
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: 38,
            height: 38,
            borderRadius: 19,
            background: `${accentColor}20`,
            flexShrink: 0,
            fontSize: 22,
            lineHeight: 1,
          }}
        >
          {icon}
        </span>
        <span style={{ fontSize: 13, fontWeight: 700, color: theme.textSecondary, letterSpacing: 0.2 }}>
          {label}
        </span>
      </div>
      <p
        style={{
          position: 'relative',
          margin: 0,
          fontSize: 22,
          fontWeight: 700,
          color: accentColor,
          letterSpacing: -0.3,
          lineHeight: 1.15,
        }}
      >
        {amount}
      </p>
    </div>
  )
}

function formatChangePercent(value: number | null): string {
  if (value === null) return '—'
  if (value > 0) return `+${value}%`
  if (value < 0) return `${value}%`
  return '0%'
}

function TrendBadge({
  changePercent,
  theme,
}: {
  changePercent: number | null
  theme: ReturnType<typeof getTheme>
}) {
  if (changePercent === null) {
    return (
      <span
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: 4,
          padding: '4px 10px',
          borderRadius: radius.full,
          fontSize: 12,
          fontWeight: 700,
          background: 'rgba(255,255,255,0.16)',
          color: '#fff',
        }}
      >
        нет данных для сравнения
      </span>
    )
  }

  const increased = changePercent > 0
  const decreased = changePercent < 0
  const bg = increased ? `${theme.expense}33` : decreased ? 'rgba(255,255,255,0.2)' : 'rgba(255,255,255,0.16)'

  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: 4,
        padding: '4px 10px',
        borderRadius: radius.full,
        fontSize: 13,
        fontWeight: 700,
        background: bg,
        color: '#fff',
      }}
    >
      {increased ? '↑' : decreased ? '↓' : '→'} {formatChangePercent(changePercent)} к прошлому периоду
    </span>
  )
}

export function AdvancedReportsScreen() {
  const { categories, transactions, settings } = useApp()
  const theme = getTheme(settings.theme)
  const [period, setPeriod] = useState<ReportPeriod>('3months')

  useSetPageHeader({ showBack: true, backTo: '/more' })

  const periodTx = useMemo(
    () => filterTransactionsByPeriod(transactions, period),
    [transactions, period],
  )

  const comparison = useMemo(
    () => getPeriodExpenseComparison(transactions, period),
    [transactions, period],
  )

  const monthlyTotals = useMemo(
    () => getMonthlyTotals(transactions, getMonthsCountForPeriod(period)),
    [transactions, period],
  )

  const categoryTrends = useMemo(
    () => getCategoryExpenseTrends(categories, transactions, period).slice(0, 5),
    [categories, transactions, period],
  )

  const budgetWarnings = useMemo(
    () => getBudgetWarnings(categories, periodTx, 0.75),
    [categories, periodTx],
  )

  const totalIncome = getTotalIncome(periodTx)
  const totalExpenses = getTotalExpenses(periodTx)
  const netBalance = totalIncome - totalExpenses
  const avgDaily = getAverageDailyExpense(transactions, period)
  const periodDescription = getReportPeriodDescription(period)
  const maxMonthlyExpense = Math.max(...monthlyTotals.map((m) => m.expenses), 1)

  const insights: string[] = []
  if (avgDaily > 0) {
    insights.push(`Средний расход в день — ${formatMoney(avgDaily)}`)
  }
  if (netBalance >= 0 && totalIncome > 0) {
    insights.push(`Профицит за период — ${formatMoney(netBalance)}`)
  } else if (netBalance < 0) {
    insights.push(`Расходы превышают доходы на ${formatMoney(Math.abs(netBalance))}`)
  }
  const topGrowth = categoryTrends.find((t) => (t.changePercent ?? 0) > 0 && t.current > 0)
  if (topGrowth?.changePercent) {
    insights.push(`Рост расходов: «${topGrowth.name}» +${topGrowth.changePercent}%`)
  }
  const topDrop = categoryTrends.find((t) => (t.changePercent ?? 0) < 0)
  if (topDrop?.changePercent) {
    insights.push(`Снижение: «${topDrop.name}» ${topDrop.changePercent}%`)
  }
  if (budgetWarnings.length > 0) {
    const w = budgetWarnings[0]
    const cat = categories.find((c) => c.id === w.categoryId)
    if (cat) {
      insights.push(`«${cat.name}» — ${Math.round(w.percent * 100)}% от лимита бюджета`)
    }
  }

  return (
    <PremiumGate feature="Расширенная аналитика">
      <div style={{ padding: '0 16px 24px' }}>
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(4, 1fr)',
            gap: 8,
            marginBottom: 16,
          }}
        >
          {REPORT_PERIODS.map(({ id, label }) => {
            const active = period === id
            return (
              <button
                key={id}
                type="button"
                onClick={() => setPeriod(id)}
                style={{
                  padding: '10px 4px',
                  borderRadius: radius.md,
                  border: `2px solid ${active ? theme.primary : `${theme.primary}45`}`,
                  background: active ? theme.primary : theme.surface,
                  color: active ? '#fff' : theme.textSecondary,
                  fontSize: 12,
                  fontWeight: 700,
                  cursor: 'pointer',
                  boxShadow: active ? 'none' : shadows.sm,
                }}
              >
                {label}
              </button>
            )
          })}
        </div>

        <div
          style={{
            position: 'relative',
            padding: '18px 20px 16px',
            borderRadius: radius.xl,
            background: `linear-gradient(145deg, ${theme.primary} 0%, ${theme.primaryDark} 100%)`,
            color: '#fff',
            boxShadow: shadows.hero,
            marginBottom: 16,
            overflow: 'hidden',
          }}
        >
          <div
            aria-hidden
            style={{
              position: 'absolute',
              top: -36,
              right: -28,
              width: 120,
              height: 120,
              borderRadius: '50%',
              background: 'rgba(255,255,255,0.06)',
            }}
          />

          <div style={{ position: 'relative', display: 'flex', alignItems: 'stretch', gap: 14 }}>
            <span
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                width: 58,
                alignSelf: 'stretch',
                minHeight: 58,
                flexShrink: 0,
                fontSize: 38,
                lineHeight: 1,
              }}
            >
              📉
            </span>
            <div style={{ flex: 1, minWidth: 0 }}>
              <p style={{ margin: 0, fontSize: 13, opacity: 0.88, fontWeight: 500 }}>Расходы за период</p>
              <p style={{ margin: '2px 0 6px', fontSize: 12, opacity: 0.75 }}>{periodDescription}</p>
              <p style={{ margin: '0 0 8px', fontSize: 28, fontWeight: 700, letterSpacing: -0.5, lineHeight: 1.1 }}>
                {formatMoney(comparison.currentExpenses)}
              </p>
              <TrendBadge changePercent={comparison.changePercent} theme={theme} />
            </div>
          </div>
        </div>

        <div
          style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: 10,
            marginBottom: 16,
          }}
        >
          <FlowCard
            label="Доходы"
            amount={formatMoney(totalIncome)}
            icon="💰"
            accentColor={theme.income}
            theme={theme}
          />
          <FlowCard
            label="Расходы"
            amount={formatMoney(totalExpenses)}
            icon="💸"
            accentColor={theme.expense}
            theme={theme}
          />
        </div>

        <SectionHeading icon="📊" title="Динамика по месяцам" theme={theme} />
        <div
          style={{
            padding: '18px 14px 14px',
            borderRadius: radius.lg,
            background: theme.surface,
            border: `2px solid ${theme.primary}45`,
            boxShadow: shadows.card,
            marginBottom: 16,
          }}
        >
          <div
            style={{
              display: 'flex',
              alignItems: 'flex-end',
              justifyContent: 'space-between',
              gap: 6,
              minHeight: 140,
            }}
          >
            {monthlyTotals.map((m) => {
              const heightPct = m.expenses > 0 ? Math.max(8, (m.expenses / maxMonthlyExpense) * 100) : 4
              const isEmpty = m.expenses === 0
              return (
                <div
                  key={m.key}
                  style={{
                    flex: 1,
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    gap: 6,
                    minWidth: 0,
                  }}
                >
                  <span
                    style={{
                      fontSize: 10,
                      fontWeight: 600,
                      color: theme.textMuted,
                      textAlign: 'center',
                      lineHeight: 1.2,
                      minHeight: 24,
                    }}
                  >
                    {m.expenses > 0 ? formatMoney(m.expenses).replace(' ₽', '') : '—'}
                  </span>
                  <div
                    style={{
                      width: '100%',
                      maxWidth: 36,
                      height: 100,
                      display: 'flex',
                      alignItems: 'flex-end',
                      justifyContent: 'center',
                    }}
                  >
                    <div
                      style={{
                        width: '72%',
                        height: `${heightPct}%`,
                        borderRadius: '6px 6px 2px 2px',
                        background: isEmpty
                          ? theme.surfaceAlt
                          : `linear-gradient(180deg, ${theme.primaryLight} 0%, ${theme.primary} 100%)`,
                        border: isEmpty ? `2px dashed ${theme.primary}30` : 'none',
                        transition: 'height 0.35s ease',
                      }}
                    />
                  </div>
                  <span style={{ fontSize: 11, fontWeight: 700, color: theme.textSecondary }}>{m.label}</span>
                </div>
              )
            })}
          </div>
        </div>

        {insights.length > 0 && (
          <>
            <SectionHeading icon="💡" title="Выводы" theme={theme} />
            <div
              style={{
                padding: '4px 0',
                borderRadius: radius.lg,
                background: theme.surface,
                border: `2px solid ${theme.primary}45`,
                boxShadow: shadows.card,
                marginBottom: 16,
                overflow: 'hidden',
              }}
            >
              {insights.map((text, i) => (
                <div
                  key={text}
                  style={{
                    padding: '12px 16px',
                    borderBottom: i < insights.length - 1 ? `1px solid ${theme.primary}20` : 'none',
                    display: 'flex',
                    alignItems: 'flex-start',
                    gap: 10,
                    fontSize: 14,
                    color: theme.text,
                    lineHeight: 1.45,
                  }}
                >
                  <span
                    style={{
                      width: 8,
                      height: 8,
                      borderRadius: 4,
                      background: theme.primary,
                      flexShrink: 0,
                      marginTop: 6,
                    }}
                  />
                  {text}
                </div>
              ))}
            </div>
          </>
        )}

        <h3 style={{ margin: '0 0 10px', fontSize: 16, fontWeight: 700, color: theme.text }}>
          Изменение по категориям
        </h3>
        {categoryTrends.length === 0 ? (
          <div
            style={{
              padding: '28px 16px',
              borderRadius: radius.lg,
              background: theme.surface,
              border: `2px solid ${theme.primary}45`,
              textAlign: 'center',
              color: theme.textMuted,
              fontSize: 14,
            }}
          >
            Недостаточно данных для сравнения
          </div>
        ) : (
          <div
            style={{
              borderRadius: radius.lg,
              background: theme.surface,
              border: `2px solid ${theme.primary}45`,
              boxShadow: shadows.card,
              overflow: 'hidden',
            }}
          >
            {categoryTrends.map((trend, i) => {
              const isLast = i === categoryTrends.length - 1
              const change = trend.changePercent
              const changeColor =
                change === null
                  ? theme.textMuted
                  : change > 0
                    ? theme.expense
                    : change < 0
                      ? theme.income
                      : theme.textSecondary

              return (
                <div
                  key={trend.categoryId}
                  style={{
                    padding: '14px 16px',
                    borderBottom: isLast ? 'none' : `1px solid ${theme.primary}20`,
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                    <CategoryIcon iconId={trend.iconId} size={20} variant="circle" emphasis />
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <p style={{ margin: 0, fontWeight: 600, fontSize: 15, color: theme.text }}>{trend.name}</p>
                      <p style={{ margin: '2px 0 0', fontSize: 12, color: theme.textMuted }}>
                        {formatMoney(trend.current)} · было {formatMoney(trend.previous)}
                      </p>
                    </div>
                    <span style={{ fontSize: 14, fontWeight: 700, color: changeColor, flexShrink: 0 }}>
                      {change === null ? '—' : formatChangePercent(change)}
                    </span>
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>
    </PremiumGate>
  )
}
