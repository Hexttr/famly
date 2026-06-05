# Famly Android — Release (RuStore beta)

## Version

- `versionName`: 1.0.4
- `versionCode`: 5
- Monetization: **disabled** (`MONETIZATION_ENABLED=false`)

## Build AAB

```bash
cd android
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

## Signing

Create a keystore (once, outside the repo):

```bash
keytool -genkey -v -keystore famly-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias famly
```

Add to `android/gradle.properties` (do not commit secrets):

```properties
RELEASE_STORE_FILE=/path/to/famly-release.jks
RELEASE_STORE_PASSWORD=...
RELEASE_KEY_ALIAS=famly
RELEASE_KEY_PASSWORD=...
```

Without these properties, release builds use the debug keystore (local testing only).

## Backend

Deploy before release testing:

```bash
cd backend
./gradlew installDist
docker compose up -d backend
```

Set `JWT_SECRET` in production. Release app points to `https://api.famly.app`.

Legal pages: `{API_BASE_URL}/legal/privacy` and `/legal/terms`

## RuStore Internal Testing

See [docs/launch/rustore-beta-listing.md](../docs/launch/rustore-beta-listing.md)

## Enable monetization later

1. Set `MONETIZATION_ENABLED=true` in `app/build.gradle.kts` (release)
2. Integrate RuStore Pay SDK in `RuStoreBillingManager`
3. Restore Premium UI (already gated by `FamlyAccess.showPaywall()`)
