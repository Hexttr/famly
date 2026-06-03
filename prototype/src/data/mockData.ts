import type { Account, Category, FamilyMember, IOUBalance, Transaction } from '../types'

export const mockAccounts: Account[] = [
  { id: 'a1', name: 'Наличные', icon: '💵', balance: 12500, color: '#52B788' },
  { id: 'a2', name: 'Сбербанк', icon: '💳', balance: 87300, color: '#2D6A4F' },
  { id: 'a3', name: 'Накопления', icon: '🏦', balance: 150000, color: '#40916C' },
]

export const mockCategories: Category[] = [
  { id: 'c1', name: 'Продукты', iconId: 'groceries', type: 'expense', color: '#E63946', budgetLimit: 25000 },
  { id: 'c2', name: 'Транспорт', iconId: 'transport', type: 'expense', color: '#457B9D', budgetLimit: 8000 },
  { id: 'c3', name: 'Кафе', iconId: 'cafe', type: 'expense', color: '#F4A261', budgetLimit: 6000 },
  { id: 'c4', name: 'ЖКХ', iconId: 'home', type: 'expense', color: '#6D597A', budgetLimit: 12000 },
  { id: 'c5', name: 'Развлечения', iconId: 'entertainment', type: 'expense', color: '#E76F51', budgetLimit: 5000 },
  { id: 'c6', name: 'Зарплата', iconId: 'salary', type: 'income', color: '#2D6A4F' },
  { id: 'c7', name: 'Фриланс', iconId: 'freelance', type: 'income', color: '#40916C' },
]

export const mockTransactions: Transaction[] = [
  { id: 't1', amount: 1847, type: 'expense', categoryId: 'c1', accountId: 'a2', date: '2026-06-02', note: 'Пятёрочка' },
  { id: 't2', amount: 89, type: 'expense', categoryId: 'c2', accountId: 'a2', date: '2026-06-02', note: 'Метро' },
  { id: 't3', amount: 450, type: 'expense', categoryId: 'c3', accountId: 'a1', date: '2026-06-01', note: 'Кофейня' },
  { id: 't4', amount: 85000, type: 'income', categoryId: 'c6', accountId: 'a2', date: '2026-05-28', note: 'Зарплата май' },
  { id: 't5', amount: 3200, type: 'expense', categoryId: 'c4', accountId: 'a2', date: '2026-05-30', note: 'Электричество' },
  { id: 't6', amount: 1200, type: 'expense', categoryId: 'c5', accountId: 'a2', date: '2026-05-29', note: 'Кино' },
  { id: 't7', amount: 2100, type: 'expense', categoryId: 'c1', accountId: 'a2', date: '2026-05-28', note: 'Магнит' },
  { id: 't8', amount: 15000, type: 'income', categoryId: 'c7', accountId: 'a2', date: '2026-05-25', note: 'Проект' },
  { id: 't9', amount: 650, type: 'expense', categoryId: 'c1', accountId: 'a2', date: '2026-05-27', note: 'ВкусВилл' },
  { id: 't10', amount: 120, type: 'expense', categoryId: 'c2', accountId: 'a2', date: '2026-05-27', note: 'Автобус' },
  { id: 't11', amount: 890, type: 'expense', categoryId: 'c3', accountId: 'a1', date: '2026-05-26', note: 'Обед' },
  { id: 't12', amount: 4500, type: 'expense', categoryId: 'c4', accountId: 'a2', date: '2026-05-26', note: 'Вода и газ' },
  { id: 't13', amount: 780, type: 'expense', categoryId: 'c5', accountId: 'a2', date: '2026-05-25', note: 'Стрижка' },
  { id: 't14', amount: 2340, type: 'expense', categoryId: 'c1', accountId: 'a2', date: '2026-05-24', note: 'Лента' },
  { id: 't15', amount: 350, type: 'expense', categoryId: 'c2', accountId: 'a2', date: '2026-05-24', note: 'Яндекс Go' },
  { id: 't16', amount: 520, type: 'expense', categoryId: 'c3', accountId: 'a1', date: '2026-05-23', note: 'Starbucks' },
  { id: 't17', amount: 1800, type: 'expense', categoryId: 'c5', accountId: 'a2', date: '2026-05-22', note: 'Концерт' },
  { id: 't18', amount: 980, type: 'expense', categoryId: 'c1', accountId: 'a2', date: '2026-05-21', note: 'Перекрёсток' },
  { id: 't19', amount: 65, type: 'expense', categoryId: 'c2', accountId: 'a2', date: '2026-05-21', note: 'Метро' },
  { id: 't20', amount: 1100, type: 'expense', categoryId: 'c4', accountId: 'a2', date: '2026-05-20', note: 'Интернет' },
  { id: 't21', amount: 340, type: 'expense', categoryId: 'c3', accountId: 'a1', date: '2026-05-19', note: 'Кофе с собой' },
  { id: 't22', amount: 1560, type: 'expense', categoryId: 'c1', accountId: 'a2', date: '2026-05-18', note: 'Ашан' },
  { id: 't23', amount: 2400, type: 'expense', categoryId: 'c5', accountId: 'a2', date: '2026-05-17', note: 'Ресторан' },
  { id: 't24', amount: 430, type: 'expense', categoryId: 'c2', accountId: 'a2', date: '2026-05-16', note: 'Каршеринг' },
  { id: 't25', amount: 720, type: 'expense', categoryId: 'c3', accountId: 'a1', date: '2026-05-15', note: 'Пекарня' },
  { id: 't26', amount: 8900, type: 'expense', categoryId: 'c4', accountId: 'a2', date: '2026-05-14', note: 'Аренда' },
  { id: 't27', amount: 1120, type: 'expense', categoryId: 'c1', accountId: 'a2', date: '2026-05-13', note: 'Дикси' },
  { id: 't28', amount: 280, type: 'expense', categoryId: 'c2', accountId: 'a2', date: '2026-05-12', note: 'Трамвай' },
  { id: 't29', amount: 670, type: 'expense', categoryId: 'c3', accountId: 'a1', date: '2026-05-11', note: 'Суши' },
  { id: 't30', amount: 950, type: 'expense', categoryId: 'c5', accountId: 'a2', date: '2026-05-10', note: 'Боулинг' },
  { id: 't31', amount: 1890, type: 'expense', categoryId: 'c1', accountId: 'a2', date: '2026-05-09', note: 'Магнит' },
  { id: 't32', amount: 150, type: 'expense', categoryId: 'c2', accountId: 'a2', date: '2026-05-08', note: 'Маршрутка' },
  { id: 't33', amount: 410, type: 'expense', categoryId: 'c3', accountId: 'a1', date: '2026-05-07', note: 'Чайная' },
  { id: 't34', amount: 5600, type: 'expense', categoryId: 'c4', accountId: 'a2', date: '2026-05-06', note: 'Капремонт' },
  { id: 't35', amount: 1350, type: 'expense', categoryId: 'c5', accountId: 'a2', date: '2026-05-05', note: 'Театр' },
  { id: 't36', amount: 760, type: 'expense', categoryId: 'c1', accountId: 'a2', date: '2026-05-04', note: 'Пятёрочка' },
  { id: 't37', amount: 990, type: 'expense', categoryId: 'c3', accountId: 'a1', date: '2026-05-03', note: 'Пицца' },
  { id: 't38', amount: 220, type: 'expense', categoryId: 'c2', accountId: 'a2', date: '2026-05-02', note: 'Метро' },
]

