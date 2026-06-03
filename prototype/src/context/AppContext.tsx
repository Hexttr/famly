import { createContext, useContext, useState, type ReactNode } from 'react'
import {
  mockAccounts,
  mockCategories,
  mockFamily,
  mockIOU,
  mockTransactions,
} from '../data/mockData'
import type {
  Account,
  AppSettings,
  Category,
  FamilyMember,
  IOUBalance,
  Transaction,
} from '../types'

interface QuickAddPreset {
  categoryId?: string
  type?: 'expense' | 'income'
}

interface AppContextValue {
  accounts: Account[]
  categories: Category[]
  transactions: Transaction[]
  family: FamilyMember[]
  iouBalances: IOUBalance[]
  settings: AppSettings
  quickAddOpen: boolean
  quickAddPreset: QuickAddPreset | null
  setQuickAddOpen: (open: boolean) => void
  openQuickAdd: (preset?: QuickAddPreset) => void
  addTransaction: (tx: Omit<Transaction, 'id'>) => void
  updateTransaction: (id: string, tx: Partial<Transaction>) => void
  deleteTransaction: (id: string) => void
  updateCategory: (id: string, data: Partial<Category>) => void
  addCategory: (cat: Omit<Category, 'id'>) => void
  deleteCategory: (id: string) => void
  updateAccount: (id: string, data: Partial<Account>) => void
  addAccount: (acc: Omit<Account, 'id'>) => void
  deleteAccount: (id: string) => void
  updateSettings: (data: Partial<AppSettings>) => void
  completeOnboarding: () => void
  activatePremium: () => void
  daysLeftInTrial: () => number
}

const defaultSettings: AppSettings = {
  theme: 'light',
  budgetPeriod: { startDay: 28, type: 'monthly' },
  currency: '₽',
  onboardingComplete: false,
  isPremium: false,
  trialEndsAt: new Date(Date.now() + 7 * 86400000).toISOString(),
}

const SETTINGS_STORAGE_KEY = 'famly-app-settings'

function loadStoredSettings(): Partial<AppSettings> {
  try {
    const raw = localStorage.getItem(SETTINGS_STORAGE_KEY)
    if (!raw) return {}
    return JSON.parse(raw) as Partial<AppSettings>
  } catch {
    return {}
  }
}

function persistSettings(settings: AppSettings) {
  try {
    localStorage.setItem(SETTINGS_STORAGE_KEY, JSON.stringify(settings))
  } catch {
    // приватный режим или переполнение — игнорируем
  }
}

const AppContext = createContext<AppContextValue | null>(null)

export function AppProvider({ children }: { children: ReactNode }) {
  const [accounts, setAccounts] = useState(mockAccounts)
  const [categories, setCategories] = useState(mockCategories)
  const [transactions, setTransactions] = useState(mockTransactions)
  const [family] = useState(mockFamily)
  const [iouBalances] = useState(mockIOU)
  const [settings, setSettings] = useState<AppSettings>(() => ({
    ...defaultSettings,
    ...loadStoredSettings(),
  }))
  const [quickAddOpen, setQuickAddOpen] = useState(false)
  const [quickAddPreset, setQuickAddPreset] = useState<QuickAddPreset | null>(null)

  const openQuickAdd = (preset?: QuickAddPreset) => {
    setQuickAddPreset(preset ?? null)
    setQuickAddOpen(true)
  }

  const handleSetQuickAddOpen = (open: boolean) => {
    setQuickAddOpen(open)
    if (!open) setQuickAddPreset(null)
  }

  const updateSettings = (data: Partial<AppSettings>) =>
    setSettings((s) => {
      const next = { ...s, ...data }
      persistSettings(next)
      return next
    })

  const addTransaction = (tx: Omit<Transaction, 'id'>) =>
    setTransactions((prev) => [{ ...tx, id: `t${Date.now()}` }, ...prev])

  const updateTransaction = (id: string, tx: Partial<Transaction>) =>
    setTransactions((prev) => prev.map((t) => (t.id === id ? { ...t, ...tx } : t)))

  const deleteTransaction = (id: string) =>
    setTransactions((prev) => prev.filter((t) => t.id !== id))

  const updateCategory = (id: string, data: Partial<Category>) =>
    setCategories((prev) => prev.map((c) => (c.id === id ? { ...c, ...data } : c)))

  const addCategory = (cat: Omit<Category, 'id'>) =>
    setCategories((prev) => [...prev, { ...cat, id: `c${Date.now()}` }])

  const deleteCategory = (id: string) =>
    setCategories((prev) => prev.filter((c) => c.id !== id))

  const updateAccount = (id: string, data: Partial<Account>) =>
    setAccounts((prev) => prev.map((a) => (a.id === id ? { ...a, ...data } : a)))

  const addAccount = (acc: Omit<Account, 'id'>) =>
    setAccounts((prev) => [...prev, { ...acc, id: `a${Date.now()}` }])

  const deleteAccount = (id: string) =>
    setAccounts((prev) => prev.filter((a) => a.id !== id))

  const completeOnboarding = () => updateSettings({ onboardingComplete: true })

  const activatePremium = () =>
    updateSettings({ isPremium: true, trialEndsAt: null })

  const daysLeftInTrial = () => {
    if (!settings.trialEndsAt || settings.isPremium) return 0
    const diff = new Date(settings.trialEndsAt).getTime() - Date.now()
    return Math.max(0, Math.ceil(diff / 86400000))
  }

  return (
    <AppContext.Provider
      value={{
        accounts,
        categories,
        transactions,
        family,
        iouBalances,
        settings,
        quickAddOpen,
        quickAddPreset,
        setQuickAddOpen: handleSetQuickAddOpen,
        openQuickAdd,
        addTransaction,
        updateTransaction,
        deleteTransaction,
        updateCategory,
        addCategory,
        deleteCategory,
        updateAccount,
        addAccount,
        deleteAccount,
        updateSettings,
        completeOnboarding,
        activatePremium,
        daysLeftInTrial,
      }}
    >
      {children}
    </AppContext.Provider>
  )
}

export function useApp() {
  const ctx = useContext(AppContext)
  if (!ctx) throw new Error('useApp must be used within AppProvider')
  return ctx
}
