# estate — Feature Batch 2 (design / decisions log)

Follow-on to the redesign-continuation work. Baseline: Quiet Minimal, DB version 3,
build on F: toolchain (`source /f/android-dev/env.sh && ./gradlew … --offline`).

## Goal

Eleven concrete improvements: consistent Home FAB, swipeable Home transactions, exact
transaction time, live-rate onboarding prefill, draggable category reorder, remove the
dead color picker, curated icons + Russian icon search, reworked analytics periods,
clear-all that resets to default categories, and renaming the 5th tab to Settings.

## Decisions (locked)

- **Analytics:** period buttons **Week / Month / Year / All** + a **rolling↔calendar
  toggle**. Rolling is the default (last 7 / 30 / 365 days); calendar = this week / month
  / year. **All** is always all-time (toggle hidden).
- **Icons:** do BOTH — curate the pickable list AND add Russian search.
- **Clear-all:** wipe every table, then **re-seed the default categories**
  (`DatabaseSeed.categories()`) → fresh-install state.

---

## Stream A — Home & transaction interaction

### A1. Dashboard "+" FAB consistency
`DashboardScreen` currently uses a custom flat `FloatingActionButton` (elevation 0, primary
container). Replace with the standard `FloatingActionButton(onClick = …) { Icon(add) }` —
identical to `TransactionsScreen`/`BudgetsScreen`. Keep bottom-end placement.

### A2. Swipe on Home
`TransactionsScreen` already has a private `SwipeRow` (`SwipeToDismissBox`: EndToStart =
delete, StartToEnd = duplicate; tap = edit). Extract it into a shared
`core/ui/components/SwipeTransactionRow.kt` taking `(item, onEdit, onDelete, onDuplicate)`,
and use it in both `TransactionsScreen` and the Dashboard recent list. Dashboard wires
`onEdit` → open edit sheet, `onDelete` → `vm.deleteTransaction`, `onDuplicate` →
`vm.duplicateTransaction`. Add those methods to `DashboardViewModel` (mirroring the
Transactions VM; reuse existing use-cases).

### A3. Exact transaction time
Add a time picker to `AddEditTransactionSheet` (pattern from `ReminderSheet`: M3
`TimePicker` in an `AlertDialog`). Show a Date button + Time button row. The form keeps a
single `date: Long` epoch-millis timestamp; `setDate` preserves the existing time-of-day,
and a new `setTime(hour, minute)` sets it. History/rows may show the time next to the date
(optional, low priority). `AddEditTransactionViewModel` gains `setTime`.

---

## Stream B — Onboarding live-rate prefill

On the onboarding **currency** step, prefill the USD/EUR rate fields with live rates
instead of the hardcoded "90"/"100". `OnboardingViewModel` fetches once (on init or first
entry to the step) via `ExchangeRateApi.fetchRates(resolveApiKey(null, appConfig))` using
the built-in BuildConfig key; on `Success` it sets `rateUsd`/`rateEur` **only if the user
hasn't edited them**; on failure it leaves the current defaults. Inject `ExchangeRateApi` +
`AppConfig` into the VM. No blocking spinner required.

---

## Stream C — Categories

### C1. `sortOrder` + migration (data)
Add `sortOrder: Int = 0` to `CategoryEntity`, `Category` (domain), and the mapper.
`MIGRATION_3_4` (DB version → 4): `ALTER TABLE categories ADD COLUMN sortOrder INTEGER NOT
NULL DEFAULT 0`, then `UPDATE categories SET sortOrder = id` to seed a stable initial order.
`CategoryDao.observeAll`/managed queries order by `sortOrder, id`. `DatabaseSeed` assigns
increasing `sortOrder` to presets. Add `CategoryRepository.reorder(orderedIds: List<Long>)`
→ writes `sortOrder` = index (single transaction).

### C2. Draggable reorder (UI)
The manage-categories `LazyColumn` becomes reorderable via long-press drag implemented with
`Modifier.pointerInput { detectDragGesturesAfterLongPress(...) }` + item offset animation +
a drag handle affordance — no new dependency. On drop, call `vm.reorder(newOrder)` which
persists. Reduced-motion aware.

### C3. Remove color picker
Delete the color `FlowRow` + `Eyebrow(cat_field_color)` from `AddEditCategorySheet` and the
`setColor` usage from the form UI. Keep `CategoryEntity.color` (data/backup compatibility);
new categories default `color` to the accent (or existing preset). Selected icon tile uses
`MaterialTheme.colorScheme.primary` instead of `form.color`.

### C4. Curate icons
Trim `pickableIcons` (`MaterialIconMap.kt`) from ~45 to a clean ~28–32 finance-relevant set
(drop odd/rarely-useful entries: e.g. `sports_soccer`, `park`, `spa`, `cake`, `celebration`,
`water_drop`, `pets` if not wanted — final list chosen in implementation, keeping food,
transport, home, bills, health, shopping, entertainment, salary, gifts, savings, etc.).

### C5. Russian icon search
Add an icon→RU-keywords map (e.g. `restaurant` → "еда ресторан кафе"). The picker filter
matches if the query (lowercased, trimmed) is contained in the icon name OR any RU keyword.
English substring matching still works.

---

## Stream D — Analytics periods

`AnalyticsPeriod` → `enum { WEEK, MONTH, YEAR, ALL }`. `GetAnalyticsDataUseCase.invoke` gains
a `rolling: Boolean` param. Start of range:

| period | rolling (default) | calendar |
|--------|-------------------|----------|
| WEEK | now − 7d | `DateUtils.startOfWeek(now)` (new) |
| MONTH | now − 30d | `startOfMonth(now)` |
| YEAR | now − 365d | `startOfYear(now)` |
| ALL | `Long.MIN_VALUE` | `Long.MIN_VALUE` (toggle hidden) |

Add `DateUtils.startOfWeek` (Monday, local). `AnalyticsViewModel` holds `period` +
`rolling`; UI adds a Week text-tab and a compact rolling/calendar toggle ("Последние N дней"
/ "Этот период"), hidden when `ALL`. Trend bars (last 6 months) unchanged. New RU/EN strings.

---

## Stream E — Clear-all resets to defaults

`BackupManager.clearAllData()`: after `db.clearAllTables()`, re-insert
`DatabaseSeed.categories()` so default categories return. Success event unchanged.

---

## Stream F — 5th tab → Settings

`BottomBar`: change the item icon `more_horiz` → `settings` and the label. `nav_more` value
becomes "Настройки" / "Settings" (this also retitles the hub header, which reads
`nav_more`). Route stays `SETTINGS`.

---

## Testing

- Unit: analytics start-of-range for every (period × rolling) incl. ALL; `DateUtils.startOfWeek`;
  category reorder persistence (fake repo → sortOrder = index); clear-all re-seeds defaults
  (Robolectric on real DB); RU icon-search matching.
- Build: `./gradlew :app:assembleDebug :app:testDebugUnitTest --offline` after each stream;
  keep the suite green (currently 45). Release build at the end.
- No on-device verification available (compile + tests + assemble only).

## Sequencing

C1 (data/migration) → C2–C5 (categories UI) → D (analytics) → A (home FAB/swipe/time) →
B (onboarding rates) → E (clear-all) → F (tab rename). Small commits per stream on a feature
branch; merge to `master` at the end.

## Out of scope

- On-device/emulator verification. Reordering library (using manual drag). PIN hardening.
