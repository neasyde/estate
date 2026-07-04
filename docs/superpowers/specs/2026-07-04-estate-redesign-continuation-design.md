# estate — Redesign Continuation (design / decisions log)

Continuation of the completed "Quiet Minimal" (Warm Serif) redesign
(`2026-07-03-estate-ui-redesign.md`). Baseline: flat warm palette, Fraunces serif
for amounts/titles, Inter for UI, monochrome rounded icons, hairline lists, deep-green
accent (default) / amber alt, dark theme, 5-tab nav (Главная · История · Бюджеты ·
Анализ · Ещё) + flat accent "+" FAB. Build on F: toolchain; verify = compile + unit
tests + assembleDebug. 36 unit tests currently green.

## Goal

Continue and deepen the redesign: elevate the currency feature onto Home, make rates
work out-of-the-box, expand Settings for far more control, smooth animations app-wide
without losing FPS, redesign the first-launch screens, and do a full screen-by-screen
visual sweep.

## Strategy — Approach A (foundation → sweep), approved

Front-load functional wins, then shared motion/perf, then first screens, then the full
visual pass so every screen benefits from the improved motion. Small commits per stream;
`build + test` after each. Streams are ordered but each ships independently.

1. Currency, elevated (functional + Home block)
2. Settings expansion (more control)
3. Motion & FPS pass (shared foundation)
4. First-launch screens (Splash / Onboarding / Lock) — incl. PIN-dot animation & auto-biometric
5. Screen-by-screen visual sweep

Everything keeps the Quiet Minimal language (flat, warm, serif amounts, hairlines,
monochrome icons). No gradients/shadows are reintroduced.

---

## Stream 1 — 💱 Currency: elevated + auto-refresh

### API key (out-of-the-box)
- Key lives in `local.properties` (gitignored) as `EXCHANGE_API_KEY=...`; exposed via
  `buildConfigField "String", "EXCHANGE_API_KEY", ...` (enable `buildFeatures.buildConfig`).
- Resolution order in the rates use-case / VM: **user-entered key in Settings first**,
  else fall back to `BuildConfig.EXCHANGE_API_KEY`. So rates work on a fresh install with
  no setup, but a user can still override with their own key.
- The user's key value is recorded out-of-band in build-env memory, NOT in this spec or
  any committed source.

### Build output naming
- Rename the **debug** APK output to `estate.apk` via `applicationVariants.all { ...
  outputFileName = "estate.apk" }` (or the AGP `outputs` API) scoped to the debug variant,
  so `:app:assembleDebug` produces `app/build/outputs/apk/debug/estate.apk`.

### Auto-refresh policy
- On app open (dashboard first composition), if `ratesUpdatedAt` is older than the
  configured interval (default 12h) AND connectivity is available AND a key resolves,
  fetch silently in the background. No blocking spinner, no dialog on success.
- Free exchangerate-api plan refreshes ~once/day, so 12h default is safe; interval is a
  Setting (see Stream 2). Failures are silent on Home (last-known rates stay); the manual
  "Refresh" button in Settings keeps its explicit success/error dialog.
- Reuse existing `ExchangeRateApi` + settings fields (`rateUsd`, `rateEur`,
  `ratesUpdatedAt`, `exchangeApiKey`). Add an `autoRefreshRates` bool + `ratesIntervalHours`.

### Home rates block (minimal)
- A thin hairline block under the masthead on Dashboard: `USD 91,20 · EUR 98,40`
  (values from stored rates, formatted). Tiny eyebrow above: `Курс · обновлено 2 ч назад`
  (relative time; "—" when never). Flat, no card/shadow — pure Quiet Minimal.
- Hidden entirely if no rates are known yet and none can be fetched.
- Optional (nice-to-have, low priority): tap opens a small converter bottom sheet
  (amount × currency → base). Ships only if it stays simple; otherwise deferred.

### Settings de-emphasis
- Manual USD/EUR rate fields remain as a **fallback**, collapsed under a
  "Ручная правка курса" disclosure (expandable), no longer the primary control.

---

## Stream 2 — ⚙️ Settings: much more control

Keep the flat grouped-hairline sections. Fix, then expand. New/changed controls:

**Appearance**
- Theme mode (system/light/dark) — exists.
- **Fix accent swatches:** currently mislabeled "Purple/Orange" while the colors are
  green/amber. Rename to correct labels and add **2 more accent options** (e.g. indigo,
  terracotta) → 4 accents total. (Color.kt constant names may stay; only labels/entries
  change to limit churn — document any new `ColorScheme` entries.)
