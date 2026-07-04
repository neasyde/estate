# estate Feature Batch 2 — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Ship 11 improvements: consistent Home FAB, swipeable Home rows, exact transaction time, live-rate onboarding prefill, draggable category reorder, remove dead color picker, curated + RU-searchable icons, reworked analytics periods, clear-all→reset-to-defaults, and 5th-tab→Settings.

**Architecture:** Same layering. Schema change (category `sortOrder`) via Room `MIGRATION_3_4`. Testable logic (analytics start-of-range, `startOfWeek`, reorder, RU icon search) as pure functions/repo methods; UI in `feature/*`.

**Tech Stack:** Kotlin, Compose M3, Hilt, Room, DataStore, JUnit/Robolectric/Truth. Build F: toolchain, `--offline`.

## Global Constraints
- Build: `source /f/android-dev/env.sh && ./gradlew … --offline`. No new dependencies. Quiet Minimal language. New user strings in BOTH `values/` (EN) and `values-ru/` (RU). Small commits per task on a feature branch; keep suite green (currently 45).

---

## Task C1: Category `sortOrder` + migration
**Files:** `core/data/local/entity/CategoryEntity.kt`, `core/domain/model/Category.kt`, `core/data/mapper/*` (category mapper), `core/data/local/FinanceDatabase.kt`, `core/data/local/DatabaseSeed.kt`, `core/data/local/dao/CategoryDao.kt`, `core/domain/repository/CategoryRepository.kt`, `core/data/repository/CategoryRepositoryImpl.kt`, test `Fakes.kt`.
**Produces:** `CategoryEntity.sortOrder: Int`, `Category.sortOrder: Int`, `CategoryRepository.reorder(orderedIds: List<Long>)`, DB version 4 + `MIGRATION_3_4`.

- [ ] Add `val sortOrder: Int = 0` to `CategoryEntity` and `Category`; update the category mapper both directions.
- [ ] Bump `@Database(version = 4)`; add `MIGRATION_3_4` (`ALTER TABLE categories ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0`; then `UPDATE categories SET sortOrder = id`); register in `.addMigrations(...)`.
- [ ] `DatabaseSeed.categories()` assigns increasing `sortOrder` (index).
- [ ] `CategoryDao`: order `observeAll`/managed queries by `ORDER BY sortOrder, id`. Add `@Query("UPDATE categories SET sortOrder = :order WHERE id = :id") suspend fun setOrder(id: Long, order: Int)`.
- [ ] `CategoryRepository`/impl: `suspend fun reorder(orderedIds: List<Long>)` → loop `setOrder(id, index)`. Add to `FakeCategoryRepository` (record order).
- [ ] Build + test: `./gradlew :app:assembleDebug :app:testDebugUnitTest --offline` → green.
- [ ] Commit: `feat(categories): sortOrder column + reorder repo (migration v3→v4)`

## Task C2: Draggable reorder UI
**Files:** `feature/categories/CategoriesScreen.kt`, `feature/categories/CategoriesViewModel.kt`.
**Consumes:** `CategoryRepository.reorder`.

- [ ] VM: `fun reorder(ids: List<Long>) = viewModelScope.launch { repo.reorder(ids) }`.
- [ ] In the manage `LazyColumn`, keep a local mutable `remember` copy of the list; add long-press drag via `Modifier.pointerInput { detectDragGesturesAfterLongPress(onDrag=…, onDragEnd=…) }` swapping items by index (offset the dragged item with `graphicsLayer`), and on drag end call `vm.reorder(current.map{it.id})`. Add a `drag_handle`/`more_horiz`-style handle icon. Reduced-motion aware.
- [ ] Build → green. Commit: `feat(categories): long-press drag to reorder`

## Task C3: Remove color picker
**Files:** `feature/categories/AddEditCategorySheet.kt`, `feature/categories/AddEditCategoryViewModel.kt` (only if it forces a color choice), `values*/strings.xml` (drop `cat_field_color` usage; keep string).
- [ ] Delete the `Eyebrow(cat_field_color)` + color `FlowRow` block. Selected icon tile background → `MaterialTheme.colorScheme.primary` (was `selectedColor`). New categories keep a default `color` (accent argb) in the VM.
- [ ] Build → green. Commit: `feat(categories): remove non-functional color picker`

