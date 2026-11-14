# SOYER ONE

Native Android application built with Kotlin and Jetpack Compose.

## Features

- Ukrainian-language SOYER EXCHANGE overview.
- Shared server-managed USDT/UAH rates and minimum amount.
- USDT ↔ UAH calculator using the current server settings.
- Local calculation history saved on the device.
- Company, partnership, promotions and Telegram contact information.
- Hidden administrator entry (seven taps on the logo); server verifies the signed-in Telegram ID against `ADMIN_IDS`.
- Create exchange requests from the calculator and store them on the API server.
- Admin can review requests and change their status (new, processing, completed, cancelled).
- Admin changes are saved through the SOYER API and are visible to Android users on refresh (the app refreshes settings every 60 seconds).
- Dark interface matching the website palette: `#080A0F`, `#111722`, blue/violet accents and mint status indicators.

## API

The Android client currently connects to:

`https://soyer-exchange-api.onrender.com`

The API server must be deployed from the `EgorMr111/soyer-exchange` repository. The app uses `/api/settings`, `/api/auth/telegram`, `/api/auth/me`, `/api/requests`, `/api/admin/me`, `/api/admin/requests`, `/api/admin/requests/:id` and `/api/admin/settings`.

## Build

Open this repository in Android Studio, allow Gradle sync, then run the `app` configuration on an Android device or emulator.

GitHub Actions builds a debug APK on every push to `main`. The artifact is named `soyer-one-debug`.

- Minimum Android version: Android 8.0 (API 26)
- Language: Kotlin
- UI: Jetpack Compose
- Package: `ua.soyer.one`

## Important

The app does not accept payments, connect wallets or exchanges, or execute transfers. An exchange request is a contact/request record only; final terms must be confirmed with the service team. Calculation history remains local to the device.
