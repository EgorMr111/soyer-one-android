# SOYER ONE

Native Android application built with Kotlin and Jetpack Compose.

## Features

- Ukrainian-language SOYER EXCHANGE overview.
- Shared server-managed USDT/UAH rates and minimum amount.
- USDT ↔ UAH calculator using the current server settings.
- Local calculation history saved on the device.
- Company, partnership, promotions and Telegram contact information.
- Server-backed administrator panel in the **Адмін** tab.
- Admin login uses the same server-side password as the SOYER EXCHANGE website; no password is embedded in the APK.
- Admin changes are saved through the SOYER API and are visible to Android users on refresh (the app refreshes settings every 60 seconds).
- Dark forest-green interface.

## API

The Android client currently connects to:

`https://soyer-exchange-api.onrender.com`

The API server must be deployed from the `EgorMr111/soyer-exchange` repository. The app uses the existing routes `/api/settings`, `/api/admin/login`, `/api/admin/me`, `/api/admin/settings` and `/api/admin/logout`.

## Build

Open this repository in Android Studio, allow Gradle sync, then run the `app` configuration on an Android device or emulator.

GitHub Actions builds a debug APK on every push to `main`. The artifact is named `soyer-one-debug`.

- Minimum Android version: Android 8.0 (API 26)
- Language: Kotlin
- UI: Jetpack Compose
- Package: `ua.soyer.one`

## Important

The displayed rates are informational quotes and do not execute an exchange. The app does not accept payments, connect wallets or exchanges, or execute transfers. Calculation history is stored locally on the device and is not synchronized.
