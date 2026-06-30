# FinanceApp — Design Spec

**Date:** 2026-06-30
**Status:** Approved (verbal) — written spec under review
**Goal:** Full offline Android personal-finance app (Kotlin / Compose / Material 3), built and delivered in phases. Phase 1 is a working vertical slice.

---

## 1. Tech stack (fixed by client spec)

- Kotlin, Jetpack Compose (Material 3)
- MVVM + Clean Architecture: UI → ViewModel → UseCase → Repository → DataSource
- Room (DB), Hilt (DI), DataStore Preferences (settings)
- WorkManager (background/notifications — phase 4)
- Lottie + Compose animations; Vico (charts)
- AndroidX Biometric
- minSdk 29, compileSdk/targetSdk 35 (Android 15), JDK 17
- i18n: `values/strings.xml` (en, default) + `values-ru/strings.xml` (ru), runtime switch

## 2. Build & environment constraints

- **All tooling + downloads live on F: only** (see memory `f-drive-only`). Layout:
  - `F:\android-dev\jdk\jdk-17*` (Temurin JDK 17)
  - `F:\android-dev\sdk` (`ANDROID_HOME`, `ANDROID_SDK_ROOT`)
  - `F:\android-dev\.android` (`ANDROID_USER_HOME`)
  - `F:\android-dev\gradle-home` (`GRADLE_USER_HOME`)
  - `F:\android-dev\tmp` (build temp)
