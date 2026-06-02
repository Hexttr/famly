import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { Layout } from './components/Layout'
import { AppProvider, useApp } from './context/AppContext'
import { AccountsScreen } from './screens/AccountsScreen'
import { AdvancedReportsScreen } from './screens/AdvancedReportsScreen'
import { BackupExportScreen } from './screens/BackupExportScreen'
import { BalancesScreen } from './screens/BalancesScreen'
import { BudgetScreen } from './screens/BudgetScreen'
import { CategoriesScreen } from './screens/CategoriesScreen'
import { CategoryBudgetEditScreen } from './screens/CategoryBudgetEditScreen'
import { FamilyHubScreen } from './screens/FamilyHubScreen'
import { FamilyMemberScreen } from './screens/FamilyMemberScreen'
import { HomeScreen } from './screens/HomeScreen'
import { MoreScreen } from './screens/MoreScreen'
import { OnboardingScreen } from './screens/OnboardingScreen'
import { OperationDetailScreen } from './screens/OperationDetailScreen'
import { OperationsScreen } from './screens/OperationsScreen'
import { PremiumPaywallScreen } from './screens/PremiumPaywallScreen'
import { ReportsScreen } from './screens/ReportsScreen'
import { SettingsScreen } from './screens/SettingsScreen'
import { SplitExpenseScreen } from './screens/SplitExpenseScreen'

function AppRoutes() {
  const { settings } = useApp()

  if (!settings.onboardingComplete) {
    return (
      <Routes>
        <Route path="/onboarding" element={<OnboardingScreen />} />
        <Route path="*" element={<Navigate to="/onboarding" replace />} />
      </Routes>
    )
  }

  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<HomeScreen />} />
        <Route path="/operations" element={<OperationsScreen />} />
        <Route path="/operations/:id" element={<OperationDetailScreen />} />
        <Route path="/budget" element={<BudgetScreen />} />
        <Route path="/budget/:id" element={<CategoryBudgetEditScreen />} />
        <Route path="/categories" element={<CategoriesScreen />} />
        <Route path="/more" element={<MoreScreen />} />
        <Route path="/accounts" element={<AccountsScreen />} />
        <Route path="/reports" element={<ReportsScreen />} />
        <Route path="/settings" element={<SettingsScreen />} />
        <Route path="/backup" element={<BackupExportScreen />} />
        <Route path="/family" element={<FamilyHubScreen />} />
        <Route path="/family/:id" element={<FamilyMemberScreen />} />
        <Route path="/balances" element={<BalancesScreen />} />
        <Route path="/analytics" element={<AdvancedReportsScreen />} />
        <Route path="/split/:id" element={<SplitExpenseScreen />} />
      </Route>
      <Route path="/premium" element={<PremiumPaywallScreen />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

function App() {
  return (
    <AppProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AppProvider>
  )
}

export default App
