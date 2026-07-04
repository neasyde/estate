# estate Redesign Continuation — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Elevate currency onto Home with out-of-the-box auto-rates, expand Settings for far more control, smooth motion without FPS loss, redesign first-launch screens (incl. animated PIN dots + auto-biometric), and do a full visual sweep — all in the existing Quiet Minimal language.

**Architecture:** Clean-ish layering already present: `core/domain` (models, repository interfaces, use-cases), `core/data` (DataStore, Room, remote, repository impls), `feature/*` (Compose screens + Hilt VMs). New logic goes behind testable use-cases; UI stays in `feature/*` and `core/ui`. Settings are DataStore-backed via `AppSettings` + `SettingsRepository`.

**Tech Stack:** Kotlin, Jetpack Compose (Material3), Hilt, Room, DataStore Preferences, kotlinx.serialization, Robolectric/JUnit/Truth for tests. Build via F: toolchain, Gradle 8.10.2, `--offline`.

## Global Constraints

- All downloads/toolchain stay on **F:** only (see build-env memory). Build: `source /f/android-dev/env.sh && ./gradlew ... --offline`.
- Keep the **Quiet Minimal** language: flat (no gradients/shadows), warm palette, Fraunces serif for amounts/titles, Inter for UI, monochrome rounded icons, hairline lists. Deep-green accent default.
- **No new dependencies** unless unavoidable (offline build). Reuse `HttpURLConnection`, existing libs.
- Robolectric tests pin `sdk=34` (already configured).
- Every new user-facing string added for **both** RU and EN (`values/strings.xml`, `values-en/strings.xml`).
- New *portable* settings included in backup; security-sensitive ones (PIN, biometric, api key) excluded — match existing `BackupData`/`SettingsBackup` pattern.
- Small commits per task, conventional-commit messages, on `master`. Keep unit tests green (currently 36).
- Secrets: the exchangerate API key lives ONLY in `local.properties` (gitignored) → `BuildConfig`. Never commit the key value.

---

## Phase 0 — Build setup

### Task 0.1: BuildConfig API key + rename debug APK

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `local.properties` (gitignored — add key line)

**Interfaces:**
- Produces: `BuildConfig.EXCHANGE_API_KEY: String` (empty string when absent); debug APK named `estate.apk`.

- [ ] **Step 1: Add the key to `local.properties`** (gitignored, not committed)

Append this line (real value from the user; `<KEY>` placeholder shown here only):
```
EXCHANGE_API_KEY=<KEY>
```

- [ ] **Step 2: Enable buildConfig + inject the field + rename debug APK**

In `app/build.gradle.kts`, top of `android { }` add a properties read, then wire the field and output name:
```kotlin
import java.util.Properties

// inside android { } — read local.properties for the default API key
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val exchangeApiKey: String = (localProps.getProperty("EXCHANGE_API_KEY") ?: "")

// in defaultConfig { }
buildConfigField("String", "EXCHANGE_API_KEY", "\"$exchangeApiKey\"")

// change buildFeatures to:
buildFeatures { compose = true; buildConfig = true }

// rename the debug APK output
applicationVariants.all {
    if (buildType.name == "debug") {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName = "estate.apk"
        }
    }
}
```

- [ ] **Step 3: Build to verify BuildConfig + APK name**

Run: `source /f/android-dev/env.sh && ./gradlew :app:assembleDebug --offline`
Expected: BUILD SUCCESSFUL; `ls app/build/outputs/apk/debug/estate.apk` exists.

- [ ] **Step 4: Commit**
```bash
git add app/build.gradle.kts
git commit -m "build: inject EXCHANGE_API_KEY via BuildConfig + name debug APK estate.apk"
```
(Do not `git add local.properties` — it is gitignored.)

---

## Phase 1 — 💱 Currency: elevated + auto-refresh

### Task 1.1: `AppConfig` seam + `shouldRefreshRates` decision (testable)

**Files:**
- Create: `app/src/main/java/com/financeapp/core/config/AppConfig.kt`
- Create: `app/src/main/java/com/financeapp/core/config/AppConfigModule.kt`
- Create: `app/src/main/java/com/financeapp/core/domain/usecase/RatesRefreshPolicy.kt`
- Test: `app/src/test/java/com/financeapp/core/domain/usecase/RatesRefreshPolicyTest.kt`

