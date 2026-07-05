package com.financeapp.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
import com.financeapp.core.ui.anim.Reveal
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.ui.icons.materialIcon
import com.financeapp.core.ui.theme.IndigoPrimary
import com.financeapp.core.ui.theme.IndigoSecondary
import com.financeapp.core.ui.theme.OrangeGradEnd
import com.financeapp.core.ui.theme.OrangeGradStart
import com.financeapp.core.ui.theme.PillShape
import com.financeapp.core.ui.theme.PurpleGradEnd
import com.financeapp.core.ui.theme.PurpleGradStart
import com.financeapp.core.ui.theme.TerracottaPrimary
import com.financeapp.core.ui.theme.TerracottaSecondary
import com.financeapp.core.utils.LocaleManager

@Composable
fun SettingsScreen(
    onManageCategories: () -> Unit,
    onManageReminders: () -> Unit,
    vm: SettingsViewModel = hiltViewModel(),
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val backupEvent by vm.backupEvent.collectAsStateWithLifecycle()
    val ratesEvent by vm.ratesEvent.collectAsStateWithLifecycle()
    val ratesLoading by vm.ratesLoading.collectAsStateWithLifecycle()
    var showPinDialog by remember { mutableStateOf(false) }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let(vm::exportBackup) }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let(vm::importBackup) }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.nav_more),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(16.dp))

        Reveal(0) {
            SettingsGroup(stringResource(R.string.set_appearance)) {
                PillSelector(
                    options = listOf(
                        ThemeMode.SYSTEM to stringResource(R.string.set_theme_system),
                        ThemeMode.LIGHT to stringResource(R.string.set_theme_light),
                        ThemeMode.DARK to stringResource(R.string.set_theme_dark),
                    ),
                    selected = settings.themeMode,
                    onSelect = vm::setTheme,
                )
                Spacer(Modifier.height(16.dp))
                Eyebrow(stringResource(R.string.set_scheme))
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SchemeSwatch(
                        label = stringResource(R.string.set_scheme_purple),
                        brush = Brush.linearGradient(listOf(PurpleGradStart, PurpleGradEnd)),
                        selected = settings.colorScheme == ColorScheme.PURPLE,
                        modifier = Modifier.weight(1f),
                    ) { vm.setScheme(ColorScheme.PURPLE) }
                    SchemeSwatch(
                        label = stringResource(R.string.set_scheme_orange),
                        brush = Brush.linearGradient(listOf(OrangeGradStart, OrangeGradEnd)),
                        selected = settings.colorScheme == ColorScheme.ORANGE,
                        modifier = Modifier.weight(1f),
                    ) { vm.setScheme(ColorScheme.ORANGE) }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SchemeSwatch(
                        label = stringResource(R.string.set_scheme_indigo),
                        brush = Brush.linearGradient(listOf(IndigoPrimary, IndigoSecondary)),
                        selected = settings.colorScheme == ColorScheme.INDIGO,
                        modifier = Modifier.weight(1f),
                    ) { vm.setScheme(ColorScheme.INDIGO) }
                    SchemeSwatch(
                        label = stringResource(R.string.set_scheme_terracotta),
                        brush = Brush.linearGradient(listOf(TerracottaPrimary, TerracottaSecondary)),
                        selected = settings.colorScheme == ColorScheme.TERRACOTTA,
                        modifier = Modifier.weight(1f),
                    ) { vm.setScheme(ColorScheme.TERRACOTTA) }
                }
            }
        }
        GroupGap()

        Reveal(1) {
            SettingsGroup(stringResource(R.string.set_typography)) {
                Eyebrow(stringResource(R.string.set_font_family))
                Spacer(Modifier.height(10.dp))
                PillSelector(
                    options = listOf(
                        AppFont.BRANDED to stringResource(R.string.set_font_branded),
                        AppFont.SYSTEM to stringResource(R.string.set_font_system),
                    ),
                    selected = settings.appFont,
                    onSelect = vm::setAppFont,
                )
                Spacer(Modifier.height(16.dp))
                Eyebrow(stringResource(R.string.set_font_size))
                Spacer(Modifier.height(10.dp))
                PillSelector(
                    options = listOf(
                        AppFontSize.SMALL to stringResource(R.string.set_font_size_s),
                        AppFontSize.MEDIUM to stringResource(R.string.set_font_size_m),
                        AppFontSize.LARGE to stringResource(R.string.set_font_size_l),
                        AppFontSize.HUGE to stringResource(R.string.set_font_size_xl),
                    ),
                    selected = settings.fontSize,
                    onSelect = vm::setFontSize,
                )
                Spacer(Modifier.height(16.dp))
                // Live preview: reflects both the chosen typeface and text size instantly.
                Text(
                    text = stringResource(R.string.set_font_preview),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        GroupGap()

        Reveal(1) {
            SettingsGroup(stringResource(R.string.set_display)) {
                SettingSwitch("tune", stringResource(R.string.set_show_decimals), settings.showDecimals, vm::setShowDecimals)
                SettingSwitch("visibility_off", stringResource(R.string.set_hide_balance), settings.hideBalanceByDefault, vm::setHideBalance)
                Spacer(Modifier.height(12.dp))
                Eyebrow(stringResource(R.string.set_default_tx))
                Spacer(Modifier.height(10.dp))
                PillSelector(
                    options = listOf(
                        TransactionType.EXPENSE to stringResource(R.string.dash_add_expense),
                        TransactionType.INCOME to stringResource(R.string.dash_add_income),
                    ),
                    selected = settings.defaultTxType,
                    onSelect = vm::setDefaultTxType,
                )
            }
        }
        GroupGap()

        Reveal(1) {
            SettingsGroup(stringResource(R.string.set_language)) {
                PillSelector(
                    options = listOf(
                        AppLanguage.RU to "Русский",
                        AppLanguage.EN to "English",
                    ),
                    selected = settings.language,
                    onSelect = { vm.setLanguage(it); LocaleManager.apply(it) },
                )
            }
        }
        GroupGap()

        Reveal(2) {
            var manualOpen by remember { mutableStateOf(false) }
            SettingsGroup(stringResource(R.string.set_finance)) {
                Eyebrow(stringResource(R.string.set_base_currency))
                Spacer(Modifier.height(10.dp))
                PillSelector(
                    options = Currency.entries.map { it to "${it.symbol} ${it.code}" },
                    selected = settings.baseCurrency,
                    onSelect = vm::setBaseCurrency,
                )
                Spacer(Modifier.height(6.dp))
                SettingSwitch("autorenew", stringResource(R.string.set_auto_refresh), settings.autoRefreshRates, vm::setAutoRefresh)
                if (settings.autoRefreshRates) {
                    Spacer(Modifier.height(10.dp))
                    PillSelector(
                        options = listOf(
                            12 to stringResource(R.string.set_interval_12h),
                            24 to stringResource(R.string.set_interval_24h),
                        ),
                        selected = settings.ratesIntervalHours,
                        onSelect = vm::setRatesInterval,
                    )
                }
                Spacer(Modifier.height(16.dp))
                ApiKeyField(settings.exchangeApiKey.orEmpty(), onChange = vm::setApiKey)
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.set_api_key_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                RefreshRatesButton(
                    updatedAt = settings.ratesUpdatedAt,
                    loading = ratesLoading,
                    onClick = { vm.refreshRates() },
                )
                Spacer(Modifier.height(14.dp))
                DisclosureRow(stringResource(R.string.set_rates_manual), manualOpen) { manualOpen = !manualOpen }
                if (manualOpen) {
                    Spacer(Modifier.height(10.dp))
                    RateField(stringResource(R.string.onb_rate_usd), settings.rateUsd) { vm.setRates(it, settings.rateEur) }
                    Spacer(Modifier.height(10.dp))
                    RateField(stringResource(R.string.onb_rate_eur), settings.rateEur) { vm.setRates(settings.rateUsd, it) }
                }
            }
        }
        GroupGap()

        Reveal(3) {
            SettingsGroup(stringResource(R.string.set_feedback)) {
                SettingSwitch("animation", stringResource(R.string.set_animations), settings.animationsEnabled, vm::setAnimations)
                SettingSwitch("vibration", stringResource(R.string.set_haptics), settings.hapticsEnabled, vm::setHaptics)
            }
        }
        GroupGap()

        Reveal(4) {
            SettingsGroup(stringResource(R.string.set_security)) {
                ActionRow("lock", stringResource(R.string.set_change_pin)) { showPinDialog = true }
                SettingSwitch("fingerprint", stringResource(R.string.set_biometric), settings.biometricEnabled, vm::setBiometric)
                SettingSwitch("fingerprint", stringResource(R.string.set_auto_biometric), settings.autoBiometric, vm::setAutoBiometric)
                SettingSwitch("lock", stringResource(R.string.set_require_pin), settings.requirePinOnLaunch, vm::setRequirePin)
                Spacer(Modifier.height(12.dp))
                Eyebrow(stringResource(R.string.set_auto_lock))
                Spacer(Modifier.height(10.dp))
                PillSelector(
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
        GroupGap()

        Reveal(5) {
            SettingsGroup(stringResource(R.string.cat_manage_title)) {
                ActionRow("edit", stringResource(R.string.cat_manage_title), onClick = onManageCategories)
                ActionRow("notifications", stringResource(R.string.rem_title), onClick = onManageReminders)
                Spacer(Modifier.height(12.dp))
                StepperRow(
                    icon = "schedule",
                    label = stringResource(R.string.set_reminder_time),
                    value = settings.defaultReminderHour,
                    range = 0..23,
                    format = { "%02d:00".format(it) },
                    onChange = vm::setReminderHour,
                )
                StepperRow(
                    icon = "notifications",
                    label = stringResource(R.string.set_reminder_lead),
                    value = settings.defaultReminderLeadDays,
                    range = 0..14,
                    format = { it.toString() },
                    onChange = vm::setReminderLeadDays,
                )
            }
        }
        GroupGap()

        Reveal(6) {
            SettingsGroup(stringResource(R.string.set_data)) {
                ActionRow("download", stringResource(R.string.set_backup)) {
                    exportLauncher.launch("estate-backup.json")
                }
                ActionRow("upload", stringResource(R.string.set_restore)) {
                    showRestoreConfirm = true
                }
                DangerRow("delete_forever", stringResource(R.string.set_clear_data)) { showClearConfirm = true }
            }
        }
        GroupGap()

        Reveal(7) {
            SettingsGroup(stringResource(R.string.set_about)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Icon(materialIcon("info"), null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(12.dp))
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text(
                        stringResource(R.string.set_version, BuildConfig.VERSION_NAME),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(Modifier.height(28.dp))
    }

    if (showPinDialog) {
        ChangePinDialog(onDismiss = { showPinDialog = false }, onConfirm = { vm.changePin(it); showPinDialog = false })
    }

    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
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
            confirmButton = {
                TextButton(onClick = { vm.clearBackupEvent() }) { Text(stringResource(R.string.action_ok)) }
            },
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
            confirmButton = {
                TextButton(onClick = { vm.clearRatesEvent() }) { Text(stringResource(R.string.action_ok)) }
            },
            text = { Text(message) },
        )
    }
}

@Composable
private fun ApiKeyField(value: String, onChange: (String) -> Unit) {
    var text by remember(value) { mutableStateOf(value) }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it; onChange(it) },
        label = { Text(stringResource(R.string.set_api_key)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun RefreshRatesButton(updatedAt: Long, loading: Boolean, onClick: () -> Unit) {
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(PillShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(enabled = !loading) { onClick() }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Icon(
                    materialIcon("refresh"),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.size(8.dp))
            Text(
                stringResource(R.string.set_refresh_rates),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Spacer(Modifier.height(8.dp))
        val label = if (updatedAt <= 0L) {
            stringResource(R.string.set_rates_never)
        } else {
            val stamp = remember(updatedAt) {
                SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(updatedAt))
            }
            stringResource(R.string.set_rates_updated, stamp)
        }
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SettingSwitch(icon: String, label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    ) {
        Icon(materialIcon(icon), null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.size(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    ) {
        Icon(materialIcon(icon), null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.size(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        TextButton(onClick = { if (value > range.first) onChange(value - 1) }, enabled = value > range.first) { Text("−") }
        Text(
            format(value),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        TextButton(onClick = { if (value < range.last) onChange(value + 1) }, enabled = value < range.last) { Text("+") }
    }
}

@Composable
private fun DangerRow(icon: String, label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).clickable { onClick() }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(materialIcon(icon), null, tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.size(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DisclosureRow(label: String, expanded: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).clickable { onToggle() }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Eyebrow(label, modifier = Modifier.weight(1f))
        Icon(
            materialIcon(if (expanded) "expand_less" else "expand_more"),
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GroupGap() = Spacer(Modifier.height(26.dp))

@Composable
private fun SettingsGroup(eyebrow: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Eyebrow(eyebrow, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun <T> PillSelector(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(PillShape).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (value, label) ->
            val active = value == selected
            Box(
                Modifier.weight(1f).clip(PillShape)
                    .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelect(value) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SchemeSwatch(label: String, brush: Brush, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Column(
        modifier
            .clip(MaterialTheme.shapes.large)
            .border(if (selected) 2.dp else 1.dp, borderColor, MaterialTheme.shapes.large)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(brush))
        Spacer(Modifier.height(10.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ActionRow(icon: String, label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).clickable { onClick() }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(materialIcon(icon), null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.size(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(materialIcon("chevron_right"), null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RateField(label: String, value: Double, onChange: (Double) -> Unit) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            it.toDoubleOrNull()?.let(onChange)
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
    )
}

@Composable
private fun ChangePinDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
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
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
