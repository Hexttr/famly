# Деплой Famly на jazz68.ru

## Ваш сервер

- IP: `178.170.165.78`
- Backend в Docker на `127.0.0.1:8080`
- Публичный URL: `https://api.jazz68.ru`

## 1. DNS (сделайте в панели регистратора jazz68.ru)

| Тип | Имя | Значение | TTL |
|-----|-----|----------|-----|
| **A** | `api` | `178.170.165.78` | 300–3600 |

Итог: `api.jazz68.ru` → `178.170.165.78`

Проверка (через 5–30 мин):

```bash
nslookup api.jazz68.ru
```

## 2. SSL (после того как DNS заработал)

На сервере по SSH:

```bash
sudo certbot --nginx -d api.jazz68.ru
sudo nginx -t && sudo systemctl reload nginx
```

## 3. Проверка

```bash
curl https://api.jazz68.ru/health
curl https://api.jazz68.ru/legal/privacy
```

## 4. Android release

В `android/app/build.gradle.kts`:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://api.jazz68.ru\"")
```

RuStore → Privacy policy: `https://api.jazz68.ru/legal/privacy`

## Обновление backend

```bash
cd famly
# локально:
set DEPLOY_HOST=178.170.165.78
set DEPLOY_USER=user_adm
set DEPLOY_PASS=...
python deploy/deploy_backend.py
```