**Interfaces:**
- Produces:
  - `interface AppConfig { val defaultExchangeApiKey: String }`
  - `fun resolveApiKey(userKey: String?, config: AppConfig): String?` — returns userKey if non-blank, else config default if non-blank, else null.
  - `fun shouldRefreshRates(now: Long, updatedAt: Long, intervalMs: Long, hasKey: Boolean): Boolean`

- [ ] **Step 1: Write failing tests**
```kotlin
package com.financeapp.core.domain.usecase

import com.financeapp.core.config.AppConfig
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RatesRefreshPolicyTest {
    private fun cfg(key: String) = object : AppConfig { override val defaultExchangeApiKey = key }

    @Test fun userKeyWins() {
        assertThat(resolveApiKey("user", cfg("default"))).isEqualTo("user")
    }
    @Test fun fallsBackToConfig() {
        assertThat(resolveApiKey(null, cfg("default"))).isEqualTo("default")
        assertThat(resolveApiKey("  ", cfg("default"))).isEqualTo("default")
    }
    @Test fun nullWhenNoneAvailable() {
        assertThat(resolveApiKey(null, cfg(""))).isNull()
    }
    @Test fun refreshWhenStaleAndHasKey() {
        assertThat(shouldRefreshRates(now = 100_000, updatedAt = 0, intervalMs = 1000, hasKey = true)).isTrue()
    }
    @Test fun noRefreshWhenFresh() {
        assertThat(shouldRefreshRates(now = 1500, updatedAt = 1000, intervalMs = 1000, hasKey = true)).isFalse()
    }
    @Test fun noRefreshWithoutKey() {
        assertThat(shouldRefreshRates(now = 100_000, updatedAt = 0, intervalMs = 1000, hasKey = false)).isFalse()
    }
}
```

- [ ] **Step 2: Run to verify FAIL**

Run: `./gradlew :app:testDebugUnitTest --offline --tests "*RatesRefreshPolicyTest"`
Expected: FAIL (unresolved references).

- [ ] **Step 3: Implement**

`AppConfig.kt`:
```kotlin
package com.financeapp.core.config

interface AppConfig { val defaultExchangeApiKey: String }
```
`AppConfigModule.kt`:
```kotlin
package com.financeapp.core.config

import com.financeapp.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object AppConfigModule {
    @Provides @Singleton
    fun provideAppConfig(): AppConfig = object : AppConfig {
        override val defaultExchangeApiKey: String = BuildConfig.EXCHANGE_API_KEY
    }
}
```
`RatesRefreshPolicy.kt`:
```kotlin
package com.financeapp.core.domain.usecase

import com.financeapp.core.config.AppConfig

fun resolveApiKey(userKey: String?, config: AppConfig): String? {
    val u = userKey?.trim().orEmpty()
    if (u.isNotEmpty()) return u
    val d = config.defaultExchangeApiKey.trim()
    return d.ifEmpty { null }
}

fun shouldRefreshRates(now: Long, updatedAt: Long, intervalMs: Long, hasKey: Boolean): Boolean =
    hasKey && (now - updatedAt >= intervalMs)
```

- [ ] **Step 4: Run to verify PASS**

Run: `./gradlew :app:testDebugUnitTest --offline --tests "*RatesRefreshPolicyTest"`
Expected: PASS.

- [ ] **Step 5: Commit**
```bash
git add app/src/main/java/com/financeapp/core/config app/src/main/java/com/financeapp/core/domain/usecase/RatesRefreshPolicy.kt app/src/test/java/com/financeapp/core/domain/usecase/RatesRefreshPolicyTest.kt
git commit -m "feat(rates): AppConfig seam + testable key-resolution & refresh policy"
```

### Task 1.2: Settings model — auto-refresh fields + wire key resolution into VM

**Files:**
- Modify: `app/src/main/java/com/financeapp/core/domain/model/AppSettings.kt`
- Modify: `app/src/main/java/com/financeapp/core/data/datastore/SettingsDataStore.kt`
- Modify: `app/src/main/java/com/financeapp/core/data/repository/SettingsRepositoryImpl.kt`
- Modify: `app/src/main/java/com/financeapp/core/domain/repository/SettingsRepository.kt`
- Modify: `app/src/main/java/com/financeapp/feature/settings/SettingsViewModel.kt`

**Interfaces:**
- Produces: `AppSettings.autoRefreshRates: Boolean = true`, `AppSettings.ratesIntervalHours: Int = 12`; repo `setAutoRefreshRates(Boolean)`, `setRatesIntervalHours(Int)`. `SettingsViewModel.refreshRates` resolves key via `resolveApiKey(userKey, appConfig)`.

