# Famly Android

Jetpack Compose + Room + DataStore. Offline-first free tier.

## Open in Android Studio

1. Open `android/` folder
2. Sync Gradle
3. Run on emulator or device (API 26+)

## Architecture

- `data/local/` — Room entities, DAOs, DataStore preferences
- `data/repository/` — FamlyRepository
- `domain/` — BudgetCalculator, MoneyFormatter
- `ui/screens/` — Compose screens (mirror prototype)
- `ui/navigation/` — NavHost + bottom tabs

## Features (Phase 1)

- Onboarding + 7-day trial timer
- Home with safe-to-spend
- Quick add (FAB + bottom sheet)
- Operations, Budget, Categories, Accounts
- Reports, Settings, Backup placeholders
- Premium gates for Family, IOU, Analytics

## Next (Phase 2)

- Backend sync (see `backend/`)
- RuStore Billing SDK
- Real JSON/CSV export