## Task C4: Curate icons
**Files:** `core/ui/icons/MaterialIconMap.kt` (`pickableIcons`).
- [ ] Trim `pickableIcons` to a clean finance set (~30): keep food/transport/home/bills/shopping/health/entertainment/salary/gifts/savings/work/education/travel/phone/wifi/gas/car/bus/train/coffee/fitness/music/movie/pets/wallet/card/bank/build/star/more_horiz. Drop the rest.
- [ ] Build → green. Commit: `feat(categories): curate icon picker set`

## Task C5: Russian icon search
**Files:** `core/ui/icons/IconSearch.kt` (new), `feature/categories/AddEditCategorySheet.kt`, test `IconSearchTest.kt`.
**Produces:** `fun iconMatches(name: String, query: String): Boolean` + `val iconKeywordsRu: Map<String,String>`.
- [ ] Test: `iconMatches("restaurant", "еда")` == true; `iconMatches("directions_car", "машина")` == true; `iconMatches("restaurant", "food")` == true (name substring); blank query == true.
- [ ] Implement `iconKeywordsRu` (RU synonyms per curated icon) + `iconMatches(name, q)` = q blank OR name contains q OR keywords[name] contains q (all lowercased/trimmed).
- [ ] Sheet: replace `it.contains(query,…)` filter with `iconMatches(it, form.iconQuery)`.
- [ ] Test + build → green. Commit: `feat(categories): Russian icon search`

## Task D1: Analytics period start logic (testable)
**Files:** `core/utils/DateUtils.kt`, `core/domain/model/AnalyticsData.kt` (enum), `core/domain/usecase/GetAnalyticsDataUseCase.kt`, tests `DateUtilsTest.kt` + new `AnalyticsRangeTest.kt`.
**Produces:** `enum AnalyticsPeriod { WEEK, MONTH, YEAR, ALL }`, `DateUtils.startOfWeek(now): Long`, `fun analyticsStart(now, period, rolling): Long`.
- [ ] Test `startOfWeek` returns Monday 00:00 local for a known instant; test `analyticsStart` for each period×rolling (WEEK rolling = now-7d; MONTH calendar = startOfMonth; ALL = Long.MIN_VALUE both modes).
- [ ] Add `AnalyticsPeriod.WEEK`; add `DateUtils.startOfWeek`; add top-level `analyticsStart(now,period,rolling)` (in the use-case file or DateUtils). Use it in `invoke(now, period, rolling)`.
- [ ] Update `GetAnalyticsDataUseCase.invoke` signature to `(now, period, rolling)`.
- [ ] Test + build → green. Commit: `feat(analytics): week period + rolling/calendar start ranges`

## Task D2: Analytics VM + UI
**Files:** `feature/analytics/AnalyticsViewModel.kt`, `feature/analytics/AnalyticsScreen.kt`, `values*/strings.xml`.
- [ ] VM: add `rolling: StateFlow<Boolean>` (default true) + `setRolling`; combine period+rolling into the use-case call. Add `setPeriod`.
- [ ] UI: add a **Неделя** text-tab (period selector now 4). Add a compact rolling/calendar toggle (two-pill `PillSelector`-style: "Последние N дней" / "Этот период"), hidden when period == ALL. New strings: `an_period_week`, `an_rolling`, `an_calendar`.
- [ ] Build → green. Commit: `feat(analytics): week tab + rolling/calendar toggle`

## Task A1: Dashboard FAB consistency
**Files:** `feature/dashboard/DashboardScreen.kt`.
- [ ] Replace the custom `FloatingActionButton` (elevation 0/custom colors) with the standard `FloatingActionButton(onClick = { sheetOpen = true }) { Icon(materialIcon("add"), null) }`, keeping `Modifier.align(BottomEnd).padding(20.dp)`.
- [ ] Build → green. Commit: `feat(home): standard FAB matching other screens`