- [ ] **Step 1: Add fields to `AppSettings`**
```kotlin
val autoRefreshRates: Boolean = true,
val ratesIntervalHours: Int = 12,
```
- [ ] **Step 2: Add DataStore keys + repo read/write**

In `SettingsKeys`: `val AUTO_REFRESH = booleanPreferencesKey("auto_refresh_rates")`, `val RATES_INTERVAL = androidx.datastore.preferences.core.intPreferencesKey("rates_interval_hours")`.
In `SettingsRepositoryImpl.settings.map`: add `autoRefreshRates = p[SettingsKeys.AUTO_REFRESH] ?: def.autoRefreshRates`, `ratesIntervalHours = p[SettingsKeys.RATES_INTERVAL] ?: def.ratesIntervalHours`. Add setters + interface methods.

- [ ] **Step 3: Inject `AppConfig` into `SettingsViewModel`; use `resolveApiKey`**

Constructor: add `private val appConfig: AppConfig`. In `refreshRates`, replace the key read:
```kotlin
val s = settingsRepo.settings.first()
val key = resolveApiKey(s.exchangeApiKey, appConfig)
if (key.isNullOrBlank()) { if (!silent) _ratesEvent.value = RatesEvent.NO_KEY; return@launch }
```
In `init`, gate silent refresh on `s.autoRefreshRates` and use `shouldRefreshRates(now, s.ratesUpdatedAt, s.ratesIntervalHours*3600_000L, hasKey = resolveApiKey(s.exchangeApiKey, appConfig) != null)`.

- [ ] **Step 4: Build + test**

Run: `./gradlew :app:assembleDebug :app:testDebugUnitTest --offline`
Expected: BUILD SUCCESSFUL; existing tests still green.

- [ ] **Step 5: Commit**
```bash
git add app/src/main/java/com/financeapp/core
git commit -m "feat(rates): auto-refresh settings + BuildConfig key fallback in Settings VM"
```

### Task 1.3: Dashboard auto-refresh + rates in state

**Files:**
- Modify: `app/src/main/java/com/financeapp/feature/dashboard/DashboardViewModel.kt`

**Interfaces:**
- Consumes: `resolveApiKey`, `shouldRefreshRates`, `ExchangeRateApi`, `SettingsRepository`, `AppConfig`.
- Produces: `DashboardViewModel.rates: StateFlow<RatesUi>` where `data class RatesUi(val usd: Double, val eur: Double, val updatedAt: Long)`.

- [ ] **Step 1:** Add `RatesUi` + a `rates` StateFlow derived from `settingsRepo.settings` (map to usd/eur/updatedAt).
- [ ] **Step 2:** In `init`, on first settings emission, if `autoRefreshRates` and `shouldRefreshRates(...)`, call `exchangeApi.fetchRates(resolvedKey)` in `viewModelScope`; on success `settingsRepo.setRates(...)`. Silent (no events). Guard with a one-shot `AtomicBoolean`/flag so it runs once per VM.
- [ ] **Step 3: Build + test**

Run: `./gradlew :app:assembleDebug :app:testDebugUnitTest --offline` → SUCCESSFUL.

- [ ] **Step 4: Commit**
```bash
git add app/src/main/java/com/financeapp/feature/dashboard/DashboardViewModel.kt
git commit -m "feat(rates): silent auto-refresh on Home open"
```

### Task 1.4: Home rates block (minimal, Quiet Minimal)

**Files:**
- Modify: `app/src/main/java/com/financeapp/feature/dashboard/DashboardScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`, `app/src/main/res/values-en/strings.xml`

**Interfaces:**
- Consumes: `DashboardViewModel.rates`.

- [ ] **Step 1:** Add strings: `dash_rates` ("Курс"/"Rate"), `dash_rates_updated` ("обновлено %1$s"/"updated %1$s"), `dash_rates_never` ("—").
- [ ] **Step 2:** Add a private `RatesGlance(rates, currency)` composable: a hairline block under `Masthead`. Row of `USD <v> · EUR <v>` in `titleSmall` serif (`FrauncesTitle`), tiny `Eyebrow` above with relative-time label (reuse a small `relativeTime(updatedAt)` helper — add to `core/utils`). Hidden when `usd<=0 && eur<=0`. Insert into the Dashboard `Column` between `Masthead` and the reminders `Spacer`.
- [ ] **Step 3: Build**

