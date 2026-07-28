package com.financeapp.feature.settings

import android.text.format.DateUtils as AndroidDateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.BuildConfig
import com.financeapp.R
import com.financeapp.core.domain.model.AppFont
import com.financeapp.core.domain.model.AppFontSize
import com.financeapp.core.domain.model.AppLanguage
import com.financeapp.core.domain.model.AutoLock
import com.financeapp.core.domain.model.ColorScheme
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.ThemeMode
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.ui.anim.pressScale
import com.financeapp.core.ui.anim.Motion
import com.financeapp.core.ui.anim.reducedMotion
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.ui.components.Hairline
import com.financeapp.core.ui.components.SoftCard
import com.financeapp.core.ui.icons.MaterialIcon
import com.financeapp.core.ui.icons.symbol
import com.financeapp.core.ui.theme.IndigoPrimary
import com.financeapp.core.ui.theme.IndigoSecondary
import com.financeapp.core.ui.theme.LocalBrandType
import com.financeapp.core.ui.theme.OrangeGradEnd
import com.financeapp.core.ui.theme.OrangeGradStart
import com.financeapp.core.ui.theme.PaperLight
import com.financeapp.core.ui.theme.PaperDark
import com.financeapp.core.ui.theme.PurpleGradEnd
import com.financeapp.core.ui.theme.PurpleGradStart
import com.financeapp.core.ui.theme.SurfaceDark
import com.financeapp.core.ui.theme.SurfaceLight
import com.financeapp.core.ui.theme.TerracottaPrimary
import com.financeapp.core.ui.theme.TerracottaSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    vm: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val settings by vm.settings.collectAsStateWithLifecycle()
    val backupEvent by vm.backupEvent.collectAsStateWithLifecycle()
    val ratesEvent by vm.ratesEvent.collectAsStateWithLifecycle()
    val ratesLoading by vm.ratesLoading.collectAsStateWithLifecycle()
    var showPinDialog by remember { mutableStateOf(false) }
    val biometricAvailable = remember {
        val result = BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        result != BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE &&
            result != BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
    }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let(vm::exportBackup) }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let(vm::importBackup) }

    // Accordion state — by default only the first section is open.
    val openSections = remember {
        mutableStateMapOf(
            "appearance" to true,
            "display" to false,
            "dark" to false,
            "money" to false,
            "security" to false,
            "data" to false,
            "danger" to false,
            "about" to false,
        )
    }
    fun toggle(key: String) { openSections[key] = !(openSections[key] ?: false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { MaterialIcon(symbol("arrow_back"), contentDescription = stringResource(R.string.action_back)) }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // === Look & feel ===
            SectionHeader(
                key = "appearance",
                title = stringResource(R.string.set_section_appearance),
                icon = "tune",
                isOpen = openSections["appearance"] ?: false,
                onToggle = { toggle("appearance") },
            )
            Spacer(Modifier.height(8.dp))
            SectionBody(visible = openSections["appearance"] ?: false) {
                SoftCard(border = true) {
                    PillSetting(
                        label = stringResource(R.string.set_theme),
                        options = listOf(
                            ThemeMode.SYSTEM to stringResource(R.string.set_theme_system),
                            ThemeMode.LIGHT to stringResource(R.string.set_theme_light),
                            ThemeMode.DARK to stringResource(R.string.set_theme_dark),
                        ),
                        selected = settings.themeMode,
                        onSelect = vm::setTheme,
                    )
                    Hairline()
                    EyebrowLabel(stringResource(R.string.set_scheme))
                    SchemePicker(selected = settings.colorScheme, onSelect = vm::setScheme)
                    Spacer(Modifier.height(12.dp))
                    Hairline()
                    PillSetting(
                        label = stringResource(R.string.set_font_family),
                        options = listOf(
                            AppFont.BRANDED to stringResource(R.string.set_font_branded),
                            AppFont.SYSTEM to stringResource(R.string.set_font_system),
                        ),
                        selected = settings.appFont,
                        onSelect = vm::setAppFont,
                    )
                    Hairline()
                    PillSetting(
                        label = stringResource(R.string.set_font_size),
                        options = listOf(
                            AppFontSize.SMALL to stringResource(R.string.set_font_size_s),
                            AppFontSize.MEDIUM to stringResource(R.string.set_font_size_m),
                            AppFontSize.LARGE to stringResource(R.string.set_font_size_l),
                            AppFontSize.HUGE to stringResource(R.string.set_font_size_xl),
                        ),
                        selected = settings.fontSize,
                        onSelect = vm::setFontSize,
                    )
                    Hairline()
                    FontPreview()
                    Hairline()
                    PillSetting(
                        label = stringResource(R.string.set_language),
                        options = listOf(
                            AppLanguage.RU to stringResource(R.string.lang_ru),
                            AppLanguage.EN to stringResource(R.string.lang_en),
                        ),
                        selected = settings.language,
                        onSelect = vm::setLanguage,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // === Display ===
            SectionHeader(
                key = "display",
                title = stringResource(R.string.set_display),
                icon = "animation",
                isOpen = openSections["display"] ?: false,
                onToggle = { toggle("display") },
            )
            Spacer(Modifier.height(8.dp))
            SectionBody(visible = openSections["display"] ?: false) {
                SoftCard(border = true) {
                    SwitchRow(
                        icon = "vibration",
                        title = stringResource(R.string.set_haptics),
                        subtitle = stringResource(R.string.set_haptics_sub),
                        checked = settings.hapticsEnabled,
                        onCheckedChange = vm::setHaptics,
                    )
                    Hairline()
                    SwitchRow(
                        icon = "animation",
                        title = stringResource(R.string.set_animations),
                        subtitle = stringResource(R.string.set_animations_sub),
                        checked = settings.animationsEnabled,
                        onCheckedChange = vm::setAnimations,
                    )
                    Hairline()
                    SwitchRow(
                        icon = "visibility_off",
                        title = stringResource(R.string.set_hide_balance),
                        subtitle = stringResource(R.string.set_hide_balance_sub),
                        checked = settings.hideBalanceByDefault,
                        onCheckedChange = vm::setHideBalance,
                    )
                    Hairline()
                    SwitchRow(
                        icon = "tune",
                        title = stringResource(R.string.set_show_decimals),
                        subtitle = stringResource(R.string.set_show_decimals_sub),
                        checked = settings.showDecimals,
                        onCheckedChange = vm::setShowDecimals,
                    )
                    Hairline()
                    PillSetting(
                        label = stringResource(R.string.set_default_tx),
                        options = listOf(
                            TransactionType.EXPENSE to stringResource(R.string.dash_add_expense),
                            TransactionType.INCOME to stringResource(R.string.dash_add_income),
                        ),
                        selected = settings.defaultTxType,
                        onSelect = vm::setDefaultTxType,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // === Dark / OLED ===
            SectionHeader(
                key = "dark",
                title = stringResource(R.string.set_theme_dark),
                icon = "dark_mode",
                isOpen = openSections["dark"] ?: false,
                onToggle = { toggle("dark") },
            )
            Spacer(Modifier.height(8.dp))
            SectionBody(visible = openSections["dark"] ?: false) {
                SoftCard(border = true) {
                    SwitchRow(
                        icon = "dark_mode",
                        title = stringResource(R.string.set_amoled_mode),
                        subtitle = stringResource(R.string.set_amoled_sub),
                        checked = settings.amoledMode,
                        onCheckedChange = vm::setAmoledMode,
                    )
                    Hairline()
                    SwitchRow(
                        icon = "schedule",
                        title = stringResource(R.string.set_auto_switch_theme),
                        subtitle = stringResource(R.string.set_auto_switch_sub),
                        checked = settings.autoSwitchTheme,
                        onCheckedChange = vm::setAutoSwitchTheme,
                    )
                    if (reducedMotion()) {
                        if (settings.autoSwitchTheme) {
                            Column {
                                Hairline()
                                StepperRow(
                                    icon = "schedule",
                                    label = stringResource(R.string.set_auto_switch_start),
                                    value = settings.autoSwitchStart,
                                    range = 0..23,
                                    format = { "%02d:00".format(it) },
                                    onChange = vm::setAutoSwitchStart,
                                )
                                Hairline()
                                StepperRow(
                                    icon = "wb_sunny",
                                    label = stringResource(R.string.set_auto_switch_end),
                                    value = settings.autoSwitchEnd,
                                    range = 0..23,
                                    format = { "%02d:00".format(it) },
                                    onChange = vm::setAutoSwitchEnd,
                                )
                            }
                        }
                    } else {
                        AnimatedVisibility(visible = settings.autoSwitchTheme) {
                            Column {
                                Hairline()
                                StepperRow(
                                    icon = "schedule",
                                    label = stringResource(R.string.set_auto_switch_start),
                                    value = settings.autoSwitchStart,
                                    range = 0..23,
                                    format = { "%02d:00".format(it) },
                                    onChange = vm::setAutoSwitchStart,
                                )
                                Hairline()
                                StepperRow(
                                    icon = "wb_sunny",
                                    label = stringResource(R.string.set_auto_switch_end),
                                    value = settings.autoSwitchEnd,
                                    range = 0..23,
                                    format = { "%02d:00".format(it) },
                                    onChange = vm::setAutoSwitchEnd,
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // === Money ===
            SectionHeader(
                key = "money",
                title = stringResource(R.string.set_section_finance),
                icon = "payments",
                isOpen = openSections["money"] ?: false,
                onToggle = { toggle("money") },
            )
            Spacer(Modifier.height(8.dp))
            SectionBody(visible = openSections["money"] ?: false) {
                SoftCard(border = true) {
                    EyebrowLabel(stringResource(R.string.set_base_currency))
                    CurrencyPicker(selected = settings.baseCurrency, onSelect = vm::setBaseCurrency)
                    Spacer(Modifier.height(12.dp))
                    Hairline()
                    SettingRow(
                        icon = "autorenew",
                        title = stringResource(R.string.set_rates_section),
                        subtitle = formatRelativeTime(context, settings.ratesUpdatedAt),
                        onClick = { vm.refreshRates() },
                    )
                    Hairline()
                    SwitchRow(
                        icon = "autorenew",
                        title = stringResource(R.string.set_auto_refresh),
                        subtitle = null,
                        checked = settings.autoRefreshRates,
                        onCheckedChange = vm::setAutoRefresh,
                    )
                    if (reducedMotion()) {
                        if (settings.autoRefreshRates) {
                            Column {
                                Hairline()
                                PillSetting(
                                    label = stringResource(R.string.set_auto_refresh),
                                    options = listOf(
                                        12 to stringResource(R.string.set_interval_12h),
                                        24 to stringResource(R.string.set_interval_24h),
                                    ),
                                    selected = settings.ratesIntervalHours,
                                    onSelect = vm::setRatesInterval,
                                )
                            }
                        }
                    } else {
                        AnimatedVisibility(visible = settings.autoRefreshRates) {
                            Column {
                                Hairline()
                                PillSetting(
                                    label = stringResource(R.string.set_auto_refresh),
                                    options = listOf(
                                        12 to stringResource(R.string.set_interval_12h),
                                        24 to stringResource(R.string.set_interval_24h),
                                    ),
                                    selected = settings.ratesIntervalHours,
                                    onSelect = vm::setRatesInterval,
                                )
                            }
                        }
                    }
                    Hairline()
                    ApiKeyField(settings.exchangeApiKey.orEmpty(), onChange = vm::setApiKey)
                    Text(
                        text = stringResource(R.string.set_api_key_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // === Security ===
            SectionHeader(
                key = "security",
                title = stringResource(R.string.set_section_security),
                icon = "lock",
                isOpen = openSections["security"] ?: false,
                onToggle = { toggle("security") },
            )
            Spacer(Modifier.height(8.dp))
            SectionBody(visible = openSections["security"] ?: false) {
                SoftCard(border = true) {
                    ActionRow(
                        icon = "lock",
                        title = stringResource(R.string.set_change_pin),
                        subtitle = stringResource(R.string.set_change_pin_sub),
                        onClick = { showPinDialog = true },
                    )
                    Hairline()
                    if (biometricAvailable) {
                        SwitchRow(
                            icon = "fingerprint",
                            title = stringResource(R.string.set_biometric),
                            subtitle = stringResource(R.string.set_biometric_sub),
                            checked = settings.biometricEnabled,
                            onCheckedChange = vm::setBiometric,
                        )
                        Hairline()
                    }
                    SwitchRow(
                        icon = "fingerprint",
                        title = stringResource(R.string.set_auto_biometric),
                        subtitle = stringResource(R.string.set_auto_biometric_sub),
                        checked = settings.autoBiometric,
                        onCheckedChange = vm::setAutoBiometric,
                    )
                    Hairline()
                    SwitchRow(
                        icon = "lock",
                        title = stringResource(R.string.set_require_pin),
                        subtitle = null,
                        checked = settings.requirePinOnLaunch,
                        onCheckedChange = vm::setRequirePin,
                    )
                    Hairline()
                    PillSetting(
                        label = stringResource(R.string.set_auto_lock),
                        options = listOf(
                            AutoLock.IMMEDIATE to stringResource(R.string.set_lock_immediate),
                            AutoLock.ONE_MIN to stringResource(R.string.set_lock_1min),
                            AutoLock.FIVE_MIN to stringResource(R.string.set_lock_5min),
                        ),
                        selected = settings.autoLock,
                        onSelect = vm::setAutoLock,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // === Data ===
            SectionHeader(
                key = "data",
                title = stringResource(R.string.set_section_data),
                icon = "storage",
                isOpen = openSections["data"] ?: false,
                onToggle = { toggle("data") },
            )
            Spacer(Modifier.height(8.dp))
            SectionBody(visible = openSections["data"] ?: false) {
                SoftCard(border = true) {
                    ActionRow(
                        icon = "download",
                        title = stringResource(R.string.set_backup),
                        subtitle = null,
                        onClick = { exportLauncher.launch("estate-backup.json") },
                    )
                    Hairline()
                    ActionRow(
                        icon = "upload",
                        title = stringResource(R.string.set_restore),
                        subtitle = null,
                        onClick = { showRestoreConfirm = true },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // === Danger zone ===
            SectionHeader(
                key = "danger",
                title = stringResource(R.string.set_section_danger),
                icon = "warning",
                isOpen = openSections["danger"] ?: false,
                onToggle = { toggle("danger") },
                accent = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.height(8.dp))
            SectionBody(visible = openSections["danger"] ?: false) {
                SoftCard(border = true) {
                    DangerRow(
                        icon = "delete_forever",
                        title = stringResource(R.string.set_clear_data),
                        subtitle = stringResource(R.string.set_clear_data_sub),
                        onClick = { showClearConfirm = true },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // === About ===
            SectionHeader(
                key = "about",
                title = stringResource(R.string.set_section_about),
                icon = "info",
                isOpen = openSections["about"] ?: false,
                onToggle = { toggle("about") },
            )
            Spacer(Modifier.height(8.dp))
            SectionBody(visible = openSections["about"] ?: false) {
                SoftCard(border = true) {
                    AboutRow()
                }
            }

            Spacer(Modifier.height(36.dp))
        }
    }

    if (showPinDialog) {
        ChangePinDialog(onDismiss = { showPinDialog = false }, onConfirm = { vm.changePin(it); showPinDialog = false })
    }
    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            containerColor = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.restore_confirm_title)) },
            text = { Text(stringResource(R.string.restore_confirm_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreConfirm = false
                    importLauncher.launch(arrayOf("application/json"))
                }) { Text(stringResource(R.string.set_restore)) }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.set_clear_title)) },
            text = { Text(stringResource(R.string.set_clear_msg)) },
            confirmButton = {
                TextButton(onClick = { showClearConfirm = false; vm.clearAllData() }) {
                    Text(stringResource(R.string.set_clear_data), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
    backupEvent?.let { event ->
        val message = when (event) {
            BackupEvent.EXPORT_OK -> stringResource(R.string.backup_ok)
            BackupEvent.EXPORT_FAIL -> stringResource(R.string.backup_fail)
            BackupEvent.IMPORT_OK -> stringResource(R.string.restore_ok)
            BackupEvent.IMPORT_FAIL -> stringResource(R.string.restore_fail)
            BackupEvent.CLEARED -> stringResource(R.string.backup_cleared)
            BackupEvent.CLEAR_FAIL -> stringResource(R.string.backup_fail)
        }
        AlertDialog(
            onDismissRequest = { vm.clearBackupEvent() },
            containerColor = MaterialTheme.colorScheme.background,
            confirmButton = { TextButton(onClick = { vm.clearBackupEvent() }) { Text(stringResource(R.string.action_ok)) } },
            text = { Text(message) },
        )
    }
    ratesEvent?.let { event ->
        val message = when (event) {
            RatesEvent.UPDATED -> stringResource(R.string.rates_updated_ok)
            RatesEvent.NO_KEY -> stringResource(R.string.rates_no_key)
            RatesEvent.INVALID_KEY -> stringResource(R.string.rates_invalid_key)
            RatesEvent.FAILED -> stringResource(R.string.rates_failed)
        }
        AlertDialog(
            onDismissRequest = { vm.clearRatesEvent() },
            containerColor = MaterialTheme.colorScheme.background,
            confirmButton = { TextButton(onClick = { vm.clearRatesEvent() }) { Text(stringResource(R.string.action_ok)) } },
            text = { Text(message) },
        )
    }
    if (ratesLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp).padding(top = 4.dp), strokeWidth = 2.dp)
        }
    }
}

/* ----------------------------- Building blocks ----------------------------- */

@Composable
private fun SectionHeader(
    key: String,
    title: String,
    icon: String,
    isOpen: Boolean,
    onToggle: () -> Unit,
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    val interaction = remember { MutableInteractionSource() }
    val reduced = reducedMotion()
    val angle by animateFloatAsState(
        targetValue = if (isOpen) 90f else 0f,
        animationSpec = if (reduced) tween(0) else Motion.springPress,
        label = "chevron",
    )
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onToggle)
            .pressScale(interaction)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(28.dp).clip(CircleShape).background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            MaterialIcon(symbol(icon), tint = accent, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = LocalBrandType.current.title,
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.rotate(angle),
        )
    }
}

@Composable
private fun SectionBody(visible: Boolean, content: @Composable () -> Unit) {
    if (reducedMotion()) {
        if (visible) content()
    } else {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) { content() }
    }
}

@Composable
private fun EyebrowLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun SettingRow(
    icon: String,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {},
) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .pressScale(interaction)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                MaterialIcon(symbol(icon), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        MaterialIcon(symbol("chevron_right"), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SwitchRow(
    icon: String,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            MaterialIcon(symbol(icon), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ActionRow(icon: String, title: String, subtitle: String? = null, onClick: () -> Unit) =
    SettingRow(icon, title, subtitle, onClick)

@Composable
private fun DangerRow(icon: String, title: String, subtitle: String? = null, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .pressScale(interaction)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center,
        ) {
            MaterialIcon(symbol(icon), tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun <T> PillSetting(
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { (value, text) ->
                val active = value == selected
                val interaction = remember { MutableInteractionSource() }
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                        .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable(interactionSource = interaction, indication = null) { onSelect(value) }
                        .pressScale(interaction)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun SchemePicker(selected: ColorScheme, onSelect: (ColorScheme) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SchemeSwatch(stringResource(R.string.set_scheme_purple), Brush.linearGradient(listOf(PurpleGradStart, PurpleGradEnd)), selected == ColorScheme.PURPLE, Modifier.weight(1f)) { onSelect(ColorScheme.PURPLE) }
        SchemeSwatch(stringResource(R.string.set_scheme_orange), Brush.linearGradient(listOf(OrangeGradStart, OrangeGradEnd)), selected == ColorScheme.ORANGE, Modifier.weight(1f)) { onSelect(ColorScheme.ORANGE) }
        SchemeSwatch(stringResource(R.string.set_scheme_indigo), Brush.linearGradient(listOf(IndigoPrimary, IndigoSecondary)), selected == ColorScheme.INDIGO, Modifier.weight(1f)) { onSelect(ColorScheme.INDIGO) }
        SchemeSwatch(stringResource(R.string.set_scheme_terracotta), Brush.linearGradient(listOf(TerracottaPrimary, TerracottaSecondary)), selected == ColorScheme.TERRACOTTA, Modifier.weight(1f)) { onSelect(ColorScheme.TERRACOTTA) }
    }
}

@Composable
private fun SchemeSwatch(label: String, brush: Brush, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(if (selected) 2.dp else 1.dp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(14.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .pressScale(interaction)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(brush))
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun CurrencyPicker(selected: Currency, onSelect: (Currency) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Currency.entries.forEach { c ->
            val isSel = c == selected
            val interaction = remember { MutableInteractionSource() }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .clickable(interactionSource = interaction, indication = null) { onSelect(c) }
                    .pressScale(interaction),
            ) {
                Text(
                    text = "${c.symbol} ${c.code}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun StepperRow(
    icon: String,
    label: String,
    value: Int,
    range: IntRange,
    format: (Int) -> String,
    onChange: (Int) -> Unit,
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            MaterialIcon(symbol(icon), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(format(value), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = { if (value > range.first) onChange(value - 1) }, enabled = value > range.first) {
            Icon(Icons.Filled.Remove, contentDescription = null)
        }
        Text(format(value), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(horizontal = 8.dp))
        IconButton(onClick = { if (value < range.last) onChange(value + 1) }, enabled = value < range.last) {
            Icon(Icons.Filled.Add, contentDescription = null)
        }
    }
}

@Composable
private fun ApiKeyField(value: String, onChange: (String) -> Unit) {
    var text by remember(value) { mutableStateOf(value) }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it; onChange(it) },
        label = { Text(stringResource(R.string.set_api_key)) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    )
}

@Composable
private fun FontPreview() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
    ) {
        Text(text = stringResource(R.string.set_font_preview), style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun AboutRow() {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            MaterialIcon(symbol("info"), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.set_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatRelativeTime(context: android.content.Context, updatedAt: Long): String {
    if (updatedAt <= 0L) return context.getString(R.string.set_rates_never)
    val now = System.currentTimeMillis()
    val rel = AndroidDateUtils.getRelativeTimeSpanString(
        updatedAt,
        now,
        AndroidDateUtils.MINUTE_IN_MILLIS,
    )
    return context.getString(R.string.set_rates_updated, rel)
}

@Composable
private fun ChangePinDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = { Text(stringResource(R.string.set_change_pin)) },
        text = {
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it.filter(Char::isDigit).take(4) },
                label = { Text(stringResource(R.string.lock_enter_pin)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(pin) }, enabled = pin.length == 4) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
