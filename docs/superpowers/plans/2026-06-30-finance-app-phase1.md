# FinanceApp — Phase 1 (Vertical Slice) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a runnable debug APK of a Kotlin/Compose personal-finance app whose core works end-to-end: Splash → Onboarding → Lock(PIN) → Dashboard + Transactions (add/edit/delete/duplicate) + Settings (theme/scheme/language/currency/PIN), persisted in Room.

**Architecture:** Single Gradle module `:app`, Clean Architecture in Kotlin packages (UI → ViewModel → UseCase → Repository → DataSource). Hilt for DI, Room for persistence, DataStore for settings, Compose Material 3 for UI, Vico for the dashboard mini-chart. Pure logic (utils, mappers, use cases, repositories, DataStore, DAOs) is unit-tested on the JVM (JUnit + Robolectric for Android-dependent pieces). Composables/ViewModels are verified by `assembleDebug` compiling plus ViewModel unit tests; there is **no emulator** in this environment, so instrumentation/UI tests are not run here.

**Tech Stack:** Kotlin 2.x (K2 + Compose Compiler plugin), AGP 8.x, JDK 17, Compose BOM, Room + KSP, Hilt + KSP, DataStore Preferences, Vico, Lottie (graceful static fallback), AndroidX Biometric, kotlinx-serialization, Coroutines/Flow.

## Global Constraints

- **All toolchain/SDK/Gradle caches/build temp live on F: only** — `JAVA_HOME=F:\android-dev\jdk\jdk-17*`, `ANDROID_HOME=ANDROID_SDK_ROOT=F:\android-dev\sdk`, `ANDROID_USER_HOME=F:\android-dev\.android`, `GRADLE_USER_HOME=F:\android-dev\gradle-home`. Never write tooling to C:.
- `minSdk = 29`, `compileSdk = 35`, `targetSdk = 35`. JDK 17. Java/Kotlin jvmTarget = 17.
- Single Gradle module `:app`, package root `com.financeapp`. Versions only via `gradle/libs.versions.toml`.
- **No hardcoded user-facing strings** — every visible string from `res/values/strings.xml` (en, default) + `res/values-ru/strings.xml` (ru).
- **No hardcoded colors** — all from `MaterialTheme.colorScheme`. Two schemes: PURPLE (primary `0xFF7C3AED`, secondary `0xFF9F67F8`, tertiary `0xFFE879F9`) and ORANGE (primary `0xFFEA580C`, secondary `0xFFF97316`, tertiary `0xFFFBBF24`), each with light + dark.
- Currencies fixed to enum `RUB, USD, EUR`. Base currency + USD/EUR rates from settings; conversion to base via `CurrencyConverter`.
- App is fully offline. No network permission required by app code.
- Each code step shows complete code. Commit after every task. Run unit tests with the F: toolchain env sourced (`source F:/android-dev/env.sh` in Git Bash, then `./gradlew testDebugUnitTest`).

## File Structure (created across the plan)

```
settings.gradle.kts, build.gradle.kts, gradle.properties, local.properties (F: sdk.dir)
gradle/libs.versions.toml, gradle/wrapper/*
app/build.gradle.kts, app/proguard-rules.pro
app/src/main/AndroidManifest.xml
app/src/main/res/values/strings.xml, values-ru/strings.xml, values/themes.xml, mipmap*, drawable*
app/src/main/java/com/financeapp/
  FinanceApp.kt                      (Application + @HiltAndroidApp)
  MainActivity.kt                    (single activity, hosts NavHost + theme)
  core/
    domain/
      model/Enums.kt, Money.kt, Category.kt, Transaction.kt, AppSettings.kt,
            DashboardData.kt, TransactionWithCategory.kt, TransactionFilter.kt
      repository/{TransactionRepository,CategoryRepository,SettingsRepository}.kt
      usecase/{GetDashboardData,ObserveTransactions,SaveTransaction,DeleteTransaction,
               DuplicateTransaction,ObserveCategories,SetPin,VerifyPin}UseCase.kt
    data/
      local/entity/{TransactionEntity,CategoryEntity,BudgetEntity,ReminderEntity,RecurringRuleEntity}.kt
      local/dao/{TransactionDao,CategoryDao,BudgetDao,ReminderDao,RecurringRuleDao}.kt
      local/{FinanceDatabase,Converters,DatabaseSeed}.kt
      datastore/SettingsDataStore.kt
      mapper/{TransactionMapper,CategoryMapper}.kt
      repository/{TransactionRepositoryImpl,CategoryRepositoryImpl,SettingsRepositoryImpl}.kt
      di/{DatabaseModule,RepositoryModule,DataStoreModule}.kt
    ui/
      theme/{Color.kt,Theme.kt,Type.kt}
      components/{BalanceCard,TransactionRow,EmptyState,CategoryIcon,PinDots,...}.kt
      anim/Animations.kt
      icons/MaterialIconMap.kt
    utils/{CurrencyFormatter,DateUtils,PinHasher,CurrencyConverter,CategoryNames,Haptics}.kt
  navigation/{Routes.kt,FinanceNavHost.kt,BottomBar.kt}
  feature/
    splash/SplashScreen.kt
    onboarding/{OnboardingScreen.kt,OnboardingViewModel.kt}
    lock/{LockScreen.kt,LockViewModel.kt}
    dashboard/{DashboardScreen.kt,DashboardViewModel.kt}
    transactions/{TransactionsScreen.kt,TransactionsViewModel.kt,AddEditTransactionSheet.kt,AddEditTransactionViewModel.kt}
    settings/{SettingsScreen.kt,SettingsViewModel.kt}
    placeholder/PlaceholderScreen.kt   (budgets, analytics)
app/src/test/java/com/financeapp/...   (JVM unit tests, Robolectric where needed)
```

---

### Task 0: Project scaffold + build config

**Files:**
- Create: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `local.properties`, `gradle/libs.versions.toml`
- Create: `app/build.gradle.kts`, `app/proguard-rules.pro`, `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/financeapp/FinanceApp.kt`, `.../MainActivity.kt`
- Create: `app/src/main/res/values/strings.xml`, `.../values/themes.xml`, launcher icon drawables
- Test: `app/src/test/java/com/financeapp/ExampleUnitTest.kt`

**Interfaces:**
- Produces: buildable `:app` module; `FinanceApp` (@HiltAndroidApp Application); `MainActivity` (ComponentActivity). Gradle wrapper `./gradlew`.

- [ ] **Step 1: Bootstrap the Gradle wrapper (uses F: Gradle)**

In Git Bash from `F:/estate`:
```bash
source /f/android-dev/env.sh
/f/android-dev/gradle/gradle-8.10.2/bin/gradle.bat wrapper --gradle-version 8.10.2 --distribution-type bin
```
Expected: creates `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`.

- [ ] **Step 2: Write `local.properties` (F: SDK path — not committed)**

```properties
sdk.dir=F:\\android-dev\\sdk
```

- [ ] **Step 3: Write `gradle/libs.versions.toml`**

```toml
[versions]
agp = "8.7.3"
kotlin = "2.0.21"
ksp = "2.0.21-1.0.28"
coreKtx = "1.15.0"
lifecycle = "2.8.7"
activityCompose = "1.9.3"
composeBom = "2024.10.01"
navigationCompose = "2.8.4"
hilt = "2.52"
hiltNavigationCompose = "1.2.0"
room = "2.6.1"
datastore = "1.1.1"
coroutines = "1.9.0"
serialization = "1.7.3"
vico = "1.16.0"
lottie = "6.5.2"
biometric = "1.1.0"
appcompat = "1.7.0"
material = "1.12.0"
junit = "4.13.2"
robolectric = "4.13"
androidxTestCore = "1.6.1"
truth = "1.4.4"

[libraries]
androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "coreKtx" }
androidx-appcompat = { module = "androidx.appcompat:appcompat", version.ref = "appcompat" }
androidx-lifecycle-runtime-ktx = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
androidx-ui = { module = "androidx.compose.ui:ui" }
androidx-ui-graphics = { module = "androidx.compose.ui:ui-graphics" }
androidx-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
androidx-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
androidx-material3 = { module = "androidx.compose.material3:material3" }
androidx-material-icons-extended = { module = "androidx.compose.material:material-icons-extended" }
androidx-navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigationCompose" }
androidx-datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }
androidx-biometric = { module = "androidx.biometric:biometric", version.ref = "biometric" }
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version.ref = "hiltNavigationCompose" }
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
vico-compose-m3 = { module = "com.patrykandpatrick.vico:compose-m3", version.ref = "vico" }
lottie-compose = { module = "com.airbnb.android:lottie-compose", version.ref = "lottie" }
junit = { module = "junit:junit", version.ref = "junit" }
robolectric = { module = "org.robolectric:robolectric", version.ref = "robolectric" }
androidx-test-core = { module = "androidx.test:core", version.ref = "androidxTestCore" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
truth = { module = "com.google.truth:truth", version.ref = "truth" }
room-testing = { module = "androidx.room:room-testing", version.ref = "room" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

- [ ] **Step 4: Write `settings.gradle.kts`**

```kotlin
pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "FinanceApp"
include(":app")
```

- [ ] **Step 5: Write root `build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
```

- [ ] **Step 6: Write `gradle.properties`**

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.caching=true
org.gradle.parallel=true
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
```

- [ ] **Step 7: Write `app/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.financeapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.financeapp"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }
    buildTypes {
        debug { applicationIdSuffix = ".debug" }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    testOptions { unitTests { isIncludeAndroidResources = true } }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.biometric)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.vico.compose.m3)
    implementation(libs.lottie.compose)
    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.room.testing)
}
```

- [ ] **Step 8: Write `app/proguard-rules.pro`**

```proguard
# Room
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**
# Hilt / Dagger
-keep,allowobfuscation @interface dagger.hilt.* 
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.* { *; }
# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class com.financeapp.** { @kotlinx.serialization.Serializable <fields>; }
# Lottie
-dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** { *; }
# Vico
-dontwarn com.patrykandpatrick.vico.**
```

- [ ] **Step 9: Write `app/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.USE_BIOMETRIC" />
    <uses-permission android:name="android.permission.VIBRATE" />

    <application
        android:name=".FinanceApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher"
        android:supportsRtl="true"
        android:theme="@style/Theme.FinanceApp">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize"
            android:theme="@style/Theme.FinanceApp">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 10: Write launcher icon + base theme**

`app/src/main/res/values/themes.xml`:
```xml
<resources>
    <style name="Theme.FinanceApp" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```
`app/src/main/res/drawable/ic_launcher_foreground.xml` (simple coin/graph glyph, tinted at runtime):
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#FFFFFF"
        android:pathData="M54,30a24,24 0,1 0,0.01 0zM54,38a16,16 0,1 1,-0.01 0zM52,44h4v4h6v4h-6v6h6v4h-6v4h-4v-4h-6v-4h6v-6h-6v-4h6z"/>
</vector>
```
`app/src/main/res/values/ic_launcher_background.xml`:
```xml
<resources><color name="ic_launcher_background">#7C3AED</color></resources>
```
`app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` (and copy as `ic_launcher_round.xml`):
```xml
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

- [ ] **Step 11: Write `app/src/main/res/values/strings.xml` (minimal for now)**

```xml
<resources>
    <string name="app_name">FinanceApp</string>