Run: `./gradlew :app:assembleDebug --offline` → SUCCESSFUL. Visually confirm via code review (no emulator).

- [ ] **Step 4: Commit**
```bash
git add app/src/main/java/com/financeapp/feature/dashboard/DashboardScreen.kt app/src/main/res
git commit -m "feat(home): minimal live currency-rates glance under the balance"
```

### Task 1.5: Settings — API-key hint + collapse manual rates

**Files:**
- Modify: `app/src/main/java/com/financeapp/feature/settings/SettingsScreen.kt`
- Modify: both `strings.xml`

- [ ] **Step 1:** Add `autoRefreshRates` Switch row + interval `PillSelector` (12ч/24ч; label "Ручное" maps to a large interval / disabled auto) in the Finance group. Wire to VM setters from Task 1.2.
- [ ] **Step 2:** Wrap the two manual `RateField`s in a collapsible "Ручная правка курса" disclosure (a clickable `Eyebrow`/row toggling a `remember { mutableStateOf(false) }`); collapsed by default. Update `set_api_key_hint` to mention a built-in default is used when empty.
- [ ] **Step 3: Build** → SUCCESSFUL.
- [ ] **Step 4: Commit**
```bash
git commit -am "feat(settings): auto-refresh toggle + interval; collapse manual rate override"
```

---

## Phase 2 — ⚙️ Settings expansion

### Task 2.1: New settings fields (model/datastore/repo)

**Files:**
- Modify: `AppSettings.kt`, `SettingsDataStore.kt`, `SettingsRepositoryImpl.kt`, `SettingsRepository.kt`
- Modify: `Enums.kt` (new `ColorScheme` entries + `AutoLock` enum)
- Test: `app/src/test/java/com/financeapp/core/data/repository/SettingsRepositoryImplTest.kt` (extend if present, else create)

**Interfaces:**
- Produces new `AppSettings` fields with defaults:
  - `showDecimals: Boolean = true`
  - `defaultTxType: TransactionType = TransactionType.EXPENSE`
  - `animationsEnabled: Boolean = true`
  - `hapticsEnabled: Boolean = true`
  - `hideBalanceByDefault: Boolean = false`
  - `requirePinOnLaunch: Boolean = true`
  - `autoBiometric: Boolean = true`
  - `autoLock: AutoLock = AutoLock.IMMEDIATE`
  - `defaultReminderHour: Int = 9`, `defaultReminderLeadDays: Int = 1`
  - `ColorScheme` gains `INDIGO`, `TERRACOTTA`
  - `enum class AutoLock { IMMEDIATE, ONE_MIN, FIVE_MIN }`
- Repo gains a setter per field.

- [ ] **Step 1: Write failing round-trip test** (Robolectric, real DataStore in tmp) asserting a couple of new defaults + a set/read round-trip for `showDecimals` and `autoBiometric`. (Model on existing settings repo test if one exists; otherwise a minimal Robolectric test.)
- [ ] **Step 2:** Run → FAIL.
- [ ] **Step 3:** Add fields to `AppSettings`, keys to `SettingsKeys`, read mappings + setters in impl + interface methods, enums to `Enums.kt`.
- [ ] **Step 4:** Run `./gradlew :app:testDebugUnitTest --offline` → PASS.
- [ ] **Step 5: Commit**
```bash
git commit -am "feat(settings): model+repo for decimals, defaults, haptics, security, auto-lock, accents"
```

### Task 2.2: Accent schemes — fix labels + wire 2 new accents in theme

**Files:**
- Modify: `app/src/main/java/com/financeapp/core/ui/theme/Color.kt`
- Modify: `app/src/main/java/com/financeapp/core/ui/theme/Theme.kt`
- Modify: both `strings.xml`

- [ ] **Step 1:** Add color constants for INDIGO + TERRACOTTA accents (light+dark variants) to `Color.kt`, matching the existing accent-endpoint pattern.
- [ ] **Step 2:** Extend `LocalAccentColors`/scheme mapping in `Theme.kt` to cover all four `ColorScheme` values.
- [ ] **Step 3:** Rename swatch strings `set_scheme_purple`→ value "Зелёный"/"Green", `set_scheme_orange`→"Янтарный"/"Amber"; add `set_scheme_indigo`, `set_scheme_terracotta`.
- [ ] **Step 4: Build** → SUCCESSFUL.
- [ ] **Step 5: Commit**
```bash
git commit -am "feat(theme): correct accent labels + add indigo & terracotta accents"
```