export const mockFamily: FamilyMember[] = [
  { id: 'f1', name: 'Алексей', role: 'admin', visibility: 'full', avatar: '👨' },
  { id: 'f2', name: 'Мария', role: 'member', visibility: 'partial', avatar: '👩' },
  { id: 'f3', name: 'Саша', role: 'viewer', visibility: 'private', avatar: '👦' },
  { id: 'f4', name: 'Дедушка Виктор', role: 'viewer', visibility: 'partial', avatar: '👴' },
  { id: 'f5', name: 'Бабушка Нина', role: 'viewer', visibility: 'partial', avatar: '👵' },
]

export const mockIOU: IOUBalance[] = [
  { fromId: 'f2', toId: 'f1', amount: 1250 },
  { fromId: 'f1', toId: 'f2', amount: 800 },
]

/** Сводит встречные долги в один net-баланс по паре участников */
export function netIOUBalances(balances: IOUBalance[]): IOUBalance[] {
  const debt: Record<string, Record<string, number>> = {}

  for (const { fromId, toId, amount } of balances) {
    debt[fromId] ??= {}
    debt[fromId][toId] = (debt[fromId][toId] ?? 0) + amount
  }

  const result: IOUBalance[] = []
  const processed = new Set<string>()

  for (const fromId of Object.keys(debt)) {
    for (const toId of Object.keys(debt[fromId] ?? {})) {
      const pairKey = [fromId, toId].sort().join('|')
      if (processed.has(pairKey)) continue
      processed.add(pairKey)

      const forward = debt[fromId]?.[toId] ?? 0
      const backward = debt[toId]?.[fromId] ?? 0
      const net = forward - backward

      if (net > 0) result.push({ fromId, toId, amount: net })
      else if (net < 0) result.push({ fromId: toId, toId: fromId, amount: -net })
    }
  }

  return result.sort((a, b) => b.amount - a.amount)
}