</resources>
```

- [ ] **Step 12: Write `FinanceApp.kt` and `MainActivity.kt`**

```kotlin
// FinanceApp.kt
package com.financeapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FinanceApp : Application()
```
```kotlin
// MainActivity.kt
package com.financeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface { Text("FinanceApp") }
            }
        }
    }
}
```

- [ ] **Step 13: Write `ExampleUnitTest.kt`**

```kotlin
package com.financeapp

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test fun buildHarnessWorks() { assertEquals(4, 2 + 2) }
}
```

- [ ] **Step 14: Verify build + tests (F: toolchain)**

```bash
source /f/android-dev/env.sh
./gradlew :app:assembleDebug :app:testDebugUnitTest
```
Expected: BUILD SUCCESSFUL; APK at `app/build/outputs/apk/debug/app-debug.apk`; `ExampleUnitTest` passes.

- [ ] **Step 15: Commit**

```bash
git add -A && git commit -m "chore: scaffold Gradle/Compose/Hilt project (Phase 1 Task 0)"
```

---

### Task 1: Domain models + enums

**Files:**
- Create: `core/domain/model/Enums.kt`, `Money.kt`, `Category.kt`, `Transaction.kt`, `AppSettings.kt`, `TransactionWithCategory.kt`, `TransactionFilter.kt`, `DashboardData.kt`
- Test: `app/src/test/java/com/financeapp/core/domain/EnumsTest.kt`

**Interfaces:**
- Produces (exact types used everywhere downstream):
  - `enum class TransactionType { INCOME, EXPENSE }`
  - `enum class CategoryType { INCOME, EXPENSE, BOTH }`
  - `enum class BudgetPeriod { WEEKLY, MONTHLY, YEARLY }`
  - `enum class RepeatType { NONE, MONTHLY, YEARLY }`
  - `enum class IntervalType { DAILY, WEEKLY, MONTHLY, YEARLY }`
  - `enum class Currency(val code: String, val symbol: String) { RUB("RUB","₽"), USD("USD","$"), EUR("EUR","€") }`
  - `enum class ThemeMode { SYSTEM, LIGHT, DARK }`
  - `enum class ColorScheme { PURPLE, ORANGE }`
  - `enum class AppLanguage(val tag: String) { RU("ru"), EN("en") }`
  - `data class Category(id: Long, name: String, icon: String, color: Int, type: CategoryType, isCustom: Boolean)`
  - `data class Transaction(id: Long, amount: Double, currency: Currency, type: TransactionType, categoryId: Long?, note: String?, date: Long, recurringRuleId: Long?)`
  - `data class AppSettings(baseCurrency, rateUsd: Double, rateEur: Double, themeMode, colorScheme, pinHash: String?, biometricEnabled: Boolean, language: AppLanguage, onboardingCompleted: Boolean)` with defaults `RUB, 90.0, 100.0, SYSTEM, PURPLE, null, false, RU, false`.
  - `data class TransactionWithCategory(transaction: Transaction, category: Category?)`
  - `data class TransactionFilter(type: TransactionType? = null, categoryId: Long? = null, currency: Currency? = null, start: Long? = null, end: Long? = null, query: String = "")`
  - `data class DashboardData(balanceBase: Double, monthIncomeBase: Double, monthExpenseBase: Double, last7Days: List<DayAmount>, recent: List<TransactionWithCategory>)` with `data class DayAmount(dayStart: Long, expenseBase: Double)`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.financeapp.core.domain

import com.financeapp.core.domain.model.*
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EnumsTest {
    @Test fun currencyHasCodeAndSymbol() {
        assertThat(Currency.RUB.symbol).isEqualTo("₽")
        assertThat(Currency.valueOf("USD").code).isEqualTo("USD")
    }
    @Test fun defaultSettingsAreSane() {
        val s = AppSettings()
        assertThat(s.baseCurrency).isEqualTo(Currency.RUB)
        assertThat(s.themeMode).isEqualTo(ThemeMode.SYSTEM)
        assertThat(s.onboardingCompleted).isFalse()
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "*EnumsTest*"`
Expected: FAIL (unresolved references).

- [ ] **Step 3: Write the models** (create the files with the exact declarations from Interfaces above)

```kotlin
// Enums.kt
package com.financeapp.core.domain.model

enum class TransactionType { INCOME, EXPENSE }
enum class CategoryType { INCOME, EXPENSE, BOTH }
enum class BudgetPeriod { WEEKLY, MONTHLY, YEARLY }
enum class RepeatType { NONE, MONTHLY, YEARLY }
enum class IntervalType { DAILY, WEEKLY, MONTHLY, YEARLY }
enum class Currency(val code: String, val symbol: String) { RUB("RUB", "₽"), USD("USD", "$"), EUR("EUR", "€") }
enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class ColorScheme { PURPLE, ORANGE }
enum class AppLanguage(val tag: String) { RU("ru"), EN("en") }
```
```kotlin
// Category.kt
package com.financeapp.core.domain.model
data class Category(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val color: Int,
    val type: CategoryType,
    val isCustom: Boolean = false,
)
```
```kotlin
// Transaction.kt
package com.financeapp.core.domain.model
data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val currency: Currency,
    val type: TransactionType,
    val categoryId: Long?,
    val note: String?,
    val date: Long,
    val recurringRuleId: Long? = null,
)
```
```kotlin
// AppSettings.kt
package com.financeapp.core.domain.model
data class AppSettings(
    val baseCurrency: Currency = Currency.RUB,
    val rateUsd: Double = 90.0,
    val rateEur: Double = 100.0,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorScheme: ColorScheme = ColorScheme.PURPLE,
    val pinHash: String? = null,
    val biometricEnabled: Boolean = false,
    val language: AppLanguage = AppLanguage.RU,
    val onboardingCompleted: Boolean = false,
)
```
```kotlin
// TransactionWithCategory.kt
package com.financeapp.core.domain.model
data class TransactionWithCategory(val transaction: Transaction, val category: Category?)
```
```kotlin
// TransactionFilter.kt
package com.financeapp.core.domain.model
data class TransactionFilter(
    val type: TransactionType? = null,
    val categoryId: Long? = null,
    val currency: Currency? = null,
    val start: Long? = null,
    val end: Long? = null,
    val query: String = "",
)
```
```kotlin
// DashboardData.kt
package com.financeapp.core.domain.model
data class DayAmount(val dayStart: Long, val expenseBase: Double)
data class DashboardData(
    val balanceBase: Double = 0.0,
    val monthIncomeBase: Double = 0.0,
    val monthExpenseBase: Double = 0.0,
    val last7Days: List<DayAmount> = emptyList(),
    val recent: List<TransactionWithCategory> = emptyList(),
)
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "*EnumsTest*"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat: domain models and enums (Phase 1 Task 1)"
```

---

### Task 2: Utils (PIN hash, currency conversion/format, dates)

**Files:**
- Create: `core/utils/PinHasher.kt`, `CurrencyConverter.kt`, `CurrencyFormatter.kt`, `DateUtils.kt`, `Haptics.kt`
- Test: `.../core/utils/PinHasherTest.kt`, `CurrencyConverterTest.kt`, `CurrencyFormatterTest.kt`, `DateUtilsTest.kt`

**Interfaces:**
- Produces:
  - `object PinHasher { fun hash(pin: String): String }` — SHA-256 lowercase hex.
  - `object CurrencyConverter { fun toBase(amount: Double, from: Currency, s: AppSettings): Double }` — pivots through RUB; `rateUsd` = RUB per 1 USD, `rateEur` = RUB per 1 EUR.
  - `object CurrencyFormatter { fun format(amount: Double, currency: Currency): String }` — RUB suffix "₽", USD prefix "$", EUR prefix "€", 2 decimals, US grouping.
  - `object DateUtils { fun startOfDay(millis, zone=ZoneId.systemDefault()): Long; fun startOfMonth(...): Long; fun lastNDayStarts(nowMillis, n, zone): List<Long>; fun dayLabel(dayStart, nowMillis, zone): DayLabel }` where `sealed interface DayLabel { object Today; object Yesterday; data class Other(val text: String) }`.
  - `Haptics` composable helpers `hapticSuccess()` / `hapticError()` (no test).

- [ ] **Step 1: Write failing tests**

```kotlin
// PinHasherTest.kt
package com.financeapp.core.utils
import com.google.common.truth.Truth.assertThat
import org.junit.Test
class PinHasherTest {
    @Test fun hashesKnownValue() {
        // SHA-256("1234")
        assertThat(PinHasher.hash("1234"))
            .isEqualTo("03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4")
    }
    @Test fun differentPinsDiffer() {
        assertThat(PinHasher.hash("0000")).isNotEqualTo(PinHasher.hash("0001"))
    }
}
```
```kotlin
// CurrencyConverterTest.kt
package com.financeapp.core.utils
import com.financeapp.core.domain.model.*
import com.google.common.truth.Truth.assertThat
import org.junit.Test
class CurrencyConverterTest {
    private val s = AppSettings(baseCurrency = Currency.RUB, rateUsd = 90.0, rateEur = 100.0)
    @Test fun sameCurrencyIsIdentity() {
        assertThat(CurrencyConverter.toBase(100.0, Currency.RUB, s)).isWithin(0.001).of(100.0)
    }
    @Test fun usdToRub() {
        assertThat(CurrencyConverter.toBase(2.0, Currency.USD, s)).isWithin(0.001).of(180.0)
    }
    @Test fun rubToUsdBase() {
        val us = s.copy(baseCurrency = Currency.USD)
        assertThat(CurrencyConverter.toBase(180.0, Currency.RUB, us)).isWithin(0.001).of(2.0)
    }
}
```
```kotlin
// CurrencyFormatterTest.kt
package com.financeapp.core.utils
import com.financeapp.core.domain.model.Currency
import com.google.common.truth.Truth.assertThat
import org.junit.Test
class CurrencyFormatterTest {
    @Test fun formatsRub() { assertThat(CurrencyFormatter.format(1234.5, Currency.RUB)).isEqualTo("1,234.50 ₽") }
    @Test fun formatsUsd() { assertThat(CurrencyFormatter.format(1234.5, Currency.USD)).isEqualTo("$1,234.50") }
    @Test fun formatsEur() { assertThat(CurrencyFormatter.format(9.9, Currency.EUR)).isEqualTo("€9.90") }
}
```
```kotlin
// DateUtilsTest.kt
package com.financeapp.core.utils
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.ZoneId
class DateUtilsTest {
    private val z = ZoneId.of("UTC")
    private val noonJan15 = 1736942400000L // 2025-01-15T12:00:00Z
    @Test fun startOfDayZeroesTime() {
        val sod = DateUtils.startOfDay(noonJan15, z)
        assertThat(sod).isEqualTo(1736899200000L) // 2025-01-15T00:00Z
    }
    @Test fun last7DayStartsHas7Ascending() {
        val days = DateUtils.lastNDayStarts(noonJan15, 7, z)
        assertThat(days).hasSize(7)
        assertThat(days.last()).isEqualTo(DateUtils.startOfDay(noonJan15, z))
        assertThat(days).isInOrder()
    }
    @Test fun dayLabelToday() {
        assertThat(DateUtils.dayLabel(DateUtils.startOfDay(noonJan15, z), noonJan15, z))
            .isEqualTo(DateUtils.DayLabel.Today)
    }
}
```

- [ ] **Step 2: Run tests, verify they fail**

Run: `./gradlew :app:testDebugUnitTest --tests "com.financeapp.core.utils.*"`
Expected: FAIL (unresolved references).

- [ ] **Step 3: Implement utils**