- Single Gradle module `:app` (the spec's "modules" are Kotlin **packages**, not Gradle modules — keeps builds fast and simple).
- Versions pinned via Gradle **version catalog** (`gradle/libs.versions.toml`).
- Kotlin 2.x with the Compose Compiler Gradle plugin (K2). KSP for Room/Hilt.
- Build verified locally with `./gradlew assembleDebug`.

## 3. Phasing roadmap

| Phase | Scope | Deliverable |
|-------|-------|-------------|
| **1 — Vertical slice (now)** | Gradle project, Room (5 tables) + category seed, Hilt, DataStore, Purple/Orange + light/dark themes, ru/en, nav Splash→Onboarding→Lock(PIN/biometric)→Main. Fully working: Dashboard, Transactions (list + add/edit), Settings (theme/scheme/lang/currency/rates/PIN/biometric). Budgets/Analytics = empty-state placeholders. | Runnable debug APK, working core |
| **2 — Budgets + Categories** | Budget list w/ animated progress (green→yellow→red), budget form, full category management (create/icons/colors, hide system, edit/delete custom). | |
| **3 — Analytics** | Vico Pie (by category) + Bar (income vs expense), top-5 categories, period tabs, empty states. | |
| **4 — Reminders + WorkManager** | Reminder list/form, notification channel "Платежи" with actions (Paid / Remind tomorrow), POST_NOTIFICATIONS, recurring transactions (daily PeriodicWork). | |
| **5 — Backup + polish** | JSON export/import via SAF (kotlinx.serialization), auto-lock timing, full Lottie assets, ProGuard rules, release build. | Full APK per spec |

## 4. Phase 1 — detailed scope

### 4.1 Architecture / packages (`com.financeapp`)
```
core/
  data/      Room DB, entities, DAOs, TypeConverters, DataStore, repository impls, category seed
  domain/    domain models, repository interfaces, use cases
  ui/        theme (color schemes, typography), shared composables, animation helpers
  utils/     currency/date formatters, haptics, PIN hashing (SHA-256)
feature/
  onboarding/ lock/ dashboard/ transactions/ settings/
  budgets/ analytics/   (placeholder screens in phase 1)
  categories/           (management UI deferred to phase 2; seed only in phase 1)
```

### 4.2 Data model (Room)
- `transactions(id, amount REAL, currency TEXT, type TEXT, category_id INT?, note TEXT?, date INT, recurring_rule_id INT?)`
- `categories(id, name TEXT, icon TEXT, color INT, type TEXT, is_custom INT default 0)`
- `budgets(id, category_id INT, limit_amount REAL, currency TEXT, period_type TEXT)`
- `reminders(id, title TEXT, amount REAL?, currency TEXT?, due_date INT, notify_days_before INT, repeat_type TEXT)`
- `recurring_rules(id, template_json TEXT, interval_type TEXT, next_date INT, auto_add INT default 0)`

Enums stored as TEXT: `TransactionType{INCOME,EXPENSE}`, `CategoryType{INCOME,EXPENSE,BOTH}`, `PeriodType{WEEKLY,MONTHLY,YEARLY}`, `RepeatType{NONE,MONTHLY,YEARLY}`, `IntervalType{DAILY,WEEKLY,MONTHLY,YEARLY}`.

All 5 tables + DAOs created in phase 1 (so schema is stable); only `transactions`/`categories` exercised by UI in phase 1.

### 4.3 DataStore (settings) keys
`base_currency` (RUB/USD/EUR), `exchange_rate_usd: Double`, `exchange_rate_eur: Double`, `theme_mode` (SYSTEM/LIGHT/DARK), `color_scheme` (PURPLE/ORANGE), `pin_hash: String` (SHA-256), `biometric_enabled: Boolean`, `language` (ru/en), `onboarding_completed: Boolean`.

### 4.4 Preset categories (seeded on first run)
Expenses: Еда и продукты (restaurant), Транспорт (directions_car), Здоровье (medical_services), Развлечения (sports_esports), Одежда (checkroom), Связь (phone), Коммунальные (home), Образование (school), Путешествия (flight), Другое (more_horiz).
Income: Зарплата (work), Фриланс (laptop), Инвестиции (trending_up), Подарки (card_giftcard), Другое (more_horiz).
Each gets a stable color from the 16-color palette. Names resolved via string resources (ru/en).

### 4.5 Screens (working in phase 1)
- **Splash** — Lottie/static fallback, 1.8 s, route to Lock (if pin set) or Dashboard.
- **Onboarding** — 3 slides (language → base currency + rates → PIN/biometric), progress dots, swipe, "Skip"; writes DataStore + `onboarding_completed`.
- **Lock** — 4-dot PIN, shake + haptic on error, lock 30 s after 5 fails, biometric button when enabled.
- **Dashboard** — balance (CountUp + hide toggle), month income/expense, 7-day mini line chart (Vico), 5 recent transactions (staggered), expandable FAB (Expense/Income).
- **Transactions** — grouped by day, swipe-left delete (confirm), swipe-right duplicate (snackbar undo), long-press menu, chips All/Income/Expense, category + period + currency filters, search, empty state, **add/edit BottomSheet** (type toggle, amount, currency, category scroller, note, date picker, recurring options).
- **Settings** — theme, color scheme, language, base currency + USD/EUR rates, change PIN, biometric toggle.
- **Bottom nav** (5 tabs: home/receipt_long/savings/bar_chart/settings). Budgets/Analytics show empty-state placeholders.

### 4.6 Themes
Material 3 light+dark for both schemes.
- PURPLE: primary `0xFF7C3AED`, secondary `0xFF9F67F8`, tertiary `0xFFE879F9`.
- ORANGE: primary `0xFFEA580C`, secondary `0xFFF97316`, tertiary `0xFFFBBF24`.
No hardcoded colors/strings — all via `MaterialTheme.colorScheme` + string resources.

### 4.7 Animations in scope
Nav slide+fade transitions, BottomSheet spring, FAB expand stagger, CountUp balance, staggered list (index*60 ms), shake keyframes (PIN), SwipeToDismiss, haptics (LongPress on success / error on failure).

## 5. Out of scope for Phase 1 (deferred)
Budget logic, analytics charts, reminders + notifications + WorkManager, recurring-transaction worker, JSON backup/restore (SAF), full custom-category management UI, complete Lottie asset set, ProGuard hardening, release signing. (Schema + nav stubs are in place so later phases plug in cleanly.)

## 6. Phase 1 acceptance criteria
- `./gradlew assembleDebug` produces a debug APK on F: toolchain.
- App launches: Splash → Onboarding (first run) → Lock (if PIN) → Dashboard, no crash.
- Add/edit/delete/duplicate transactions persist via Room; Dashboard balance + recent list update.
- Theme (system/light/dark), color scheme (purple/orange), and language (ru/en) switch live from Settings.
- No hardcoded user-facing strings or colors; runs offline.