export function formatMoney(amount: number, currency = '₽'): string {
  return `${amount.toLocaleString('ru-RU')} ${currency}`
}

export function getCategorySpent(categoryId: string, transactions: Transaction[]): number {
  return transactions
    .filter((t) => t.categoryId === categoryId && t.type === 'expense')
    .reduce((sum, t) => sum + t.amount, 0)
}

export function getTotalExpenses(transactions: Transaction[]): number {
  return transactions.filter((t) => t.type === 'expense').reduce((s, t) => s + t.amount, 0)
}

export function getTotalIncome(transactions: Transaction[]): number {
  return transactions.filter((t) => t.type === 'income').reduce((s, t) => s + t.amount, 0)
}

export function getBudgetTotal(categories: Category[]): number {
  return categories.filter((c) => c.type === 'expense' && c.budgetLimit).reduce((s, c) => s + (c.budgetLimit ?? 0), 0)
}

export function getDailySafeSpend(remaining: number, daysLeft: number): number {
  if (daysLeft <= 0 || remaining <= 0) return 0
  return Math.floor(remaining / daysLeft)
}

export function getBudgetUsedPercent(spent: number, limit: number): number {
  if (limit <= 0) return 0
  return Math.min(100, Math.round((spent / limit) * 100))
}

export function getTopExpenseCategoryIds(transactions: Transaction[], limit = 4): string[] {
  const counts = new Map<string, number>()
  for (const tx of transactions) {
    if (tx.type !== 'expense') continue
    counts.set(tx.categoryId, (counts.get(tx.categoryId) ?? 0) + 1)
  }
  return [...counts.entries()]
    .sort((a, b) => b[1] - a[1])
    .slice(0, limit)
    .map(([id]) => id)
}

export interface BudgetWarning {
  categoryId: string
  spent: number
  limit: number
  percent: number
}

export function getBudgetWarnings(
  categories: Category[],
  transactions: Transaction[],
  threshold = 0.8,
): BudgetWarning[] {
  return categories
    .filter((c) => c.type === 'expense' && c.budgetLimit)
    .map((c) => {
      const spent = getCategorySpent(c.id, transactions)
      const limit = c.budgetLimit ?? 0
      return {
        categoryId: c.id,
        spent,
        limit,
        percent: limit > 0 ? spent / limit : 0,
      }
    })
    .filter((w) => w.percent >= threshold)
    .sort((a, b) => b.percent - a.percent)
}

const MONTHS_RU = [
  'Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь',
  'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь',
]

const MONTHS_RU_SHORT = [
  'Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн',
  'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек',
]

export function formatPeriodLabel(date = new Date()): string {
  return `${MONTHS_RU[date.getMonth()]} ${date.getFullYear()}`
}

export type ReportPeriod = 'month' | '3months' | '6months' | 'year'

export const REPORT_PERIODS: { id: ReportPeriod; label: string }[] = [
  { id: 'month', label: 'Месяц' },
  { id: '3months', label: '3 мес.' },
  { id: '6months', label: 'Полгода' },
  { id: 'year', label: 'Год' },
]

function parseTxDate(dateStr: string): Date {
  const [y, m, d] = dateStr.split('-').map(Number)
  return new Date(y, m - 1, d)
}

export function getReportPeriodStart(period: ReportPeriod, now = new Date()): Date {
  switch (period) {
    case 'month':
      return new Date(now.getFullYear(), now.getMonth(), 1)
    case '3months':
      return new Date(now.getFullYear(), now.getMonth() - 2, 1)
    case '6months':
      return new Date(now.getFullYear(), now.getMonth() - 5, 1)
    case 'year':
      return new Date(now.getFullYear(), now.getMonth() - 11, 1)
  }
}

export function filterTransactionsByPeriod(
  transactions: Transaction[],
  period: ReportPeriod,
  now = new Date(),
): Transaction[] {
  const start = getReportPeriodStart(period, now)
  const end = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999)
  return transactions.filter((t) => {
    const d = parseTxDate(t.date)
    return d >= start && d <= end
  })
}

export function getReportPeriodDescription(period: ReportPeriod, now = new Date()): string {
  if (period === 'month') return formatPeriodLabel(now)
  const start = getReportPeriodStart(period, now)
  const fmt = (d: Date) =>
    `${String(d.getDate()).padStart(2, '0')}.${String(d.getMonth() + 1).padStart(2, '0')}.${d.getFullYear()}`
  return `${fmt(start)} — ${fmt(now)}`
}