```kotlin
// PinHasher.kt
package com.financeapp.core.utils
import java.security.MessageDigest
object PinHasher {
    fun hash(pin: String): String =
        MessageDigest.getInstance("SHA-256").digest(pin.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
```
```kotlin
// CurrencyConverter.kt
package com.financeapp.core.utils
import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.domain.model.Currency
object CurrencyConverter {
    fun toBase(amount: Double, from: Currency, s: AppSettings): Double {
        val inRub = when (from) {
            Currency.RUB -> amount
            Currency.USD -> amount * s.rateUsd
            Currency.EUR -> amount * s.rateEur
        }
        return when (s.baseCurrency) {
            Currency.RUB -> inRub
            Currency.USD -> inRub / s.rateUsd
            Currency.EUR -> inRub / s.rateEur
        }
    }
}
```
```kotlin
// CurrencyFormatter.kt
package com.financeapp.core.utils
import com.financeapp.core.domain.model.Currency
import java.util.Locale
object CurrencyFormatter {
    fun format(amount: Double, currency: Currency): String {
        val n = String.format(Locale.US, "%,.2f", amount)
        return when (currency) {
            Currency.RUB -> "$n ₽"
            Currency.USD -> "$$n"
            Currency.EUR -> "€$n"
        }
    }
}
```
```kotlin
// DateUtils.kt
package com.financeapp.core.utils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
object DateUtils {
    sealed interface DayLabel {
        data object Today : DayLabel
        data object Yesterday : DayLabel
        data class Other(val text: String) : DayLabel
    }
    private fun date(millis: Long, zone: ZoneId): LocalDate =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
    fun startOfDay(millis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        date(millis, zone).atStartOfDay(zone).toInstant().toEpochMilli()
    fun startOfMonth(millis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        date(millis, zone).withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
    fun lastNDayStarts(nowMillis: Long, n: Int, zone: ZoneId = ZoneId.systemDefault()): List<Long> {
        val today = date(nowMillis, zone)
        return (n - 1 downTo 0).map { today.minusDays(it.toLong()).atStartOfDay(zone).toInstant().toEpochMilli() }
    }
    fun dayLabel(dayStart: Long, nowMillis: Long, zone: ZoneId = ZoneId.systemDefault()): DayLabel {
        val d = date(dayStart, zone); val today = date(nowMillis, zone)
        return when (d) {
            today -> DayLabel.Today
            today.minusDays(1) -> DayLabel.Yesterday
            else -> DayLabel.Other(d.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())))
        }
    }
}
```
```kotlin
// Haptics.kt
package com.financeapp.core.utils
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
@Composable
fun rememberHaptics(): (Boolean) -> Unit {
    val h = LocalHapticFeedback.current
    return { success -> h.performHapticFeedback(if (success) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove) }
}
```

- [ ] **Step 4: Run tests, verify they pass**

Run: `./gradlew :app:testDebugUnitTest --tests "com.financeapp.core.utils.*"`
Expected: PASS (all).

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat: utils - pin hash, currency convert/format, dates (Phase 1 Task 2)"
```

---

### Task 3: Room — entities, DAOs, database, category seed

**Files:**
- Create: `core/data/local/entity/{Transaction,Category,Budget,Reminder,RecurringRule}Entity.kt`
- Create: `core/data/local/dao/{Transaction,Category,Budget,Reminder,RecurringRule}Dao.kt`
- Create: `core/data/local/FinanceDatabase.kt`, `core/data/local/DatabaseSeed.kt`
- Test: `.../core/data/DaoTest.kt` (Robolectric, in-memory DB)

**Interfaces:**
- Produces: `FinanceDatabase` (Room, version 1, 5 entities) exposing `transactionDao()`, `categoryDao()`, `budgetDao()`, `reminderDao()`, `recurringRuleDao()`. `DatabaseSeed.categories(): List<CategoryEntity>` (15 rows: 10 expense + 4 income + shared "other"). Enums are stored as their `.name` String columns; mapping to domain enums happens in Task 5 mappers (no Room TypeConverters needed).
- Consumes: nothing (pure data layer).

**Note:** Enum columns store `TransactionType.name`, `CategoryType.name`, etc. `isCustom`/`autoAdd` are `Boolean` (Room-supported).

- [ ] **Step 1: Write failing DAO test (Robolectric)**

```kotlin
// DaoTest.kt
package com.financeapp.core.data
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.financeapp.core.data.local.FinanceDatabase
import com.financeapp.core.data.local.entity.TransactionEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
@RunWith(RobolectricTestRunner::class)
class DaoTest {
    private lateinit var db: FinanceDatabase
    @Before fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, FinanceDatabase::class.java).allowMainThreadQueries().build()
    }
    @After fun tearDown() = db.close()
    @Test fun insertAndObserveTransaction() = runTest {
        val id = db.transactionDao().upsert(
            TransactionEntity(amount = 50.0, currency = "RUB", type = "EXPENSE",
                categoryId = null, note = "test", date = 1000L, recurringRuleId = null))
        assertThat(id).isGreaterThan(0L)
        val all = db.transactionDao().observeAll().first()
        assertThat(all).hasSize(1)
        assertThat(all.first().note).isEqualTo("test")
    }
}
```

- [ ] **Step 2: Run test, verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "*DaoTest*"` → FAIL (unresolved).

- [ ] **Step 3: Create entities**

```kotlin
// TransactionEntity.kt
package com.financeapp.core.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val currency: String,
    val type: String,
    val categoryId: Long?,
    val note: String?,
    val date: Long,
    val recurringRuleId: Long?,
)
```
```kotlin
// CategoryEntity.kt
package com.financeapp.core.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String,
    val color: Int,
    val type: String,
    val isCustom: Boolean = false,
)
```
```kotlin
// BudgetEntity.kt
package com.financeapp.core.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val limitAmount: Double,
    val currency: String,
    val periodType: String,
)
```
```kotlin
// ReminderEntity.kt
package com.financeapp.core.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double?,
    val currency: String?,
    val dueDate: Long,
    val notifyDaysBefore: Int,
    val repeatType: String,
)
```
```kotlin
// RecurringRuleEntity.kt
package com.financeapp.core.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "recurring_rules")
data class RecurringRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateJson: String,
    val intervalType: String,
    val nextDate: Long,
    val autoAdd: Boolean = false,
)
```

- [ ] **Step 4: Create DAOs**

```kotlin
// TransactionDao.kt
package com.financeapp.core.data.local.dao
import androidx.room.*
import com.financeapp.core.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun observeAll(): Flow<List<TransactionEntity>>
    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<TransactionEntity>>
    @Query("SELECT * FROM transactions WHERE date >= :start AND date < :end ORDER BY date DESC")
    fun observeBetween(start: Long, end: Long): Flow<List<TransactionEntity>>
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?
    @Upsert suspend fun upsert(e: TransactionEntity): Long
    @Query("DELETE FROM transactions WHERE id = :id") suspend fun delete(id: Long)
}
```
```kotlin
// CategoryDao.kt
package com.financeapp.core.data.local.dao
import androidx.room.*
import com.financeapp.core.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY id")
    fun observeAll(): Flow<List<CategoryEntity>>
    @Query("SELECT * FROM categories WHERE type = :type OR type = 'BOTH' ORDER BY id")
    fun observeByType(type: String): Flow<List<CategoryEntity>>
    @Query("SELECT * FROM categories WHERE id = :id") suspend fun getById(id: Long): CategoryEntity?
    @Query("SELECT COUNT(*) FROM categories") suspend fun count(): Int
    @Upsert suspend fun upsert(e: CategoryEntity): Long
    @Insert suspend fun insertAll(items: List<CategoryEntity>)
    @Query("DELETE FROM categories WHERE id = :id") suspend fun delete(id: Long)
}
```
```kotlin
// BudgetDao.kt
package com.financeapp.core.data.local.dao
import androidx.room.*
import com.financeapp.core.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY id") fun observeAll(): Flow<List<BudgetEntity>>
    @Upsert suspend fun upsert(e: BudgetEntity): Long
    @Query("DELETE FROM budgets WHERE id = :id") suspend fun delete(id: Long)
}
```
```kotlin
// ReminderDao.kt
package com.financeapp.core.data.local.dao
import androidx.room.*
import com.financeapp.core.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY dueDate") fun observeAll(): Flow<List<ReminderEntity>>
    @Upsert suspend fun upsert(e: ReminderEntity): Long
    @Query("DELETE FROM reminders WHERE id = :id") suspend fun delete(id: Long)
}
```
```kotlin
// RecurringRuleDao.kt
package com.financeapp.core.data.local.dao
import androidx.room.*
import com.financeapp.core.data.local.entity.RecurringRuleEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface RecurringRuleDao {
    @Query("SELECT * FROM recurring_rules ORDER BY nextDate") fun observeAll(): Flow<List<RecurringRuleEntity>>
    @Query("SELECT * FROM recurring_rules WHERE nextDate <= :now") suspend fun due(now: Long): List<RecurringRuleEntity>
    @Upsert suspend fun upsert(e: RecurringRuleEntity): Long
    @Query("DELETE FROM recurring_rules WHERE id = :id") suspend fun delete(id: Long)
}
```

- [ ] **Step 5: Create seed + database**

```kotlin
// DatabaseSeed.kt
package com.financeapp.core.data.local
import com.financeapp.core.data.local.entity.CategoryEntity
object DatabaseSeed {
    // name = string-resource KEY resolved in UI; colors are DATA (not UI theme colors).
    fun categories(): List<CategoryEntity> = listOf(
        CategoryEntity(name = "cat_food",          icon = "restaurant",       color = 0xFFEF5350.toInt(), type = "EXPENSE"),
        CategoryEntity(name = "cat_transport",     icon = "directions_car",   color = 0xFF42A5F5.toInt(), type = "EXPENSE"),
        CategoryEntity(name = "cat_health",        icon = "medical_services", color = 0xFF26A69A.toInt(), type = "EXPENSE"),
        CategoryEntity(name = "cat_entertainment", icon = "sports_esports",   color = 0xFFAB47BC.toInt(), type = "EXPENSE"),
        CategoryEntity(name = "cat_clothing",      icon = "checkroom",        color = 0xFFEC407A.toInt(), type = "EXPENSE"),
        CategoryEntity(name = "cat_communication", icon = "phone",            color = 0xFF5C6BC0.toInt(), type = "EXPENSE"),
        CategoryEntity(name = "cat_utilities",     icon = "home",             color = 0xFF8D6E63.toInt(), type = "EXPENSE"),
        CategoryEntity(name = "cat_education",     icon = "school",           color = 0xFF66BB6A.toInt(), type = "EXPENSE"),
        CategoryEntity(name = "cat_travel",        icon = "flight",           color = 0xFF29B6F6.toInt(), type = "EXPENSE"),
        CategoryEntity(name = "cat_other",         icon = "more_horiz",       color = 0xFF78909C.toInt(), type = "EXPENSE"),
        CategoryEntity(name = "cat_salary",        icon = "work",             color = 0xFF43A047.toInt(), type = "INCOME"),
        CategoryEntity(name = "cat_freelance",     icon = "laptop",           color = 0xFF7E57C2.toInt(), type = "INCOME"),
        CategoryEntity(name = "cat_investments",   icon = "trending_up",      color = 0xFF00897B.toInt(), type = "INCOME"),
        CategoryEntity(name = "cat_gifts",         icon = "card_giftcard",    color = 0xFFD81B60.toInt(), type = "INCOME"),
        CategoryEntity(name = "cat_other",         icon = "more_horiz",       color = 0xFF78909C.toInt(), type = "INCOME"),
    )
}
```
```kotlin
// FinanceDatabase.kt
package com.financeapp.core.data.local
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.financeapp.core.data.local.dao.*
import com.financeapp.core.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
@Database(
    entities = [TransactionEntity::class, CategoryEntity::class, BudgetEntity::class,
        ReminderEntity::class, RecurringRuleEntity::class],
    version = 1, exportSchema = false,
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun reminderDao(): ReminderDao
    abstract fun recurringRuleDao(): RecurringRuleDao

    companion object {
        const val NAME = "finance.db"
        fun build(context: Context, scope: CoroutineScope): FinanceDatabase {
            var instance: FinanceDatabase? = null
            instance = Room.databaseBuilder(context, FinanceDatabase::class.java, NAME)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        scope.launch(Dispatchers.IO) {
                            instance?.categoryDao()?.insertAll(DatabaseSeed.categories())
                        }
                    }
                })
                .build()
            return instance
        }
    }
}
```

