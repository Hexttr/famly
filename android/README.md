# Famly Android

Jetpack Compose + Room + DataStore. Offline-first family budget app with optional cloud sync and RuStore billing.

## Requirements

- Android Studio Ladybug (2024.2+) or newer
- JDK 17
- Android SDK 35, min SDK 26

## Build

### Gradle wrapper

If `gradlew` is missing, generate it from the `android/` directory:

```bash
# With Gradle installed globally:
gradle wrapper --gradle-version 8.11.1

# Or copy gradlew / gradlew.bat from another Android project into android/
```

Then build and run:

```bash
cd android
./gradlew assembleDebug          # Linux / macOS
gradlew.bat assembleDebug          # Windows
```

Open `android/` in Android Studio → **Sync Gradle** → Run on emulator or device (API 26+).

### Run unit tests

```bash
cd android
./gradlew test
# Single class:
./gradlew test --tests "com.famly.app.domain.BudgetCalculatorTest"
```

## Architecture

```
app/src/main/java/com/famly/app/
├── data/
│   ├── local/          Room entities, DAOs, DataStore (UserPreferences)
│   ├── remote/         FamlyApiClient (auth, sync, subscription)
│   ├── sync/           SyncRepository (offline queue, push/pull)
│   ├── export/         BackupExporter, CsvExporter, ExcelExporter
│   └── repository/     FamlyRepository (single source of truth)
├── domain/             BudgetCalculator, MoneyFormatter, ReportAnalytics, IOU netting
├── billing/            FamlyBillingManager + RuStore stub
└── ui/                 Compose screens, FamlyViewModel, navigation
```

**Data flow:** UI → `FamlyViewModel` → `FamlyRepository` / `SyncRepository` → Room + DataStore. Sync pushes local entities to backend and applies remote changes via `lastSyncToken`.

## Features

| Area | Description |
|------|-------------|
| Onboarding | 7-day trial timer |
| Budget | Safe-to-spend, category limits, period start day |
| Operations | Quick add, split (Premium), IOU balances |
| Reports | Period analytics, category trends |
| Export | JSON backup, CSV, Excel (.xlsx via Apache POI) |
| Sync | Register/login, household create/join, push/pull |
| Billing | RuStore stub → `activatePremium()` on success |

### Export tiers

- **Free:** CSV and Excel limited to last 30 days
- **Premium:** full period export (month / 3 / 6 / 12 months)

### Sync

Backend runs at `http://10.0.2.2:8080` on emulator (see `backend/`). Call `viewModel.syncNow()` after auth + household setup.

## Backend

Start the Ktor server from `backend/` before testing sync:

```bash
cd backend
./gradlew run
```

## RuStore billing (production)

1. Add `ru.rustore.sdk:billingclient` to `app/build.gradle.kts`
2. Set `console_app_id` in `AndroidManifest.xml`
3. Replace stub calls in `RuStoreBillingManager`
4. Wire webhooks to `POST /webhooks/rustore`

## Project status

Phases 1–8: UI, domain logic, export, sync client, unit tests, billing stub.
