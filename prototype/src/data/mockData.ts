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
]

export const mockFamily: FamilyMember[] = [
  { id: 'f1', name: 'Алексей', role: 'admin', visibility: 'full', avatar: '👨' },
  { id: 'f2', name: 'Мария', role: 'member', visibility: 'partial', avatar: '👩' },
  { id: 'f3', name: 'Саша', role: 'viewer', visibility: 'private', avatar: '👦' },
]

export const mockIOU: IOUBalance[] = [
  { fromId: 'f2', toId: 'f1', amount: 1250 },
  { fromId: 'f1', toId: 'f2', amount: 800 },
]

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

export function formatPeriodLabel(date = new Date()): string {
  return `${MONTHS_RU[date.getMonth()]} ${date.getFullYear()}`
}

export function formatShortDate(dateStr: string): string {
  const date = new Date(dateStr)
  const today = new Date()
  const yesterday = new Date()
  yesterday.setDate(today.getDate() - 1)

  if (dateStr === today.toISOString().slice(0, 10)) return 'Сегодня'
  if (dateStr === yesterday.toISOString().slice(0, 10)) return 'Вчера'

  return date.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' })
}