- [ ] **Step 6: Run test, verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "*DaoTest*"` → PASS.

- [ ] **Step 7: Commit**

```bash
git add -A && git commit -m "feat: Room entities, DAOs, database + category seed (Phase 1 Task 3)"
```

---

### Task 4: DataStore + SettingsRepository

**Files:**
- Create: `core/data/datastore/SettingsDataStore.kt`
- Create: `core/domain/repository/SettingsRepository.kt`
- Create: `core/data/repository/SettingsRepositoryImpl.kt`
- Test: `.../core/data/SettingsRepositoryTest.kt` (Robolectric, temp DataStore)

**Interfaces:**
- Produces:
  - `interface SettingsRepository { val settings: Flow<AppSettings>; suspend fun setBaseCurrency(c); suspend fun setRates(usd, eur); suspend fun setThemeMode(m); suspend fun setColorScheme(s); suspend fun setLanguage(l); suspend fun setPinHash(hash: String?); suspend fun setBiometricEnabled(b); suspend fun setOnboardingCompleted(b) }`
  - `SettingsRepositoryImpl(dataStore: DataStore<Preferences>)` mapping all keys from the spec.
- Consumes: `AppSettings` + enums (Task 1).

- [ ] **Step 1: Write failing test**

```kotlin
// SettingsRepositoryTest.kt
package com.financeapp.core.data
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.financeapp.core.data.repository.SettingsRepositoryImpl
import com.financeapp.core.domain.model.*
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
@RunWith(RobolectricTestRunner::class)
class SettingsRepositoryTest {
    private fun repo(): SettingsRepositoryImpl {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val ds: DataStore<Preferences> = PreferenceDataStoreFactory.create {
            ctx.preferencesDataStoreFile("test_settings_${System.nanoTime()}")
        }
        return SettingsRepositoryImpl(ds)
    }
    @Test fun defaultsThenPersist() = runTest {
        val r = repo()
        assertThat(r.settings.first().baseCurrency).isEqualTo(Currency.RUB)
        r.setColorScheme(ColorScheme.ORANGE)
        r.setPinHash("abc")
        val s = r.settings.first()
        assertThat(s.colorScheme).isEqualTo(ColorScheme.ORANGE)
        assertThat(s.pinHash).isEqualTo("abc")
    }
}
```

- [ ] **Step 2: Run test → FAIL.**

Run: `./gradlew :app:testDebugUnitTest --tests "*SettingsRepositoryTest*"`

- [ ] **Step 3: Implement DataStore keys + repository**

```kotlin
// SettingsDataStore.kt
package com.financeapp.core.data.datastore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
object SettingsKeys {
    val BASE_CURRENCY = stringPreferencesKey("base_currency")
    val RATE_USD = doublePreferencesKey("exchange_rate_usd")
    val RATE_EUR = doublePreferencesKey("exchange_rate_eur")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val COLOR_SCHEME = stringPreferencesKey("color_scheme")
    val PIN_HASH = stringPreferencesKey("pin_hash")
    val BIOMETRIC = booleanPreferencesKey("biometric_enabled")
    val LANGUAGE = stringPreferencesKey("language")
    val ONBOARDING = booleanPreferencesKey("onboarding_completed")
}
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
```
```kotlin
// SettingsRepository.kt
package com.financeapp.core.domain.repository
import com.financeapp.core.domain.model.*
import kotlinx.coroutines.flow.Flow
interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setBaseCurrency(c: Currency)
    suspend fun setRates(usd: Double, eur: Double)
    suspend fun setThemeMode(m: ThemeMode)
    suspend fun setColorScheme(s: ColorScheme)
    suspend fun setLanguage(l: AppLanguage)
    suspend fun setPinHash(hash: String?)
    suspend fun setBiometricEnabled(b: Boolean)
    suspend fun setOnboardingCompleted(b: Boolean)
}
```
```kotlin
// SettingsRepositoryImpl.kt
package com.financeapp.core.data.repository
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.financeapp.core.data.datastore.SettingsKeys
import com.financeapp.core.domain.model.*
import com.financeapp.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {
    override val settings: Flow<AppSettings> = dataStore.data.map { p ->
        val def = AppSettings()
        AppSettings(
            baseCurrency = p[SettingsKeys.BASE_CURRENCY]?.let { Currency.valueOf(it) } ?: def.baseCurrency,
            rateUsd = p[SettingsKeys.RATE_USD] ?: def.rateUsd,
            rateEur = p[SettingsKeys.RATE_EUR] ?: def.rateEur,
            themeMode = p[SettingsKeys.THEME_MODE]?.let { ThemeMode.valueOf(it) } ?: def.themeMode,
            colorScheme = p[SettingsKeys.COLOR_SCHEME]?.let { ColorScheme.valueOf(it) } ?: def.colorScheme,
            pinHash = p[SettingsKeys.PIN_HASH],
            biometricEnabled = p[SettingsKeys.BIOMETRIC] ?: def.biometricEnabled,
            language = p[SettingsKeys.LANGUAGE]?.let { AppLanguage.valueOf(it) } ?: def.language,
            onboardingCompleted = p[SettingsKeys.ONBOARDING] ?: def.onboardingCompleted,
        )
    }
    override suspend fun setBaseCurrency(c: Currency) { dataStore.edit { it[SettingsKeys.BASE_CURRENCY] = c.name } }
    override suspend fun setRates(usd: Double, eur: Double) { dataStore.edit { it[SettingsKeys.RATE_USD] = usd; it[SettingsKeys.RATE_EUR] = eur } }
    override suspend fun setThemeMode(m: ThemeMode) { dataStore.edit { it[SettingsKeys.THEME_MODE] = m.name } }
    override suspend fun setColorScheme(s: ColorScheme) { dataStore.edit { it[SettingsKeys.COLOR_SCHEME] = s.name } }
    override suspend fun setLanguage(l: AppLanguage) { dataStore.edit { it[SettingsKeys.LANGUAGE] = l.name } }
    override suspend fun setPinHash(hash: String?) { dataStore.edit { if (hash == null) it.remove(SettingsKeys.PIN_HASH) else it[SettingsKeys.PIN_HASH] = hash } }
    override suspend fun setBiometricEnabled(b: Boolean) { dataStore.edit { it[SettingsKeys.BIOMETRIC] = b } }
    override suspend fun setOnboardingCompleted(b: Boolean) { dataStore.edit { it[SettingsKeys.ONBOARDING] = b } }
}
```

- [ ] **Step 4: Run test → PASS.**

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat: DataStore settings repository (Phase 1 Task 4)"
```

---

### Task 5: Mappers + domain repository interfaces + repository impls

**Files:**
- Create: `core/data/mapper/TransactionMapper.kt`, `CategoryMapper.kt`
- Create: `core/domain/repository/TransactionRepository.kt`, `CategoryRepository.kt`
- Create: `core/data/repository/TransactionRepositoryImpl.kt`, `CategoryRepositoryImpl.kt`
- Test: `.../core/data/MapperTest.kt`

**Interfaces:**
- Produces:
  - `fun TransactionEntity.toDomain(): Transaction` / `fun Transaction.toEntity(): TransactionEntity`
  - `fun CategoryEntity.toDomain(): Category` / `fun Category.toEntity(): CategoryEntity`
  - `interface TransactionRepository { fun observeAll(): Flow<List<Transaction>>; fun observeRecent(limit: Int): Flow<List<Transaction>>; fun observeBetween(start: Long, end: Long): Flow<List<Transaction>>; suspend fun getById(id: Long): Transaction?; suspend fun upsert(t: Transaction): Long; suspend fun delete(id: Long) }`
  - `interface CategoryRepository { fun observeAll(): Flow<List<Category>>; fun observeByType(type: CategoryType): Flow<List<Category>>; suspend fun getById(id: Long): Category?; suspend fun upsert(c: Category): Long; suspend fun delete(id: Long) }`

- [ ] **Step 1: Failing mapper test**

```kotlin
// MapperTest.kt
package com.financeapp.core.data
import com.financeapp.core.data.mapper.toDomain
import com.financeapp.core.data.mapper.toEntity
import com.financeapp.core.domain.model.*
import com.google.common.truth.Truth.assertThat
import org.junit.Test
class MapperTest {
    @Test fun transactionRoundTrip() {
        val t = Transaction(id = 5, amount = 12.5, currency = Currency.USD, type = TransactionType.EXPENSE,
            categoryId = 3, note = "x", date = 100L, recurringRuleId = null)
        assertThat(t.toEntity().toDomain()).isEqualTo(t)
    }
    @Test fun categoryRoundTrip() {
        val c = Category(id = 2, name = "cat_food", icon = "restaurant", color = -1, type = CategoryType.BOTH, isCustom = true)
        assertThat(c.toEntity().toDomain()).isEqualTo(c)
    }
}
```

- [ ] **Step 2: Run → FAIL.**

- [ ] **Step 3: Implement**

```kotlin
// TransactionMapper.kt
package com.financeapp.core.data.mapper
import com.financeapp.core.data.local.entity.TransactionEntity
import com.financeapp.core.domain.model.*
fun TransactionEntity.toDomain() = Transaction(id, amount, Currency.valueOf(currency),
    TransactionType.valueOf(type), categoryId, note, date, recurringRuleId)
fun Transaction.toEntity() = TransactionEntity(id, amount, currency.name, type.name, categoryId, note, date, recurringRuleId)
```
```kotlin
// CategoryMapper.kt
package com.financeapp.core.data.mapper
import com.financeapp.core.data.local.entity.CategoryEntity
import com.financeapp.core.domain.model.*
fun CategoryEntity.toDomain() = Category(id, name, icon, color, CategoryType.valueOf(type), isCustom)
fun Category.toEntity() = CategoryEntity(id, name, icon, color, type.name, isCustom)
```
```kotlin
// TransactionRepository.kt
package com.financeapp.core.domain.repository
import com.financeapp.core.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
interface TransactionRepository {
    fun observeAll(): Flow<List<Transaction>>
    fun observeRecent(limit: Int): Flow<List<Transaction>>
    fun observeBetween(start: Long, end: Long): Flow<List<Transaction>>
    suspend fun getById(id: Long): Transaction?
    suspend fun upsert(t: Transaction): Long
    suspend fun delete(id: Long)
}
```
```kotlin
// CategoryRepository.kt
package com.financeapp.core.domain.repository
import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow
interface CategoryRepository {
    fun observeAll(): Flow<List<Category>>
    fun observeByType(type: CategoryType): Flow<List<Category>>
    suspend fun getById(id: Long): Category?
    suspend fun upsert(c: Category): Long
    suspend fun delete(id: Long)
}
```
```kotlin
// TransactionRepositoryImpl.kt
package com.financeapp.core.data.repository
import com.financeapp.core.data.local.dao.TransactionDao
import com.financeapp.core.data.mapper.toDomain
import com.financeapp.core.data.mapper.toEntity
import com.financeapp.core.domain.model.Transaction
import com.financeapp.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
class TransactionRepositoryImpl @Inject constructor(private val dao: TransactionDao) : TransactionRepository {
    override fun observeAll() = dao.observeAll().map { l -> l.map { it.toDomain() } }
    override fun observeRecent(limit: Int) = dao.observeRecent(limit).map { l -> l.map { it.toDomain() } }
    override fun observeBetween(start: Long, end: Long) = dao.observeBetween(start, end).map { l -> l.map { it.toDomain() } }
    override suspend fun getById(id: Long) = dao.getById(id)?.toDomain()
    override suspend fun upsert(t: Transaction) = dao.upsert(t.toEntity())
    override suspend fun delete(id: Long) = dao.delete(id)
}
```
```kotlin
// CategoryRepositoryImpl.kt
package com.financeapp.core.data.repository
import com.financeapp.core.data.local.dao.CategoryDao
import com.financeapp.core.data.mapper.toDomain
import com.financeapp.core.data.mapper.toEntity
import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType
import com.financeapp.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
class CategoryRepositoryImpl @Inject constructor(private val dao: CategoryDao) : CategoryRepository {
    override fun observeAll() = dao.observeAll().map { l -> l.map { it.toDomain() } }
    override fun observeByType(type: CategoryType) = dao.observeByType(type.name).map { l -> l.map { it.toDomain() } }
    override suspend fun getById(id: Long) = dao.getById(id)?.toDomain()
    override suspend fun upsert(c: Category) = dao.upsert(c.toEntity())
    override suspend fun delete(id: Long) = dao.delete(id)
}
```