- **Animations** toggle — off ⇒ app-wide reduced motion (feeds `reducedMotion()`).

**Finance**
- Base currency — exists.
- **Auto-refresh rates** toggle + **interval** selector (12h / 24h / manual).
- API key field — exists (now optional; hint updated to say a default is built in).
- **Manual rate override** — collapsed disclosure (from Stream 1).
- **Show kopecks/decimals** toggle (hide the `,00` when off).
- **Default transaction type** for the "+" sheet (Расход / Доход).

**Reminders**
- **Default reminder time** (time-of-day) and **default lead days**.

**Security**
- **Require PIN on launch** (app-lock) toggle.
- Biometric enabled — exists.
- **Auto-biometric on launch** toggle (controls Stream 4 behavior; default ON when
  biometric is enabled).
- **Auto-lock** timeout (immediately / after 1 min / after 5 min in background).
- Change PIN — exists.

**Feedback**
- **Haptics** toggle (feeds `rememberHaptics`).

**Data**
- Backup / Restore — exists.
- **Clear all data** (destructive, confirm dialog).

**About**
- App name + version line (read from `BuildConfig`).

Each new setting: add field to the settings model + repository (DataStore) with a safe
default, wire the VM, add strings for RU + EN. Backup schema: include new *portable*
prefs (exclude security-sensitive ones, matching the existing PIN/biometric exclusion).

---

## Stream 3 — 🎞 Motion & FPS

### Smoother motion (shared)
- Consolidate `Motion` tokens: unify durations/easings; gentle spring on press,
  consistent fade + slide enter for lists and sections, one shared tab-change transition.
- All new animations honor `reducedMotion()` (now also driven by the Animations setting).

### FPS without losing quality
- Lists: stable `key`s, hoist/stabilize lambdas, `derivedStateOf` for computed values,
  no allocation/heavy work in composition.
- Flat list rows keep `elevation = 0` (avoids scroll jank — existing SoftDepth note).
- Analytics Canvas: use `drawWithCache` / cache paths & brushes across recompositions.
- Prefer `Modifier.graphicsLayer` for transform animations (offset/scale/alpha) to keep
  work off the layout pass.
- Measure on a **release** build (debug FPS is inherently janky).

---

## Stream 4 — 🚪 First-launch screens

### Splash
- Serif "estate" wordmark on warm paper, thin accent rule + eyebrow tagline; soft
  fade/scale-in (honors reduced motion). No logo asset needed.

### Onboarding
- Serif step headings, generous air, flat accent primary buttons, smooth page
  transitions with progress dots. Collect base currency (and optionally set PIN) here.

### Lock (PIN) — incl. the two additions
- **PIN-dot animation:** each dot springs in as its digit is entered — scale-pop +
  smooth color/size transition (empty = hairline ring → filled = accent disc). Implement
  in `PinDots.kt` with a per-dot `Animatable`/`animateFloatAsState`. Strengthen the
  existing error **shake**; brief error-color flash on the dots.
- **Auto-biometric on launch:** when biometric is enabled, available
  (`BiometricManager.canAuthenticate(BIOMETRIC_WEAK) == SUCCESS`), the app is not in a
  lock-out window, and the **Auto-biometric** setting is ON — automatically present the
  prompt once on Lock appearance (guarded by a one-shot flag so it doesn't re-trigger on
  recomposition or after a user cancel). Manual fingerprint key stays as fallback.
- Minimal PIN layout, serif wordmark, warm paper — unchanged structure.

---

## Stream 5 — 🧹 Screen-by-screen visual sweep

Systematic pass in nav order: **Главная → История → Бюджеты → Анализ → Ещё**
(+ Категории, Напоминания, Настройки). Per screen: tighten hierarchy, spacing, and empty
states; apply the improved motion; keep hairline-list consistency; verify RU/EN strings.
No structural rework — polish only. Note any per-screen decisions inline here as they're made.

---

## Testing & verification

- After each stream: `source /f/android-dev/env.sh && ./gradlew :app:assembleDebug
  :app:testDebugUnitTest --offline`; keep 36+ unit tests green.
- Unit-test the pure logic where it exists: rates resolution/interval decision, settings
  defaults/serialization, currency formatting with the decimals toggle. UI/motion verified
  by compile + assembleDebug (no emulator/adb here; release build for real FPS).
- Small commits per stream, conventional-commit messages, all on `master` (matching the
  project's recent history).

## Out of scope / deferred

- Full Lottie animation assets (still deferred).
- PIN hardening beyond current SHA-256 (separate security task).
- On-device/emulator verification (not available in this environment).