export function formatShortDate(dateStr: string): string {
  const parts = dateStr.split('-')
  if (parts.length === 3) {
    const [y, m, d] = parts
    return `${d.padStart(2, '0')}-${m.padStart(2, '0')}-${y}`
  }
  const date = new Date(dateStr)
  if (Number.isNaN(date.getTime())) return dateStr
  const d = String(date.getDate()).padStart(2, '0')
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const y = date.getFullYear()
  return `${d}-${m}-${y}`
}

export function getMonthsCountForPeriod(period: ReportPeriod): number {
  switch (period) {
    case 'month':
      return 1
    case '3months':
      return 3
    case '6months':
      return 6
    case 'year':
      return 12
  }
}

export interface MonthlyTotal {
  key: string
  label: string
  year: number
  month: number
  expenses: number
  income: number
}

export function getMonthlyTotals(
  transactions: Transaction[],
  monthsCount: number,
  now = new Date(),
): MonthlyTotal[] {
  const result: MonthlyTotal[] = []
  for (let i = monthsCount - 1; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    const year = d.getFullYear()
    const month = d.getMonth()
    const monthTx = transactions.filter((t) => {
      const td = parseTxDate(t.date)
      return td.getFullYear() === year && td.getMonth() === month
    })
    result.push({
      key: `${year}-${String(month + 1).padStart(2, '0')}`,
      label: MONTHS_RU_SHORT[month] ?? MONTHS_RU[month],
      year,
      month,
      expenses: getTotalExpenses(monthTx),
      income: getTotalIncome(monthTx),
    })
  }
  return result
}

export function filterTransactionsByPreviousPeriod(
  transactions: Transaction[],
  period: ReportPeriod,
  now = new Date(),
): Transaction[] {
  const currentStart = getReportPeriodStart(period, now)
  const prevEnd = new Date(currentStart)
  prevEnd.setDate(prevEnd.getDate() - 1)
  prevEnd.setHours(23, 59, 59, 999)

  const prevStart = new Date(currentStart)
  switch (period) {
    case 'month':
      prevStart.setMonth(prevStart.getMonth() - 1)
      break
    case '3months':
      prevStart.setMonth(prevStart.getMonth() - 3)
      break
    case '6months':
      prevStart.setMonth(prevStart.getMonth() - 6)
      break
    case 'year':
      prevStart.setFullYear(prevStart.getFullYear() - 1)
      break
  }

  return transactions.filter((t) => {
    const d = parseTxDate(t.date)
    return d >= prevStart && d <= prevEnd
  })
}

export interface PeriodComparison {
  currentExpenses: number
  previousExpenses: number
  changePercent: number | null
}

export function getPeriodExpenseComparison(
  transactions: Transaction[],
  period: ReportPeriod,
  now = new Date(),
): PeriodComparison {
  const currentExpenses = getTotalExpenses(filterTransactionsByPeriod(transactions, period, now))
  const previousExpenses = getTotalExpenses(filterTransactionsByPreviousPeriod(transactions, period, now))
  const changePercent =
    previousExpenses > 0
      ? Math.round(((currentExpenses - previousExpenses) / previousExpenses) * 100)
      : null
  return { currentExpenses, previousExpenses, changePercent }
}

export interface CategoryTrend {
  categoryId: string
  name: string
  iconId: string
  color: string
  current: number
  previous: number
  changePercent: number | null
}

export function getCategoryExpenseTrends(
  categories: Category[],
  transactions: Transaction[],
  period: ReportPeriod,
  now = new Date(),
): CategoryTrend[] {
  const currentTx = filterTransactionsByPeriod(transactions, period, now)
  const previousTx = filterTransactionsByPreviousPeriod(transactions, period, now)

  return categories
    .filter((c) => c.type === 'expense')
    .map((c) => {
      const current = getCategorySpent(c.id, currentTx)
      const previous = getCategorySpent(c.id, previousTx)
      const changePercent =
        previous > 0 ? Math.round(((current - previous) / previous) * 100) : current > 0 ? 100 : null
      return {
        categoryId: c.id,
        name: c.name,
        iconId: c.iconId,
        color: c.color,
        current,
        previous,
        changePercent,
      }
    })
    .filter((t) => t.current > 0 || t.previous > 0)
    .sort((a, b) => Math.abs(b.changePercent ?? 0) - Math.abs(a.changePercent ?? 0))
}

export function getAverageDailyExpense(
  transactions: Transaction[],
  period: ReportPeriod,
  now = new Date(),
): number {
  const total = getTotalExpenses(filterTransactionsByPeriod(transactions, period, now))
  const start = getReportPeriodStart(period, now)
  const days = Math.max(1, Math.ceil((now.getTime() - start.getTime()) / 86400000) + 1)
  return Math.round(total / days)
}