- [ ] **Step 4: Run → PASS. Step 5: Commit** `feat: mappers + repositories (Phase 1 Task 5)`

---

### Task 6: Hilt DI modules

**Files:**
- Create: `core/data/di/DataStoreModule.kt`, `DatabaseModule.kt`, `RepositoryModule.kt`
- Test: build-time (Hilt graph resolves) — verified by `assembleDebug`.

**Interfaces:**
- Produces bindings for `DataStore<Preferences>`, application `CoroutineScope` (`@AppScope`), `FinanceDatabase` + all DAOs, `SettingsRepository`, `TransactionRepository`, `CategoryRepository`.

- [ ] **Step 1: Implement modules**

```kotlin
// DataStoreModule.kt
package com.financeapp.core.data.di
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.financeapp.core.data.datastore.settingsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton
@Qualifier annotation class AppScope
@Module @InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides @Singleton fun dataStore(@ApplicationContext c: Context): DataStore<Preferences> = c.settingsDataStore
    @Provides @Singleton @AppScope fun appScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
```
```kotlin
// DatabaseModule.kt
package com.financeapp.core.data.di
import android.content.Context
import com.financeapp.core.data.local.FinanceDatabase
import com.financeapp.core.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton
@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton fun db(@ApplicationContext c: Context, @AppScope scope: CoroutineScope): FinanceDatabase =
        FinanceDatabase.build(c, scope)
    @Provides fun txDao(db: FinanceDatabase): TransactionDao = db.transactionDao()
    @Provides fun catDao(db: FinanceDatabase): CategoryDao = db.categoryDao()
    @Provides fun budgetDao(db: FinanceDatabase): BudgetDao = db.budgetDao()
    @Provides fun reminderDao(db: FinanceDatabase): ReminderDao = db.reminderDao()
    @Provides fun recurringDao(db: FinanceDatabase): RecurringRuleDao = db.recurringRuleDao()
}
```
```kotlin
// RepositoryModule.kt
package com.financeapp.core.data.di
import com.financeapp.core.data.repository.*
import com.financeapp.core.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun settings(impl: SettingsRepositoryImpl): SettingsRepository
    @Binds @Singleton abstract fun transactions(impl: TransactionRepositoryImpl): TransactionRepository
    @Binds @Singleton abstract fun categories(impl: CategoryRepositoryImpl): CategoryRepository
}
```

- [ ] **Step 2: Verify** `./gradlew :app:assembleDebug` compiles (Hilt graph OK). **Step 3: Commit** `feat: Hilt DI modules (Phase 1 Task 6)`

---

### Task 7: Use cases

**Files:**
- Create: `core/domain/usecase/ObserveCategoriesUseCase.kt`, `SaveTransactionUseCase.kt`, `DeleteTransactionUseCase.kt`, `DuplicateTransactionUseCase.kt`, `ObserveTransactionsUseCase.kt`, `GetDashboardDataUseCase.kt`, `SetPinUseCase.kt`, `VerifyPinUseCase.kt`
- Test: `.../core/domain/UseCaseTest.kt` (fake repositories, JVM)

**Interfaces:**
- `ObserveCategoriesUseCase(catRepo)`: `operator fun invoke(type: CategoryType? = null): Flow<List<Category>>`
- `SaveTransactionUseCase(txRepo)`: `suspend operator fun invoke(t: Transaction): Long`
- `DeleteTransactionUseCase(txRepo)`: `suspend operator fun invoke(id: Long)`
- `DuplicateTransactionUseCase(txRepo)`: `suspend operator fun invoke(id: Long, now: Long): Long?` (copy with `id=0`, `date=now`)
- `ObserveTransactionsUseCase(txRepo, catRepo)`: `operator fun invoke(filter: TransactionFilter): Flow<List<TransactionWithCategory>>` — combines transactions + categories, applies type/category/currency/date/query filters (query matches note or category name, case-insensitive).
- `GetDashboardDataUseCase(txRepo, catRepo, settingsRepo)`: `operator fun invoke(now: Long): Flow<DashboardData>` — balance = Σ(income base) − Σ(expense base) over all; month* over `[startOfMonth(now), now]`; `last7Days` = per-day expense base for `lastNDayStarts(now,7)`; `recent` = 5 newest with category.
- `SetPinUseCase(settingsRepo)`: `suspend operator fun invoke(pin: String)` → `setPinHash(PinHasher.hash(pin))`
- `VerifyPinUseCase(settingsRepo)`: `suspend operator fun invoke(pin: String): Boolean` → compares to stored hash.

- [ ] **Step 1: Failing test (fakes)**

```kotlin
// UseCaseTest.kt (excerpt — implement fakes for TransactionRepository/CategoryRepository/SettingsRepository)
package com.financeapp.core.domain
import com.financeapp.core.domain.model.*
import com.financeapp.core.domain.usecase.*
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
class UseCaseTest {
    @Test fun duplicateCopiesWithNowDate() = runTest {
        val repo = FakeTxRepo(mutableListOf(
            Transaction(id = 1, amount = 10.0, currency = Currency.RUB, type = TransactionType.EXPENSE,
                categoryId = null, note = "n", date = 100L, recurringRuleId = null)))
        val newId = DuplicateTransactionUseCase(repo)(1, now = 999L)
        val copy = repo.items.first { it.id == newId }
        assertThat(copy.date).isEqualTo(999L)
        assertThat(copy.amount).isEqualTo(10.0)
    }
    @Test fun filterByTypeAndQuery() = runTest {
        val txs = listOf(
            Transaction(1, 10.0, Currency.RUB, TransactionType.EXPENSE, 1, "coffee", 100, null),
            Transaction(2, 20.0, Currency.RUB, TransactionType.INCOME, 2, "pay", 200, null))
        val cats = listOf(Category(1, "cat_food", "restaurant", 0, CategoryType.EXPENSE),
                          Category(2, "cat_salary", "work", 0, CategoryType.INCOME))
        val uc = ObserveTransactionsUseCase(FakeTxRepo(txs.toMutableList()), FakeCatRepo(cats))
        val res = uc(TransactionFilter(type = TransactionType.EXPENSE)).first()
        assertThat(res.map { it.transaction.id }).containsExactly(1L)
        assertThat(res.first().category?.name).isEqualTo("cat_food")
    }
}
```
(Fakes: keep an in-memory `MutableList`, expose via `flowOf`/`MutableStateFlow`; `upsert` assigns `id = items.size+1` when `id==0`.)

- [ ] **Step 2: Run → FAIL. Step 3: Implement use cases**

```kotlin
// DuplicateTransactionUseCase.kt
package com.financeapp.core.domain.usecase
import com.financeapp.core.domain.repository.TransactionRepository
import javax.inject.Inject
class DuplicateTransactionUseCase @Inject constructor(private val repo: TransactionRepository) {
    suspend operator fun invoke(id: Long, now: Long): Long? {
        val src = repo.getById(id) ?: return null
        return repo.upsert(src.copy(id = 0, date = now, recurringRuleId = null))
    }
}
```
```kotlin
// SaveTransactionUseCase.kt
package com.financeapp.core.domain.usecase
import com.financeapp.core.domain.model.Transaction
import com.financeapp.core.domain.repository.TransactionRepository
import javax.inject.Inject
class SaveTransactionUseCase @Inject constructor(private val repo: TransactionRepository) {
    suspend operator fun invoke(t: Transaction): Long = repo.upsert(t)
}
```
```kotlin
// DeleteTransactionUseCase.kt
package com.financeapp.core.domain.usecase
import com.financeapp.core.domain.repository.TransactionRepository
import javax.inject.Inject
class DeleteTransactionUseCase @Inject constructor(private val repo: TransactionRepository) {
    suspend operator fun invoke(id: Long) = repo.delete(id)
}
```
```kotlin
// ObserveCategoriesUseCase.kt
package com.financeapp.core.domain.usecase
import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType
import com.financeapp.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
class ObserveCategoriesUseCase @Inject constructor(private val repo: CategoryRepository) {
    operator fun invoke(type: CategoryType? = null): Flow<List<Category>> =
        if (type == null) repo.observeAll() else repo.observeByType(type)
}
```
```kotlin
// ObserveTransactionsUseCase.kt
package com.financeapp.core.domain.usecase
import com.financeapp.core.domain.model.*
import com.financeapp.core.domain.repository.CategoryRepository
import com.financeapp.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
class ObserveTransactionsUseCase @Inject constructor(
    private val txRepo: TransactionRepository,
    private val catRepo: CategoryRepository,
) {
    operator fun invoke(filter: TransactionFilter): Flow<List<TransactionWithCategory>> =
        combine(txRepo.observeAll(), catRepo.observeAll()) { txs, cats ->
            val byId = cats.associateBy { it.id }
            txs.asSequence()
                .filter { filter.type == null || it.type == filter.type }
                .filter { filter.categoryId == null || it.categoryId == filter.categoryId }
                .filter { filter.currency == null || it.currency == filter.currency }
                .filter { filter.start == null || it.date >= filter.start }
                .filter { filter.end == null || it.date < filter.end }
                .map { TransactionWithCategory(it, it.categoryId?.let(byId::get)) }
                .filter { twc ->
                    filter.query.isBlank() ||
                        twc.transaction.note?.contains(filter.query, true) == true ||
                        twc.category?.name?.contains(filter.query, true) == true
                }
                .toList()
        }
}
```
```kotlin
// GetDashboardDataUseCase.kt
package com.financeapp.core.domain.usecase
import com.financeapp.core.domain.model.*
import com.financeapp.core.domain.repository.CategoryRepository
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.domain.repository.TransactionRepository
import com.financeapp.core.utils.CurrencyConverter
import com.financeapp.core.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
class GetDashboardDataUseCase @Inject constructor(
    private val txRepo: TransactionRepository,
    private val catRepo: CategoryRepository,
    private val settingsRepo: SettingsRepository,
) {
    operator fun invoke(now: Long): Flow<DashboardData> =
        combine(txRepo.observeAll(), catRepo.observeAll(), settingsRepo.settings) { txs, cats, s ->
            val byId = cats.associateBy { it.id }
            fun base(t: Transaction) = CurrencyConverter.toBase(t.amount, t.currency, s)
            val balance = txs.sumOf { if (it.type == TransactionType.INCOME) base(it) else -base(it) }
            val monthStart = DateUtils.startOfMonth(now)
            val monthTx = txs.filter { it.date in monthStart..now }
            val income = monthTx.filter { it.type == TransactionType.INCOME }.sumOf { base(it) }
            val expense = monthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { base(it) }
            val days = DateUtils.lastNDayStarts(now, 7)
            val series = days.map { d ->
                val next = d + 86_400_000L
                DayAmount(d, txs.filter { it.type == TransactionType.EXPENSE && it.date in d until next }.sumOf { base(it) })
            }
            val recent = txs.sortedByDescending { it.date }.take(5)
                .map { TransactionWithCategory(it, it.categoryId?.let(byId::get)) }
            DashboardData(balance, income, expense, series, recent)
        }
}
```
```kotlin
// SetPinUseCase.kt / VerifyPinUseCase.kt
package com.financeapp.core.domain.usecase
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.utils.PinHasher
import kotlinx.coroutines.flow.first
import javax.inject.Inject
class SetPinUseCase @Inject constructor(private val repo: SettingsRepository) {
    suspend operator fun invoke(pin: String) = repo.setPinHash(PinHasher.hash(pin))
}
class VerifyPinUseCase @Inject constructor(private val repo: SettingsRepository) {
    suspend operator fun invoke(pin: String): Boolean = repo.settings.first().pinHash == PinHasher.hash(pin)
}
```

