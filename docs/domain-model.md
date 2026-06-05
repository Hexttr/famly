# Famly — domain model

Контракт данных для Room (Android) и API (backend).

## Entities

### Account
```kotlin
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val balance: Long,      // kopecks
    val color: String,
    val sortOrder: Int = 0,
    val createdAt: Long,
    val updatedAt: Long,
    val syncVersion: Int = 0,
    val deleted: Boolean = false
)
```

### Category
```kotlin
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val type: String,       // "expense" | "income"
    val color: String,
    val budgetLimit: Long?, // kopecks, null = no limit
    val sortOrder: Int = 0,
    val createdAt: Long,
    val updatedAt: Long,
    val syncVersion: Int = 0,
    val deleted: Boolean = false
)
```

### Transaction
```kotlin
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amount: Long,       // kopecks, always positive
    val type: String,       // "expense" | "income"
    val categoryId: String,
    val accountId: String,
    val date: Long,         // epoch day or millis
    val note: String?,
    val isRecurring: Boolean = false,
    val isPrivate: Boolean = false,
    val householdId: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val syncVersion: Int = 0,
    val deleted: Boolean = false
)
```

### BudgetPeriod (UserPreferences / DataStore)
```kotlin
data class BudgetPeriod(
    val startDay: Int,      // 1-28
    val type: String        // "monthly" | "biweekly"
)
```

### AppSettings (DataStore)
```kotlin
data class AppSettings(
    val theme: String,              // "light" | "dark" | "system"
    val budgetPeriod: BudgetPeriod,
    val currency: String = "RUB",
    val onboardingComplete: Boolean = false,
    val isPremium: Boolean = false,
    val trialEndsAt: Long?,         // epoch millis
    val premiumExpiresAt: Long?,
    val householdId: String?
)
```

## Premium entities (sync via backend)

### Household
```kotlin
data class Household(
    val id: String,
    val name: String,
    val ownerId: String,
    val maxMembers: Int = 6,
    val createdAt: Long
)
```

### HouseholdMember
```kotlin
data class HouseholdMember(
    val id: String,
    val householdId: String,
    val userId: String,
    val displayName: String,
    val role: String,       // "admin" | "member" | "viewer"
    val visibility: String, // "full" | "partial" | "private"
    val avatar: String = "",
    val joinedAt: Long
)
```

### Split
```kotlin
data class SplitEntity(
    @PrimaryKey val id: String,
    val transactionId: String,
    val memberId: String,
    val amount: Long,       // kopecks
    val settled: Boolean = false
)
```

### IOUBalance (computed)
```kotlin
data class IOUBalance(
    val fromMemberId: String,
    val toMemberId: String,
    val amount: Long
)
```

## Use cases

| Use case | Input | Output |
|----------|-------|--------|
| GetSafeToSpend | period | Long (kopecks) |
| GetPeriodBounds | startDay, now | Pair<Long, Long> |
| AddTransaction | Transaction | Unit |
| ExportCsv | daysLimit? | String |
| ExportBackup | — | JSON |
| ImportBackup | JSON | Result |
| HasPremiumAccess | settings | Boolean |
| SyncHousehold | householdId | SyncResult |

## Safe-to-spend formula

```
safeToSpend = sum(budgetLimits for expense categories)
            - sum(expenses in current period)
            + rolloverAmount (Premium only, default 0)
```

## Money storage

All amounts stored as **Long kopecks** (1 ₽ = 100 kopecks) to avoid floating-point errors.

Display: `(amount / 100).formatWithLocale("ru-RU") + " ₽"`

## Sync protocol (Premium)

- Client generates UUID for each entity
- `syncVersion` incremented on each local change
- Push: POST changed entities since last sync token
- Pull: GET entities where `updatedAt > lastSync`
- Conflict: last-write-wins by `updatedAt`, admin wins on tie

## API endpoints (backend)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/auth/register` | Email/phone + password |
| POST | `/auth/login` | JWT token |
| GET | `/households/mine` | Current household |
| POST | `/households` | Create household |
| POST | `/households/{id}/invite` | Generate invite link |
| POST | `/households/join` | Join by invite code |
| GET | `/sync/pull` | Pull changes |
| POST | `/sync/push` | Push changes |
| GET | `/subscription/status` | Premium status |
| POST | `/webhooks/rustore` | RuStore subscription webhook |
| POST | `/webhooks/yookassa` | YooKassa payment webhook |
