# estate — Full UI Redesign (decisions log)

Collaborative redesign: per area I propose 2–3 variants, the user picks/annotates,
decisions are recorded here, and everything is implemented together at the end.

Current baseline: "Soft Depth" (gradient hero, floating soft-shadow cards, rounded
shapes, rounded Material icons, Fraunces for balance / Inter for UI, purple/orange
accents). Build is on F: toolchain; verify with compile + unit tests + assembleDebug.

## Areas & status
1. Visual foundation — **DONE (Variant A — Warm Serif / Quiet Minimal)**
2. Navigation & information architecture — **DONE**
3. Splash / Onboarding / Lock — **DONE (by pattern)**
4. Home / Dashboard — **DONE (mockup Variant A)**
5. Add / Edit transaction — **DONE (+ note field)**
6. Transactions (history) — **DONE (note shown in row)**
7. Budgets — **DONE (+ thin hairline frame)**
8. Analytics — **DONE (horizontal category bars + month bars)**
+ Dark theme (Quiet Minimal, warm) — **DONE**
9. Reminders — **DONE (by pattern)**
10. Categories — **DONE (by pattern)**
11. Settings / More — **DONE (by pattern)**
+ Icons — **DONE (monochrome)**

## Decisions

### Icons ✅ — monochrome
Material Symbols **rounded**, single ink/graphite colour on a neutral tile (no per-category
colour, no emoji). Colour reserved for amounts (income/expense), the accent, and selected states.

### Remaining screens — locked "by pattern" (Quiet Minimal, same tokens)
- **Splash:** "estate" in Fraunces on warm paper, thin accent rule, tagline eyebrow.
- **Onboarding:** serif step headings, air, accent primary buttons, flat.
- **Lock (PIN):** minimal PIN dots, small serif wordmark, warm paper; biometric prompt.
- **Reminders / Categories:** hairline list rows (like History) with monochrome icon tiles;
  serif screen title; add/edit bottom sheet with thin-bordered fields + segmented pills + accent Save.
- **"Ещё" hub:** serif "Ещё" title; large hairline rows (Настройки, Категории, Напоминания,
  Резервная копия) with monochrome icon + chevron.
- **Settings:** flat grouped sections (serif group headers + hairlines, no cards); segmented
  pill selectors kept; scheme picker → accent options (green default).

### 1. Visual foundation — Quiet Minimal (Warm Serif)  ✅
Flat: no gradients, no shadows. Separation via whitespace + hairlines.
- **Palette:** paper `#F7F6F3` (warm off-white), ink `#1A1712`, muted `#9A9488`,
  hairline `#EEEAE2`, accent **deep green `#2F5D50`** (default). Income `#2F6B4F`,
  expense `#B0492F` (warm brick, not pure red).
- **Type:** Fraunces (serif) for balance, amounts, section titles; Inter for
  labels/UI. Big size contrast (large serif numerals vs tiny uppercase eyebrows).
- **Shape/detail:** rounded-but-restrained icon tiles (~12–14dp), hairline dividers,
  generous whitespace. Bottom bar hairline top, flat.
- **Motion:** minimal — count-up on balance, gentle spring on press, fades. No bounce.
- Reference mockup: `docs/superpowers/mockups/home.html` (Variant A).
- Note: keep purple/orange as optional accents later, but green is the new default.

### 2. Navigation ✅
- Bottom bar: **Главная · История · Бюджеты · Анализ · Ещё** (5 tabs, flat, hairline top).
- **Add** = flat accent circular "+" FAB, bottom-right (no shadow). Kept 5 tabs.
- **"Ещё" is a hub screen** (not everything crammed in Settings): rows for
  Настройки, Категории, Напоминания, Резервная копия. Declutters Settings.

### 4. Home / Dashboard ✅  (= mockup Variant A)
Eyebrow "Баланс · <month>", huge Fraunces balance, income/expense as two small
serif stats under a hairline, "Последние" hairline list of recent rows, flat FAB.

### 5–8 Screens ✅ (mockup `docs/superpowers/mockups/screens.html`, Variant A)
- **History:** day = uppercase eyebrow; rows on hairlines; amount in serif; search =
  thin bordered field; filters = text tabs. **Row shows the note** as the secondary
  line under the category name (left side, muted). Note is optional.
- **Add:** huge serif amount on top; type = segmented pill (Расход|Доход); category =
  grid of icon tiles (selected tile in accent); **+ a note field ("Заметка")** below
  the grid, above Save; single accent Save button.
- **Budgets:** no cards but **a thin hairline frame around each budget block**; inside:
  category + %, thin progress line (green/amber `#E0A21B`/brick), spent/limit muted.
- **Analytics:** period text-tabs (Месяц/Год/Всё); income/expense serif summary;
  category breakdown as thin horizontal bars; months as compact bars. (Donut dropped
  in favour of calmer bars.)

### Dark theme (Quiet Minimal, warm) ✅
Same language, night palette: paper `#15130E` (warm near-black), surface/tile `#221E17`,
ink `#EDEAE2`, muted `#8F8A7E`, hairline `#2A2620`, accent `#4E9B7E` (brighter green),
income `#5FA98A`, expense `#D2795E`. Everything else identical (flat, hairlines, serif).
Mockup: `docs/superpowers/mockups/refinements.html`.