### Task 2.3: Settings UI — new groups & controls

**Files:**
- Modify: `SettingsScreen.kt`, `SettingsViewModel.kt`, both `strings.xml`
- Modify: `app/src/main/java/com/financeapp/core/backup/*` (include portable new prefs)

- [ ] **Step 1:** VM: add setters delegating to repo for every Task 2.1 field.
- [ ] **Step 2:** UI: 4 accent `SchemeSwatch`es (green/amber/indigo/terracotta); Animations + Haptics + Hide-balance switches (Appearance/Feedback); Show-decimals switch + default-tx-type pill (Finance); default reminder hour + lead-days (Reminders group — a compact stepper/pill); Require-PIN + Auto-biometric + Auto-lock pill (Security); "Clear all data" destructive `ActionRow` with confirm dialog (Data); About row with `BuildConfig.VERSION_NAME`.
- [ ] **Step 3:** Backup: extend `SettingsBackup` with the new *portable* fields (exclude PIN/biometric/api key/require-pin/auto-biometric). Keep serialization backward-compatible (nullable/defaulted).
- [ ] **Step 4: Build + test** → SUCCESSFUL & green.
- [ ] **Step 5: Commit**
```bash
git commit -am "feat(settings): expanded controls — accents, motion/haptics, decimals, security, clear-data, about"
```

### Task 2.4: Honor new settings across the app

**Files:**
- Modify: `core/utils/CurrencyFormatter.kt` (+ its test) for `showDecimals`
- Modify: `core/ui/anim/reducedMotion()` source to also read `animationsEnabled`
- Modify: `core/utils` haptics to read `hapticsEnabled`
- Modify: Dashboard to read `hideBalanceByDefault`; Add-sheet to read `defaultTxType`; reminders default hour/lead-days

- [ ] **Step 1: Write failing formatter test** for decimals off:
```kotlin
@Test fun hidesDecimalsWhenDisabled() {
    assertThat(CurrencyFormatter.format(1234.0, Currency.RUB, showDecimals = false)).doesNotContain(",00")
}
```
- [ ] **Step 2:** Run → FAIL.
- [ ] **Step 3:** Add `showDecimals: Boolean = true` param to `CurrencyFormatter.format`; thread the setting from callers (default true keeps current behavior). Wire `animationsEnabled`→`reducedMotion`, `hapticsEnabled`→haptics, `hideBalanceByDefault`→Dashboard initial state, `defaultTxType`→Add sheet preset.
- [ ] **Step 4:** Run tests → PASS; build → SUCCESSFUL.
- [ ] **Step 5: Commit**
```bash
git commit -am "feat(settings): apply decimals/motion/haptics/defaults across app"
```

---

## Phase 3 — 🎞 Motion & FPS

### Task 3.1: Consolidate Motion tokens + shared transitions

**Files:**
- Modify: `app/src/main/java/com/financeapp/core/ui/anim/Motion.kt` (+ `PressScale.kt`)

- [ ] **Step 1:** Ensure a single source of durations/easings + a spec for spring press and a shared `enterFadeSlide` used by `Staggered`/`Reveal`. Expose `Motion.springPress` spec. Keep reduced-motion checks.
- [ ] **Step 2:** Replace ad-hoc `tween`s in `Staggered`/`Reveal` (Dashboard/Settings) with the shared spec.
- [ ] **Step 3: Build** → SUCCESSFUL.
- [ ] **Step 4: Commit** `git commit -am "refactor(motion): unify tokens + shared enter transition"`

### Task 3.2: FPS pass on lists & Canvas

**Files:**
- Modify: list-bearing screens (`TransactionsScreen.kt`, `DashboardScreen.kt`, `CategoriesScreen.kt`, `RemindersScreen.kt`), `feature/analytics/*` Canvas.

- [ ] **Step 1:** Ensure `LazyColumn` items use stable `key = { it.id }`; hoist lambdas; wrap computed values in `remember`/`derivedStateOf`. Convert offset/scale/alpha animations to `Modifier.graphicsLayer`.
- [ ] **Step 2:** Analytics Canvas: cache `Path`/brush with `remember`/`drawWithCache` keyed by data.
- [ ] **Step 3: Build release for perf sanity**