- [ ] **Step 4: Run → PASS. Step 5: Commit** `feat: use cases (Phase 1 Task 7)`

---

> **UI fidelity note (Tasks 8–15):** ViewModels, UI-state classes, string resources, theme values, navigation, and non-obvious composable logic are given in full. Straightforward composable layout is specified concretely (exact components, state bindings, string keys, animations) and its body is completed during execution. Each UI task's gate is: `./gradlew :app:assembleDebug` compiles **and** the task's ViewModel unit test passes. Manual on-device verification is deferred (no emulator in this environment) and listed in Task 15's smoke checklist.

### Task 8: Theme (colors, typography, FinanceTheme)

**Files:** Create `core/ui/theme/Color.kt`, `Type.kt`, `Theme.kt`.

**Interfaces:** `@Composable fun FinanceTheme(themeMode: ThemeMode, colorScheme: ColorScheme, content: @Composable () -> Unit)`.

- [ ] **Step 1: Color.kt**

```kotlin
package com.financeapp.core.ui.theme
import androidx.compose.ui.graphics.Color
val PurplePrimary = Color(0xFF7C3AED); val PurpleSecondary = Color(0xFF9F67F8); val PurpleTertiary = Color(0xFFE879F9)
val OrangePrimary = Color(0xFFEA580C); val OrangeSecondary = Color(0xFFF97316); val OrangeTertiary = Color(0xFFFBBF24)
// 16-color palette for custom categories (data, not theme surfaces)
val CategoryPalette = listOf(
    Color(0xFFEF5350), Color(0xFF42A5F5), Color(0xFF26A69A), Color(0xFFAB47BC),
    Color(0xFFEC407A), Color(0xFF5C6BC0), Color(0xFF8D6E63), Color(0xFF66BB6A),
    Color(0xFF29B6F6), Color(0xFF78909C), Color(0xFF43A047), Color(0xFF7E57C2),
    Color(0xFF00897B), Color(0xFFD81B60), Color(0xFFFFA726), Color(0xFF9CCC65),
)
```

- [ ] **Step 2: Type.kt** — default Material 3 `Typography()` instance exported as `val AppTypography = Typography()`.

- [ ] **Step 3: Theme.kt**

```kotlin
package com.financeapp.core.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.financeapp.core.domain.model.ColorScheme as AppColorScheme
import com.financeapp.core.domain.model.ThemeMode

private fun purpleLight() = lightColorScheme(primary = PurplePrimary, secondary = PurpleSecondary, tertiary = PurpleTertiary)
private fun purpleDark()  = darkColorScheme(primary = PurplePrimary, secondary = PurpleSecondary, tertiary = PurpleTertiary)
private fun orangeLight() = lightColorScheme(primary = OrangePrimary, secondary = OrangeSecondary, tertiary = OrangeTertiary)
private fun orangeDark()  = darkColorScheme(primary = OrangePrimary, secondary = OrangeSecondary, tertiary = OrangeTertiary)

@Composable
fun FinanceTheme(themeMode: ThemeMode, colorScheme: AppColorScheme, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme(); ThemeMode.LIGHT -> false; ThemeMode.DARK -> true
    }
    val scheme = when (colorScheme) {
        AppColorScheme.PURPLE -> if (dark) purpleDark() else purpleLight()
        AppColorScheme.ORANGE -> if (dark) orangeDark() else orangeLight()
    }
    MaterialTheme(colorScheme = scheme, typography = AppTypography, content = content)
}
```

- [ ] **Step 4:** `assembleDebug` compiles. **Commit** `feat: Material 3 theme, purple/orange light+dark (Phase 1 Task 8)`

---

### Task 9: Localized strings, icon map, category-name resolver, shared components

**Files:**
- Create/replace `res/values/strings.xml` (en) and `res/values-ru/strings.xml` (ru)
- Create `core/ui/icons/MaterialIconMap.kt`, `core/ui/CategoryNames.kt`
- Create `core/ui/components/{CategoryIcon.kt, EmptyState.kt, PinDots.kt, AmountText.kt}`
- Create `core/ui/anim/Animations.kt`

**Interfaces:**
- `fun materialIcon(name: String): ImageVector` — maps stored icon names to `Icons.*` (fallback `Icons.Filled.MoreHoriz`).
- `@Composable fun categoryDisplayName(category: Category): String` — if `!isCustom` maps `name` key → `@StringRes`, else returns `category.name`.
- `@Composable fun CategoryIcon(category: Category?, size: Dp = 40.dp)` — colored circle + white icon.
- `@Composable fun EmptyState(lottieAsset: String?, @StringRes title, @StringRes cta?, onCta)` — Lottie with static-icon fallback.
- `@Composable fun PinDots(filled: Int, total: Int = 4, error: Boolean)`.
- `@Composable fun AmountText(amountBase: Double, currency: Currency, income: Boolean)`.

- [ ] **Step 1: `res/values/strings.xml` (English, default)** — full key set:

```xml
<resources>
    <string name="app_name">FinanceApp</string>
    <!-- nav -->
    <string name="nav_dashboard">Dashboard</string>
    <string name="nav_transactions">Transactions</string>
    <string name="nav_budgets">Budgets</string>
    <string name="nav_analytics">Analytics</string>
    <string name="nav_settings">Settings</string>
    <!-- onboarding -->
    <string name="onb_welcome_title">Welcome to FinanceApp</string>
    <string name="onb_welcome_subtitle">Track income and expenses, simply.</string>
    <string name="onb_language">Language</string>
    <string name="onb_currency_title">Base currency</string>
    <string name="onb_currency_subtitle">Pick your main currency and set rates.</string>
    <string name="onb_rate_usd">RUB per 1 USD</string>
    <string name="onb_rate_eur">RUB per 1 EUR</string>
    <string name="onb_security_title">Security</string>
    <string name="onb_security_subtitle">Protect the app with a PIN.</string>
    <string name="onb_enable_biometric">Enable biometrics</string>
    <string name="action_skip">Skip</string>
    <string name="action_next">Next</string>
    <string name="action_done">Done</string>
    <!-- lock -->
    <string name="lock_enter_pin">Enter PIN</string>
    <string name="lock_wrong_pin">Wrong PIN</string>
    <string name="lock_locked_seconds">Locked. Try again in %1$d s</string>
    <string name="lock_use_biometric">Use biometrics</string>
    <!-- dashboard -->
    <string name="dash_balance">Total balance</string>
    <string name="dash_income">Income</string>
    <string name="dash_expense">Expense</string>
    <string name="dash_last7">Last 7 days</string>
    <string name="dash_recent">Recent transactions</string>
    <string name="dash_see_all">All transactions</string>
    <string name="dash_add_expense">Expense</string>
    <string name="dash_add_income">Income</string>
    <string name="dash_empty">Add your first transaction</string>
    <!-- transactions -->
    <string name="tx_title">Transactions</string>
    <string name="tx_search">Search</string>
    <string name="tx_filter_all">All</string>
    <string name="tx_filter_income">Income</string>
    <string name="tx_filter_expense">Expense</string>
    <string name="tx_today">Today</string>
    <string name="tx_yesterday">Yesterday</string>
    <string name="tx_empty">No transactions for the selected period</string>
    <string name="tx_deleted">Transaction deleted</string>
    <string name="tx_copied">Transaction copied</string>
    <string name="action_undo">Undo</string>
    <string name="action_edit">Edit</string>
    <string name="action_duplicate">Duplicate</string>
    <string name="action_delete">Delete</string>
    <!-- add/edit -->
    <string name="form_new_tx">New transaction</string>
    <string name="form_edit_tx">Edit transaction</string>
    <string name="form_amount">Amount</string>
    <string name="form_note">Note</string>
    <string name="form_category">Category</string>
    <string name="form_date">Date</string>
    <string name="form_recurring">Recurring</string>
    <string name="form_interval_daily">Daily</string>
    <string name="form_interval_weekly">Weekly</string>
    <string name="form_interval_monthly">Monthly</string>
    <string name="form_interval_yearly">Yearly</string>
    <string name="form_auto_add">Add automatically</string>
    <string name="action_save">Save</string>
    <!-- settings -->
    <string name="set_appearance">Appearance</string>
    <string name="set_theme">Theme</string>
    <string name="set_theme_system">System</string>
    <string name="set_theme_light">Light</string>
    <string name="set_theme_dark">Dark</string>
    <string name="set_scheme">Color scheme</string>
    <string name="set_scheme_purple">Purple</string>
    <string name="set_scheme_orange">Orange</string>
    <string name="set_language">Language</string>
    <string name="set_finance">Finance</string>
    <string name="set_base_currency">Base currency</string>
    <string name="set_security">Security</string>
    <string name="set_change_pin">Change PIN</string>
    <string name="set_biometric">Biometrics</string>
    <!-- placeholders -->
    <string name="ph_budgets">Control spending with budgets</string>
    <string name="ph_analytics">Your statistics will appear here</string>
    <!-- categories -->
    <string name="cat_food">Food &amp; groceries</string>
    <string name="cat_transport">Transport</string>
    <string name="cat_health">Health</string>
    <string name="cat_entertainment">Entertainment</string>
    <string name="cat_clothing">Clothing</string>
    <string name="cat_communication">Communication</string>
    <string name="cat_utilities">Utilities</string>
    <string name="cat_education">Education</string>
    <string name="cat_travel">Travel</string>
    <string name="cat_other">Other</string>
    <string name="cat_salary">Salary</string>
    <string name="cat_freelance">Freelance</string>
    <string name="cat_investments">Investments</string>
    <string name="cat_gifts">Gifts</string>
</resources>
```

- [ ] **Step 2: `res/values-ru/strings.xml` (Russian)** — same keys, values:

