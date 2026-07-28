<h1 align="center">estate</h1>

<p align="center">
  <em>Personal finance tracker for Android</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-29%2B-3DDC84?style=flat&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=flat&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-BOM%202024-4285F4?style=flat&logo=jetpackcompose&logoColor=white" />
  <img src="https://img.shields.io/badge/Material%203-0097A7?style=flat" />
  <img src="https://img.shields.io/badge/License-MIT-green?style=flat" />
</p>

---

## Features

- 📊 **Dashboard** — total balance, income/expense summary, exchange rates at a glance
- 💳 **Transactions** — add, edit, duplicate, swipe-to-delete with undo; grouped by day
- 🧮 **Built-in calculator** — compute amounts right when adding a transaction
- 📈 **Analytics** — spending by category, income vs expense trend, savings rate, daily velocity
- 💰 **Budgets** — set weekly/monthly/yearly limits per category with progress bars
- 🔁 **Recurring transactions** — daily/weekly/monthly/yearly auto-add rules
- 🎯 **Savings goals** — track progress toward a target amount with deadlines
- 📁 **Projects** — group transactions by project with budgets and analytics
- 🔔 **Reminders** — due-date notifications with repeat options
- 🏆 **Achievements** — unlock badges for milestones (first transaction, 100 transactions, streaks)
- 💱 **Multi-currency** — RUB, USD, EUR, CNY, KZT with auto-updating exchange rates
- 🎨 **Themes** — 4 color schemes (Green, Amber, Indigo, Terracotta), AMOLED dark, auto theme switch
- 🔒 **Security** — PIN code + biometric authentication, auto-lock
- 💾 **Backup & restore** — export/import all data
- 📤 **Export** — CSV and Excel export with period selection
- 🌐 **2 languages** — Russian and English
- ✨ **Smooth animations** — Lottie splash, haptic feedback, gesture-based interactions

## Tech Stack

| Category | Technology |
|----------|-----------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Database | Room |
| Preferences | DataStore |
| Charts | Vico |
| Animations | Lottie |
| Async | Kotlin Coroutines |
| Navigation | Compose Navigation |
| Biometrics | AndroidX Biometric |
| Build | Gradle Kotlin DSL |

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- JDK 17+
- Android SDK 34+

### Build

```bash
git clone https://github.com/neasyde/estate.git
cd estate
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/estate.apk`.

### Install on device

```bash
adb install app/build/outputs/apk/debug/estate.apk
```

## Project Structure

```
app/src/main/java/com/financeapp/
├── core/
│   ├── data/          # Room DB, DataStore, API, repositories
│   ├── domain/        # Models, use cases, repository interfaces
│   ├── ui/            # Components, theme, icons, animations
│   └── utils/         # Currency, date, locale helpers
├── feature/
│   ├── dashboard/     # Home screen
│   ├── transactions/  # Transaction list + add/edit
│   ├── budgets/       # Budget management
│   ├── analytics/     # Charts and statistics
│   ├── categories/    # Category management
│   ├── recurring/     # Recurring transaction rules
│   ├── reminders/     # Bill reminders
│   ├── achievements/  # Achievement badges
│   ├── goals/         # Savings goals
│   ├── projects/      # Project tracker
│   ├── settings/      # App settings
│   ├── onboarding/    # First-run flow
│   └── lock/          # PIN / biometric lock
├── navigation/        # Routes, NavHost, BottomBar
└── MainActivity.kt
```

## License

This project is licensed under the MIT License.