## Task A2: Shared swipe row + Home swipe
**Files:** `core/ui/components/SwipeTransactionRow.kt` (new, extracted from `TransactionsScreen`), `feature/transactions/TransactionsScreen.kt`, `feature/dashboard/DashboardScreen.kt`, `feature/dashboard/DashboardViewModel.kt`.
**Produces:** `@Composable fun SwipeTransactionRow(item, onEdit, onDelete, onDuplicate, content)`.
- [ ] Extract `TransactionsScreen.SwipeRow` into `SwipeTransactionRow` (same `SwipeToDismissBox` behavior: EndToStart=delete, StartToEnd=duplicate; the row content passed in). Use it in `TransactionsScreen`.
- [ ] `DashboardViewModel`: add `duplicateTransaction(t)` + `deleteTransaction(id)` (reuse existing use-cases; mirror Transactions VM). Wrap dashboard recent rows in `SwipeTransactionRow` (tap = open edit sheet with `editId`).
- [ ] Add edit-sheet state to Dashboard (open `AddEditTransactionSheet(editId = tappedId)`).
- [ ] Build → green. Commit: `feat(home): swipe to delete/duplicate + tap to edit on recent list`

## Task A3: Exact transaction time
**Files:** `feature/transactions/AddEditTransactionSheet.kt`, `feature/transactions/AddEditTransactionViewModel.kt`.
- [ ] VM: `setDate` preserves current time-of-day (combine picked date with existing hour/min); add `setTime(hour: Int, minute: Int)` updating `form.date`.
- [ ] Sheet: add a Time button beside the Date button; show `TimePicker` in an `AlertDialog` (pattern from `ReminderSheet`). Display formatted time.
- [ ] Build → green. Commit: `feat(transactions): pick exact time, not just date`

## Task B1: Onboarding live-rate prefill
**Files:** `feature/onboarding/OnboardingViewModel.kt`.
- [ ] Inject `ExchangeRateApi` + `AppConfig`. On init, `resolveApiKey(null, appConfig)` → if non-null, `fetchRates`; on Success set `rateUsd`/`rateEur` state (as strings) only if the user hasn't edited them (track an `edited` flag or check against defaults). Silent on failure.
- [ ] Build → green. Commit: `feat(onboarding): prefill currency rates from the internet`

## Task E1: Clear-all resets to defaults
**Files:** `core/backup/BackupManager.kt`, test `SettingsRepositoryTest.kt` or a new `BackupManagerTest` (Robolectric).
- [ ] `clearAllData()`: after `db.clearAllTables()`, `db.categoryDao().insertAll(DatabaseSeed.categories())`. 
- [ ] Robolectric test: seed→clearAllData→categories == defaults (non-empty, matches `DatabaseSeed.categories().size`).
- [ ] Test + build → green. Commit: `feat(data): clear-all resets to default categories`

## Task F1: 5th tab → Settings
**Files:** `navigation/BottomBar.kt`, `values*/strings.xml`.
- [ ] `BottomItem(Routes.SETTINGS, "settings", R.string.nav_more)` (icon `more_horiz`→`settings`). Change `nav_more` value: RU "Настройки", EN "Settings".
- [ ] Build → green. Commit: `feat(nav): rename 5th tab to Settings with gear icon`

## Final
- [ ] `./gradlew :app:assembleDebug :app:assembleRelease :app:testDebugUnitTest --offline` → all green; `estate.apk` present.
- [ ] Merge feature branch to `master`.

## Self-Review
- Coverage: A1→A1, A2→A2, A3→A3, B→B1, C1(sortOrder)→C1, C2(drag)→C2, C3(color)→C3, C4(curate)→C4, C5(RU search)→C5, D→D1+D2, E→E1, F→F1. All streams covered.
- Placeholders: none (curated icon set + RU keyword map are concrete curation tasks).
- Types: `reorder(orderedIds)`, `AnalyticsPeriod{WEEK,MONTH,YEAR,ALL}`, `analyticsStart(now,period,rolling)`, `startOfWeek`, `iconMatches(name,query)`, `SwipeTransactionRow(...)` used consistently.