```xml
<resources>
    <string name="app_name">FinanceApp</string>
    <string name="nav_dashboard">Главная</string>
    <string name="nav_transactions">Транзакции</string>
    <string name="nav_budgets">Бюджеты</string>
    <string name="nav_analytics">Аналитика</string>
    <string name="nav_settings">Настройки</string>
    <string name="onb_welcome_title">Добро пожаловать в FinanceApp</string>
    <string name="onb_welcome_subtitle">Учитывайте доходы и расходы легко.</string>
    <string name="onb_language">Язык</string>
    <string name="onb_currency_title">Базовая валюта</string>
    <string name="onb_currency_subtitle">Выберите основную валюту и задайте курсы.</string>
    <string name="onb_rate_usd">Рублей за 1 USD</string>
    <string name="onb_rate_eur">Рублей за 1 EUR</string>
    <string name="onb_security_title">Безопасность</string>
    <string name="onb_security_subtitle">Защитите приложение PIN-кодом.</string>
    <string name="onb_enable_biometric">Включить биометрию</string>
    <string name="action_skip">Пропустить</string>
    <string name="action_next">Далее</string>
    <string name="action_done">Готово</string>
    <string name="lock_enter_pin">Введите PIN</string>
    <string name="lock_wrong_pin">Неверный PIN</string>
    <string name="lock_locked_seconds">Заблокировано. Повтор через %1$d с</string>
    <string name="lock_use_biometric">Биометрия</string>
    <string name="dash_balance">Общий баланс</string>
    <string name="dash_income">Доходы</string>
    <string name="dash_expense">Расходы</string>
    <string name="dash_last7">Последние 7 дней</string>
    <string name="dash_recent">Последние транзакции</string>
    <string name="dash_see_all">Все транзакции</string>
    <string name="dash_add_expense">Расход</string>
    <string name="dash_add_income">Доход</string>
    <string name="dash_empty">Добавьте первую транзакцию</string>
    <string name="tx_title">Транзакции</string>
    <string name="tx_search">Поиск</string>
    <string name="tx_filter_all">Все</string>
    <string name="tx_filter_income">Доходы</string>
    <string name="tx_filter_expense">Расходы</string>
    <string name="tx_today">Сегодня</string>
    <string name="tx_yesterday">Вчера</string>
    <string name="tx_empty">Нет транзакций за выбранный период</string>
    <string name="tx_deleted">Транзакция удалена</string>
    <string name="tx_copied">Транзакция скопирована</string>
    <string name="action_undo">Отменить</string>
    <string name="action_edit">Изменить</string>
    <string name="action_duplicate">Дублировать</string>
    <string name="action_delete">Удалить</string>
    <string name="form_new_tx">Новая транзакция</string>
    <string name="form_edit_tx">Изменить транзакцию</string>
    <string name="form_amount">Сумма</string>
    <string name="form_note">Заметка</string>
    <string name="form_category">Категория</string>
    <string name="form_date">Дата</string>
    <string name="form_recurring">Повторяющаяся</string>
    <string name="form_interval_daily">Ежедневно</string>
    <string name="form_interval_weekly">Еженедельно</string>
    <string name="form_interval_monthly">Ежемесячно</string>
    <string name="form_interval_yearly">Ежегодно</string>
    <string name="form_auto_add">Добавлять автоматически</string>
    <string name="action_save">Сохранить</string>
    <string name="set_appearance">Внешний вид</string>
    <string name="set_theme">Тема</string>
    <string name="set_theme_system">Системная</string>
    <string name="set_theme_light">Светлая</string>
    <string name="set_theme_dark">Тёмная</string>
    <string name="set_scheme">Цветовая схема</string>
    <string name="set_scheme_purple">Фиолетовая</string>
    <string name="set_scheme_orange">Оранжевая</string>
    <string name="set_language">Язык</string>
    <string name="set_finance">Финансы</string>
    <string name="set_base_currency">Базовая валюта</string>
    <string name="set_security">Безопасность</string>
    <string name="set_change_pin">Изменить PIN</string>
    <string name="set_biometric">Биометрия</string>
    <string name="ph_budgets">Контролируйте расходы с помощью бюджетов</string>
    <string name="ph_analytics">Здесь появится ваша статистика</string>
    <string name="cat_food">Еда и продукты</string>
    <string name="cat_transport">Транспорт</string>
    <string name="cat_health">Здоровье</string>
    <string name="cat_entertainment">Развлечения</string>
    <string name="cat_clothing">Одежда</string>
    <string name="cat_communication">Связь</string>
    <string name="cat_utilities">Коммунальные</string>
    <string name="cat_education">Образование</string>
    <string name="cat_travel">Путешествия</string>
    <string name="cat_other">Другое</string>
    <string name="cat_salary">Зарплата</string>
    <string name="cat_freelance">Фриланс</string>
    <string name="cat_investments">Инвестиции</string>
    <string name="cat_gifts">Подарки</string>
</resources>
```

- [ ] **Step 3: MaterialIconMap.kt** — `when(name)` mapping the 15 seed icon names + nav icons (`home, receipt_long, savings, bar_chart, settings, restaurant, directions_car, medical_services, sports_esports, checkroom, phone, school, flight, more_horiz, work, laptop, trending_up, card_giftcard, visibility, visibility_off, add, fingerprint`) to `Icons.Filled.*`; default `Icons.Filled.MoreHoriz`.

- [ ] **Step 4: CategoryNames.kt** — `@Composable fun categoryDisplayName(c: Category): String { if (c.isCustom) return c.name; val id = when(c.name){ "cat_food"->R.string.cat_food; ... }; return stringResource(id) }`.

- [ ] **Step 5: Shared components + Animations.kt** — implement `CategoryIcon`, `EmptyState` (LottieAnimation with `RememberLottieComposition`; on failure/absent asset show `materialIcon` in a tinted circle), `PinDots` (row of 4 circles; filled = primary, empty = outline; `error` triggers shake via `Animations.shakeOffset`), `AmountText` (formatted via `CurrencyFormatter`, green/red by `income`). `Animations.kt` provides `shakeOffset(trigger): Dp` (keyframes 0→16→-16→8→0 over 400ms), `staggerDelay(index) = index*60`, `countUp(target): State<Double>` (animate 0→target 600ms EaseOutCubic).

- [ ] **Step 6:** `assembleDebug` compiles. **Commit** `feat: strings(ru/en), icons, category names, shared components (Phase 1 Task 9)`

---

### Task 10: Navigation + MainActivity + locale application

**Files:**
- Create `navigation/Routes.kt`, `FinanceNavHost.kt`, `BottomBar.kt`
- Create `core/utils/LocaleManager.kt`
- Modify `MainActivity.kt` (extend `AppCompatActivity`, apply theme + locale, host NavHost)
- Modify `AndroidManifest.xml` (add `AppLocalesMetadataHolderService` with `autoStoreLocales=true`); Modify `res/values/themes.xml` parent to `Theme.AppCompat.DayNight.NoActionBar`.

**Interfaces:**
- `object Routes { const val SPLASH="splash"; const val ONBOARDING="onboarding"; const val LOCK="lock"; const val DASHBOARD="dashboard"; const val TRANSACTIONS="transactions"; const val BUDGETS="budgets"; const val ANALYTICS="analytics"; const val SETTINGS="settings" }`
- `@Composable fun FinanceNavHost(navController, startDestination)` with slide+fade transitions (300ms).
- `object LocaleManager { fun apply(language: AppLanguage) = AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag)) }`

- [ ] **Step 1:** Add `AppRootViewModel` (`@HiltViewModel`) exposing `settings: StateFlow<AppSettings?>` (null until loaded) and computed `startDestination`.
- [ ] **Step 2:** MainActivity:

```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: AppRootViewModel = hiltViewModel()
            val settings by vm.settings.collectAsStateWithLifecycle()
            val s = settings
            if (s == null) { /* keep splash */ }
            else {
                FinanceTheme(s.themeMode, s.colorScheme) {
                    val nav = rememberNavController()
                    FinanceNavHost(nav, startDestination = when {
                        !s.onboardingCompleted -> Routes.ONBOARDING
                        s.pinHash != null -> Routes.LOCK
                        else -> Routes.DASHBOARD
                    })
                }
            }
        }
    }
}
```
Splash route always shown first (Task 11) then navigates onward; the `startDestination` above is used after Splash completes (Splash reads the same settings to decide its next hop).

- [ ] **Step 3:** `FinanceNavHost` wires all routes; `main` group (dashboard/transactions/budgets/analytics/settings) rendered inside a `Scaffold` with `BottomBar`; add/edit/settings-subroutes are Compose `ModalBottomSheet`/nested screens.
- [ ] **Step 4:** `BottomBar` = `NavigationBar` with 5 items using `materialIcon("home"/"receipt_long"/"savings"/"bar_chart"/"settings")` and `nav_*` labels.
- [ ] **Step 5:** `assembleDebug` compiles; app launches to Splash. **Commit** `feat: navigation graph, bottom bar, locale application (Phase 1 Task 10)`

---

### Task 11: Splash

**Files:** Create `feature/splash/SplashScreen.kt`.
**Behavior:** Full-screen `MaterialTheme.colorScheme.primary` background; centered Lottie logo (asset `assets/lottie/splash.json`) with static `materialIcon` fallback + `app_name`. `LaunchedEffect` waits 1800ms then `onFinished()`; NavHost navigates to `onboarding`/`lock`/`dashboard` per settings (same rule as Task 10 Step 2), popping splash.
- [ ] Implement, `assembleDebug` compiles. **Commit** `feat: splash screen (Phase 1 Task 11)`

---

### Task 12: Onboarding

**Files:** Create `feature/onboarding/OnboardingScreen.kt`, `OnboardingViewModel.kt`.
**Test:** `feature/onboarding/OnboardingViewModelTest.kt`.

**Interfaces:**
- `OnboardingViewModel(settingsRepo, setPin: SetPinUseCase)` state: `data class OnbState(language: AppLanguage, baseCurrency: Currency, rateUsd: String, rateEur: String, pin: String, biometric: Boolean)`; actions `setLanguage`, `setCurrency`, `setRateUsd/Eur`, `setPin`, `toggleBiometric`, `suspend fun finish()` (persists language/currency/rates/biometric, pin if 4 digits, `onboardingCompleted=true`).

- [ ] **Step 1: ViewModel test** — `finish()` with rates "80"/"95" and pin "1234" persists: `settings.first()` has `rateUsd==80.0`, `pinHash==PinHasher.hash("1234")`, `onboardingCompleted`. (Use in-memory `SettingsRepository` fake.)
- [ ] **Step 2:** implement ViewModel (parse rates via `toDoubleOrNull() ?: default`).
- [ ] **Step 3:** `OnboardingScreen` = `HorizontalPager(3)` with progress dots, fade+slide page transition, "Skip" on slide 3. Slide 1: radio `Русский/English` (calls `LocaleManager.apply` immediately). Slide 2: currency chips ₽/$/€ + two `OutlinedTextField` rates (`onb_rate_usd/eur`). Slide 3: 4-digit PIN field + biometric switch (shown only if `BiometricManager.canAuthenticate` succeeds) + "Done". "Done"/"Skip" → `vm.finish()` → navigate `lock` if pin set else `dashboard`.
- [ ] **Step 4:** test PASS, `assembleDebug` compiles. **Commit** `feat: onboarding (Phase 1 Task 12)`

---

### Task 13: Lock (PIN)

**Files:** Create `feature/lock/LockScreen.kt`, `LockViewModel.kt`. **Test:** `LockViewModelTest.kt`.

