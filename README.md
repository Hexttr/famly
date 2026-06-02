# Famly

Семейный бюджет для Android (RU-only). Самый сильный free-tier на рынке + Premium для семьи.

## Структура проекта

```
famly/
├── prototype/     # React UX-прототип (18 экранов)
├── android/       # Production: Jetpack Compose + Room
├── backend/       # Ktor API: sync, household, webhooks
└── docs/          # Спеки, legal, launch checklists
```

## Быстрый старт

### Прототип (веб)

```bash
cd prototype
npm install
npm run dev
```

### Android

Откройте `android/` в Android Studio → Run (API 26+)

### Backend

```bash
cd backend
gradle run   # или ./gradlew run
```

## Монетизация

| | |
|---|---|
| Free | 1 пользователь, полный solo-функционал |
| Premium | 199 ₽/мес или 1500 ₽/год |
| Trial | 7 дней Premium при регистрации |
| v1 billing | RuStore Pay SDK |
| v2 billing | Google Play install + ЮKassa |

## Документация

- [Спека экранов](docs/screens.md)
- [Domain model](docs/domain-model.md)
- [Free vs Premium](docs/free-vs-premium.md)
- [RuStore launch](docs/launch/rustore-checklist.md)
- [ЮKassa integration](docs/launch/yookassa-integration.md)

## Workflow web → Android

1. Прототип в `prototype/` — итерации UX
2. `docs/screens.md` — контракт для Compose
3. `android/` — нативная реализация экран за экраном

Прототип не портируется автоматически — переносятся дизайн и логика.
