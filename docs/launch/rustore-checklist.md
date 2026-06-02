# RuStore Launch Checklist

## 1. Регистрация

- [ ] Аккаунт разработчика [RuStore Console](https://console.rustore.ru/)
- [ ] Юридическое лицо или ИП (для монетизации)
- [ ] Подключить RuStore Pay SDK (Pay SDK, не legacy BillingClient)

## 2. Продукты подписки

| Product ID | Название | Цена |
|------------|----------|------|
| `famly_premium_monthly` | Famly Premium (месяц) | 199 ₽ |
| `famly_premium_yearly` | Famly Premium (год) | 1500 ₽ |

## 3. Trial (7 дней)

- Локально: `UserPreferences.initTrialIfNeeded()` — 7 дней с первого запуска
- RuStore: настроить introductory offer / trial в консоли (опционально дублировать)
- После trial: downgrade to Free, Premium gates активны

## 4. Интеграция в Android

```kotlin
// app/build.gradle.kts — добавить после публикации в RuStore:
// implementation("ru.rustore.sdk:billingclient:9.x.x")

// FamlyBillingManager.kt — обёртка над RuStore Pay SDK
// - purchase(productId)
// - observeSubscriptionStatus()
// - restorePurchases()
```

Webhook: `POST /webhooks/rustore` на backend (см. `backend/README.md`)

## 5. ASO (RuStore)

**Название:** Famly — семейный бюджет

**Краткое описание:** Учёт расходов и семейный бюджет. Бесплатно для одного. Premium — для всей семьи.

**Ключевые слова:**
- семейный бюджет
- учёт расходов
- домашняя бухгалтерия
- бюджет семьи
- контроль трат
- финансы семьи

**Скриншоты (минимум 4):**
1. Home — safe-to-spend
2. Quick Add — быстрый ввод
3. Budget — прогресс по категориям
4. Family Hub — Premium
5. Premium Paywall

**Категория:** Финансы

## 6. Юридические документы

- [ ] Политика конфиденциальности (`docs/legal/privacy-policy.md`)
- [ ] Пользовательское соглашение (`docs/legal/terms-of-service.md`)
- [ ] Оферта на подписку

## 7. Google Play (параллельно)

- Бесплатная установка (без Play Billing для RU)
- Кнопка Premium → RuStore deep link или in-app ЮKassa (v2)

## 8. Beta

1. Internal testing в RuStore Console
2. 10–20 тестировщиков, 2 недели
3. Метрики: activation (3+ ops/24h), D7 retention, trial→paid

## 9. Release checklist

- [ ] versionCode increment
- [ ] ProGuard rules для RuStore SDK
- [ ] Privacy policy URL в карточке приложения
- [ ] Backend `/webhooks/rustore` deployed
- [ ] Мониторинг `/health`