**Interfaces:**
- `LockViewModel(verifyPin: VerifyPinUseCase)` state: `data class LockState(entered: String, error: Boolean, attempts: Int, lockedUntil: Long)`; `fun onDigit(d)`, `fun onDelete()`, `suspend fun submit(now: Long): Boolean`. On wrong pin: `error=true`, `attempts++`, clear entered; at `attempts>=5` set `lockedUntil = now + 30_000`. `fun remainingLock(now): Long`.

- [ ] **Step 1: ViewModel test** — 5 wrong submissions set `lockedUntil` ≈ now+30000 and `remainingLock>0`; correct pin returns true and resets. (Fake `VerifyPinUseCase` accepting "1234".)
- [ ] **Step 2:** implement ViewModel.
- [ ] **Step 3:** `LockScreen` — `app_name` + icon, `PinDots(entered.length, error=error)` with shake keyframes + `rememberHaptics()(false)` on error; numeric keypad (0–9, delete); auto-submit at 4 digits; biometric button (`fingerprint`) when `settings.biometricEnabled` → `BiometricPrompt` → on success `onUnlocked()`; when locked, disable keypad and show `lock_locked_seconds` countdown. On success → `dashboard` (pop lock).
- [ ] **Step 4:** test PASS, compiles. **Commit** `feat: lock screen with PIN + biometric (Phase 1 Task 13)`

---

### Task 14: Dashboard

**Files:** Create `feature/dashboard/DashboardScreen.kt`, `DashboardViewModel.kt`. **Test:** `DashboardViewModelTest.kt`.

**Interfaces:**
- `DashboardViewModel(getDashboard: GetDashboardDataUseCase, settingsRepo)`: `val state: StateFlow<DashboardData>` (via `getDashboard(System.currentTimeMillis())`), `val baseCurrency: StateFlow<Currency>`, `fun toggleBalanceHidden()`, `val balanceHidden: StateFlow<Boolean>`.

- [ ] **Step 1: ViewModel test** — with a fake use case emitting a fixed `DashboardData`, `state.first()` matches; `toggleBalanceHidden` flips `balanceHidden`.
- [ ] **Step 2:** implement ViewModel.
- [ ] **Step 3:** `DashboardScreen` — top balance card: `AmountText`/`CurrencyFormatter` of `balanceBase` with `countUp` animation and eye toggle (`visibility`/`visibility_off`; hidden shows "••••"); two columns `dash_income`/`dash_expense` (month*). Mini 7-day line chart via Vico (`com.patrykandpatrick.vico.compose.chart.CartesianChartHost` fed from `last7Days`; built-in draw animation). `dash_recent` list of `recent` (staggered `staggerDelay(index)`) using `TransactionRow`. `dash_see_all` button → `transactions`. Empty (`recent.isEmpty()` && balance 0) → `EmptyState(dash_empty)`. Expandable FAB → two mini-FABs `dash_add_expense`(red)/`dash_add_income`(green) opening AddEdit sheet with preset type.
- [ ] **Step 4:** test PASS, compiles. **Commit** `feat: dashboard (Phase 1 Task 14)`

---

### Task 15: Transactions list + filters/search

**Files:** Create `feature/transactions/TransactionsScreen.kt`, `TransactionsViewModel.kt`, and `core/ui/components/TransactionRow.kt`. **Test:** `TransactionsViewModelTest.kt`.

**Interfaces:**
- `TransactionsViewModel(observeTx: ObserveTransactionsUseCase, observeCats: ObserveCategoriesUseCase, delete: DeleteTransactionUseCase, duplicate: DuplicateTransactionUseCase, settingsRepo)`: holds `MutableStateFlow<TransactionFilter>`; `val grouped: StateFlow<Map<Long, List<TransactionWithCategory>>>` (grouped by `DateUtils.startOfDay`); `val categories`; `val baseCurrency`; `fun setTypeFilter`, `setCategoryFilter`, `setCurrencyFilter`, `setPeriod(start,end)`, `setQuery`; `suspend fun delete(id)`, `suspend fun duplicate(id): Long?`.

- [ ] **Step 1: ViewModel test** — seed 3 tx across 2 days; assert `grouped` has 2 keys; set type filter → correct subset; `duplicate(id)` adds a row dated today.
- [ ] **Step 2:** implement ViewModel.
- [ ] **Step 3:** `TransactionsScreen` — top: search `TextField` (`tx_search`), filter chips `tx_filter_all/income/expense`, category dropdown, period selector (week/month/year/custom via `DatePicker`), currency filter. Body: `LazyColumn` with sticky day headers (`DateUtils.dayLabel` → `tx_today`/`tx_yesterday`/date) and `TransactionRow`s. `TransactionRow` in `SwipeToDismissBox`: swipe start→end duplicate (haptic + snackbar `tx_copied` + Undo), end→start delete (confirm dialog + snackbar `tx_deleted` + Undo). Long-press → dropdown (`action_edit/duplicate/delete`). Empty → `EmptyState(tx_empty, cta=dash_add_expense)`. Tap row → AddEdit sheet in edit mode.
- [ ] **Step 4:** test PASS, compiles. **Commit** `feat: transactions list, filters, swipe actions (Phase 1 Task 15)`

---

### Task 16: Add/Edit transaction BottomSheet

**Files:** Create `feature/transactions/AddEditTransactionSheet.kt`, `AddEditTransactionViewModel.kt`. **Test:** `AddEditTransactionViewModelTest.kt`.

**Interfaces:**
- `AddEditTransactionViewModel(save: SaveTransactionUseCase, txRepo, observeCats: ObserveCategoriesUseCase)`: `data class FormState(id: Long?, type: TransactionType, amount: String, currency: Currency, categoryId: Long?, note: String, date: Long, recurring: Boolean, interval: IntervalType, autoAdd: Boolean)`; `fun load(id: Long?)` (edit vs new, default date=now, currency=base), setters, `val categoriesForType: StateFlow<List<Category>>`, `suspend fun save(): Boolean` (validate amount>0 && category selected; build `Transaction`; if recurring, persist a `recurring_rules` row with `template_json` and initial `next_date`).

- [ ] **Step 1: ViewModel test** — new expense, amount "12.5", category set → `save()` true and `txRepo` contains a matching row; amount "0" → `save()` false.
- [ ] **Step 2:** implement ViewModel (serialize recurring template with kotlinx.serialization).
- [ ] **Step 3:** `AddEditTransactionSheet` = `ModalBottomSheet` (spring `dampingRatio=0.8`): animated `form_new_tx`/`form_edit_tx` title; income/expense segmented toggle; large amount `TextField` (numeric); currency selector ₽/$/€; horizontal category scroller (`CategoryIcon` + `categoryDisplayName`, filtered by type); `form_note`; `form_date` `DatePicker`; `form_recurring` checkbox → interval chips + `form_auto_add` switch; `action_save` button (`rememberHaptics()(true)` on success) closes sheet.
- [ ] **Step 4:** test PASS, compiles. **Commit** `feat: add/edit transaction bottom sheet (Phase 1 Task 16)`

---

### Task 17: Settings

**Files:** Create `feature/settings/SettingsScreen.kt`, `SettingsViewModel.kt`. **Test:** `SettingsViewModelTest.kt`.

**Interfaces:**
- `SettingsViewModel(settingsRepo, setPin: SetPinUseCase)`: `val settings: StateFlow<AppSettings>`; `fun setTheme(m)`, `setScheme(s)`, `setLanguage(l)` (also `LocaleManager.apply` + recreate), `setBaseCurrency(c)`, `setRates(usd,eur)`, `setBiometric(b)`, `suspend fun changePin(pin)`.

- [ ] **Step 1: ViewModel test** — `setScheme(ORANGE)` then `settings.first().colorScheme==ORANGE`; `changePin("4321")` sets hash.
- [ ] **Step 2:** implement ViewModel.
- [ ] **Step 3:** `SettingsScreen` — sections: **Appearance** (`set_theme` radios system/light/dark; `set_scheme` two preview cards purple/orange), **Language** (`Русский/English`), **Finance** (`set_base_currency` chips + two rate fields), **Security** (`set_change_pin` → 4-digit dialog; `set_biometric` switch). All bound to ViewModel; changes persist live and theme/scheme update immediately.
- [ ] **Step 4:** test PASS, compiles. **Commit** `feat: settings screen (Phase 1 Task 17)`

---

### Task 18: Placeholder screens + final APK build & smoke verification

**Files:** Create `feature/placeholder/PlaceholderScreen.kt` (used for `budgets`, `analytics`). Add `assets/lottie/splash.json` (simple provided coin/graph animation) — fallback already handled.

- [ ] **Step 1:** `PlaceholderScreen(@StringRes message)` = centered `EmptyState` (`ph_budgets` / `ph_analytics`). Wire into NavHost tabs.
- [ ] **Step 2: Full build + unit tests**

```bash
source /f/android-dev/env.sh
./gradlew clean :app:testDebugUnitTest :app:assembleDebug
```
Expected: all unit tests PASS; `app/build/outputs/apk/debug/app-debug.apk` exists.

- [ ] **Step 3: Manual smoke checklist** (run APK on a device/emulator when available — not in this environment):
  - First launch → Splash → Onboarding; pick RU, base ₽, rates, set PIN 1234 → Lock → enter 1234 → Dashboard.
  - Add expense & income via FAB; balance CountUp + recent list update; 7-day chart draws.
  - Transactions: swipe delete (undo), swipe duplicate (snackbar), edit via tap; filters + search work.
  - Settings: switch Light/Dark, Purple/Orange, RU/EN — UI updates live; change PIN; toggle biometric.
  - Budgets/Analytics tabs show placeholders. Kill & relaunch → data persists; Lock requires PIN.
- [ ] **Step 4: Release build (unsigned/debug-signed) sanity**

```bash
./gradlew :app:assembleRelease
```
Expected: BUILD SUCCESSFUL (ProGuard rules keep Room/Hilt/Lottie/Vico). If shrinker errors, extend `proguard-rules.pro` accordingly.
- [ ] **Step 5: Commit** `feat: placeholders + Phase 1 build verification`

---

## Self-Review

**1. Spec coverage (Phase 1 scope):** Splash (T11), Onboarding 3 slides + language/currency/rates/PIN (T12), Lock PIN+shake+lockout+biometric (T13), Dashboard balance/CountUp/hide/month/7-day chart/recent/FAB (T14), Transactions group/swipe/filters/search/empty (T15), Add/Edit sheet + recurring (T16), Settings appearance/language/finance/security (T17), Budgets/Analytics placeholders (T18), Room 5 tables + seed (T3), DataStore all keys (T4), themes purple/orange light+dark (T8), ru/en + switching (T9/T10), Clean Arch layers (T1–T7). Deferred items (budgets logic, analytics charts, reminders/WorkManager, backup/SAF, custom-category UI, full Lottie set, release hardening) are explicitly out of Phase 1 per spec §5 and scheduled in later phases.

**2. Placeholder scan:** No "TODO/TBD". UI-composable bodies are intentionally spec'd (state + components + string keys) per the fidelity note, not left vague; each has a concrete build/test gate.

**3. Type consistency:** Enum/`AppSettings`/repository/use-case signatures declared once in Tasks 1/5/7 and reused verbatim downstream. Rates semantics fixed (RUB-per-USD/EUR, pivot through RUB) in T2 and consumed identically by T7/T12/T17. Category `name` = string-resource key convention set in T3 and resolved in T9.

**4. Ambiguity check:** Currency conversion, day-grouping, and start-destination rules are pinned to exact functions. Language application uses AppCompat per-app locales (T10) consistently.

## Execution Handoff — see message.

