# Famly — спецификация экранов

Контракт для реализации в Jetpack Compose. Прототип: `prototype/src/screens/`.

## Навигация

| Вкладка | Route | Экран |
|---------|-------|-------|
| Главная | `/` | HomeScreen |
| Операции | `/operations` | OperationsScreen |
| Бюджет | `/budget` | BudgetScreen |
| Ещё | `/more` | MoreScreen |

FAB на всех вкладках (кроме onboarding/premium) → QuickAddSheet (bottom sheet).

---

## Free экраны

### 1. OnboardingScreen
- **3 слайда:** быстрый ввод / бюджет с даты зарплаты / 7 дней Premium
- **Actions:** Далее, Пропустить, Начать
- **Persist:** `onboardingComplete = true`

### 2. HomeScreen
- **Hero card:** Safe-to-spend (осталось), потрачено, дней до конца периода
- **Progress bar:** общий бюджет периода
- **2 stat cards:** доходы / расходы
- **List:** последние 5 операций → OperationDetail

### 3. QuickAddSheet (modal)
- **Fields:** тип (расход/доход), сумма, категория (chips), счёт, заметка, повтор
- **Action:** Сохранить → добавить Transaction, закрыть sheet

### 4. OperationsScreen
- **Search** + фильтры (все / расходы / доходы)
- **List:** операции с иконкой категории, датой, суммой

### 5. OperationDetailScreen
- **Display:** сумма, категория, дата, счёт, повтор
- **Actions:** Split (Premium gate), Удалить

### 6. BudgetScreen
- **Summary:** общий лимит / потрачено / progress
- **List:** категории с progress bar → CategoryBudgetEdit

### 7. CategoryBudgetEditScreen
- **Edit:** лимит бюджета
- **Display:** потрачено vs лимит
- **Hint:** Rollover — Premium

### 8. CategoriesScreen
- **Tabs:** расходы / доходы
- **CRUD:** список + добавить + удалить

### 9. AccountsScreen
- **Summary:** общий баланс
- **CRUD:** счета с иконкой и балансом

### 10. ReportsScreen
- **Donut chart:** топ-5 категорий расходов
- **List:** категория, сумма, %
- **Hint:** расширенная аналитика — Premium

### 11. SettingsScreen
- **Budget period:** день начала (1–28)
- **Currency:** ₽ (readonly v1)
- **Theme:** светлая / тёмная

### 12. BackupExportScreen
- **Actions:** JSON backup, CSV export (30 дней free)
- **Hint:** CSV без лимита — Premium

---

## Premium экраны

### 13. PremiumPaywallScreen
- **Plans:** 199 ₽/мес, 1500 ₽/год
- **Compare:** Free vs Premium feature lists
- **Trial badge:** дней осталось
- **CTA:** Оформить / Продолжить Premium

### 14. FamilyHubScreen
- **Gate:** PremiumGate
- **List:** участники (avatar, role, visibility)
- **Action:** Пригласить по ссылке/QR

### 15. FamilyMemberScreen
- **Edit:** role (admin/member/viewer), visibility (full/partial/private)

### 16. SplitExpenseScreen
- **Gate:** PremiumGate
- **Checkboxes:** участники семьи, доли суммы
- **Action:** Сохранить split

### 17. BalancesScreen (IOU)
- **Gate:** PremiumGate
- **List:** from → to, сумма, «Закрыть долг»

### 18. AdvancedReportsScreen
- **Gate:** PremiumGate
- **Bar chart:** 3 месяца тренд
- **Compare:** текущий vs прошлый период

---

## Design tokens (Compose mapping)

| Token | Light | Compose |
|-------|-------|---------|
| primary | `#2D6A4F` | `Color(0xFF2D6A4F)` |
| background | `#F8FAF9` | MaterialTheme colorScheme.background |
| surface | `#FFFFFF` | colorScheme.surface |
| expense | `#E63946` | custom SemanticColors |
| income | `#2D6A4F` | custom SemanticColors |
| radius md | 12dp | `RoundedCornerShape(12.dp)` |
| spacing md | 16dp | `16.dp` |

Полный набор: `prototype/src/theme.ts`
