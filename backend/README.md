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

## Environment

- `JWT_SECRET` — production JWT secret

## Database

H2 file database for development. Replace with PostgreSQL for production.
