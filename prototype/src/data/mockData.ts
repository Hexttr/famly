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
