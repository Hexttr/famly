# Famly Backend

Ktor REST API for Premium features: household sync, family roles, subscription webhooks.

## Run

```bash
cd backend
./gradlew run
```

Server: http://localhost:8080

## Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/health` | No | Health check |
| POST | `/auth/register` | No | Register user |
| POST | `/auth/login` | No | Login, get JWT |
| GET | `/households/mine` | JWT | Current household |
| POST | `/households` | JWT | Create household |
| POST | `/households/join` | JWT | Join by invite code |
| POST | `/households/{id}/invite` | JWT | Get invite link |
| POST | `/sync/push` | JWT | Push sync entities |
| GET | `/sync/pull?since=` | JWT | Pull changes |
| GET | `/subscription/status` | JWT | Premium status |
| POST | `/webhooks/rustore` | No | RuStore subscription events |
| POST | `/webhooks/yookassa` | No | YooKassa payment events |

## Admin panel

Web UI: `https://api.jazz68.ru/admin` (or `http://localhost:8080/admin` locally).

Set credentials in environment before first start (seeded on boot):

- `ADMIN_EMAIL` — admin login email
- `ADMIN_PASSWORD` — admin password (min 6 chars)

Session: HttpOnly cookie (`famly_admin`), 8 h expiry. POST forms require CSRF token.

JSON API (Bearer token from `POST /admin/login` with `Content-Type: application/json`) remains at `/admin/stats`, `/admin/users`, `/admin/households`, `/admin/sync-log`, etc.

## Environment

- `JWT_SECRET` — production JWT secret
- `ADMIN_EMAIL`, `ADMIN_PASSWORD` — web admin login (optional locally)
- `MONETIZATION_ENABLED` — `true` to enable subscription grant API

## Database

Persistent storage via Exposed ORM (H2 file by default, PostgreSQL via `DATABASE_URL`).

Legal pages: `GET /legal/privacy`, `GET /legal/terms`

## Docker

```bash
cd backend && gradle installDist
docker compose up -d backend
```