Run: `./gradlew :app:assembleRelease --offline` → SUCCESSFUL (no crash; perf validated on-device separately).

- [ ] **Step 4: Commit** `git commit -am "perf: stable keys, graphicsLayer transforms, cached analytics canvas"`

---

## Phase 4 — 🚪 First-launch screens

### Task 4.1: Animated PIN dots

**Files:**
- Modify: `app/src/main/java/com/financeapp/core/ui/components/PinDots.kt`

- [ ] **Step 1:** Per-dot `animateFloatAsState` scale (filled → 1.0 with spring overshoot pop, empty → 0.86) + animated color between outline/accent; empty = hairline ring, filled = accent disc. Add a brief error-color flash driven by `error`. Respect `reducedMotion()`.
- [ ] **Step 2: Build** → SUCCESSFUL.
- [ ] **Step 3: Commit** `git commit -am "feat(lock): spring-pop animated PIN dots + error flash"`

### Task 4.2: Auto-biometric on Lock

**Files:**
- Modify: `app/src/main/java/com/financeapp/feature/lock/LockScreen.kt`
- Consumes: `AppSettings.autoBiometric` (via a small VM exposure or passed arg).

- [ ] **Step 1:** Pass `autoBiometric: Boolean` into `LockScreen` (from the caller that already passes `biometricEnabled`). Add a one-shot `LaunchedEffect(Unit)` that, when `biometricEnabled && autoBiometric && !locked && canAuthenticate == SUCCESS`, calls `showBiometricPrompt(context, onUnlocked)` exactly once (guard with a `rememberSaveable` flag so cancel doesn't loop).
- [ ] **Step 2: Build** → SUCCESSFUL.
- [ ] **Step 3: Commit** `git commit -am "feat(lock): auto-present biometric prompt on entry when enabled"`

### Task 4.3: Splash & Onboarding polish

**Files:**
- Modify: `feature/splash/SplashScreen.kt`, `feature/onboarding/OnboardingScreen.kt`, both `strings.xml`

- [ ] **Step 1:** Splash: serif wordmark + thin accent rule + eyebrow tagline; soft fade/scale-in via `graphicsLayer` (reduced-motion aware).
- [ ] **Step 2:** Onboarding: serif step headings, generous spacing, flat accent primary buttons, `HorizontalPager`-style smooth transitions with progress dots (use existing pager if present; else animate an index). No new deps.
- [ ] **Step 3: Build** → SUCCESSFUL.
- [ ] **Step 4: Commit** `git commit -am "redesign(first-run): splash wordmark + airy animated onboarding"`

---

## Phase 5 — 🧹 Screen-by-screen visual sweep

### Task 5.1..5.N: Polish pass in nav order

**Files (one commit per screen):** `DashboardScreen.kt`, `TransactionsScreen.kt`, `BudgetsScreen.kt`, `AnalyticsScreen.kt`, `SettingsScreen.kt` (More hub), `CategoriesScreen.kt`, `RemindersScreen.kt`.

For each screen:
- [ ] Tighten spacing/hierarchy; ensure hairline-list consistency; verify empty states use `EmptyState`; apply shared motion; confirm RU/EN strings; confirm `elevation=0` flat rows.
- [ ] Build: `./gradlew :app:assembleDebug --offline` → SUCCESSFUL.
- [ ] Commit: `git commit -am "redesign(sweep): polish <screen>"`

Record any per-screen decision inline in the spec's Stream 5 section.

---

## Final verification

- [ ] `source /f/android-dev/env.sh && ./gradlew :app:assembleDebug :app:assembleRelease :app:testDebugUnitTest --offline` → all SUCCESSFUL, tests green (≥36).
- [ ] Confirm `app/build/outputs/apk/debug/estate.apk` exists.
- [ ] Update build-env memory: BuildConfig key location + new settings surface.

## Self-Review (done)

- **Spec coverage:** Stream 1→Phase 1 + Task 0.1; Stream 2→Phase 2; Stream 3→Phase 3; Stream 4 (incl. PIN anim + auto-biometric)→Phase 4; Stream 5→Phase 5; APK rename→Task 0.1. All covered.
- **Placeholders:** none (`<KEY>` is a deliberate secret redaction; real value goes only to gitignored `local.properties`).
- **Type consistency:** `resolveApiKey`/`shouldRefreshRates`/`AppConfig.defaultExchangeApiKey`/`RatesUi`/`AutoLock` used consistently across tasks.
