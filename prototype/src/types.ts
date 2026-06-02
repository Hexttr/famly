export type TransactionType = 'expense' | 'income'

export interface Account {
  id: string
  name: string
  icon: string
  balance: number
  color: string
}

export interface Category {
  id: string
  name: string
  iconId: string
  type: TransactionType
  color: string
  budgetLimit?: number
}

export interface Transaction {
  id: string
  amount: number
  type: TransactionType
  categoryId: string
  accountId: string
  date: string
  note?: string
  isRecurring?: boolean
  isPrivate?: boolean
  splitMemberIds?: string[]
}

export interface BudgetPeriod {
  startDay: number
  type: 'monthly' | 'biweekly'
}

export interface FamilyMember {
  id: string
  name: string
  role: 'admin' | 'member' | 'viewer'
  visibility: 'full' | 'partial' | 'private'
  avatar: string
}

export interface IOUBalance {
  fromId: string
  toId: string
  amount: number
}

export interface AppSettings {
  theme: 'light' | 'dark'
  budgetPeriod: BudgetPeriod
  currency: string
  onboardingComplete: boolean
  isPremium: boolean
  trialEndsAt: string | null
}
