# SOYER ONE

Native Android application built with Kotlin and Jetpack Compose.

## Included sections

- Ukrainian-language SOYER EXCHANGE overview and service information.
- Demonstration exchange-rate card: 46.50 UAH and 47.70 UAH.
- USDT ↔ UAH calculator with direction switching and a minimum equivalent of 10 USDT.
- Local calculation history saved on the device, including date and time.
- Partnership information, promotions, Telegram channel and manager contact links.
- Dark forest-green interface.

## Build

Open this repository in Android Studio, allow Gradle sync, then run the `app` configuration on an Android device or emulator.

GitHub Actions builds a debug APK on every push to `main`. The artifact is named `soyer-one-debug`.

- Minimum Android version: Android 8.0 (API 26)
- Language: Kotlin
- UI: Jetpack Compose
- Package: `ua.soyer.one`

## Important

This version is informational and demonstrational only. The displayed rates are test values and are not live quotes. The app does not accept payments, connect wallets or exchanges, or execute transfers. The calculator is not an offer to exchange funds. The history is stored locally on the device and is not synchronized to a server.
