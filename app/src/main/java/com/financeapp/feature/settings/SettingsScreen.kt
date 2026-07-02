package com.financeapp.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.R
import com.financeapp.core.domain.model.AppLanguage
import com.financeapp.core.domain.model.ColorScheme
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.ThemeMode
import com.financeapp.core.ui.anim.Motion
import com.financeapp.core.ui.anim.reducedMotion
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.ui.components.SoftCard
import com.financeapp.core.ui.icons.materialIcon
import com.financeapp.core.ui.theme.OrangeGradEnd
import com.financeapp.core.ui.theme.OrangeGradStart
import com.financeapp.core.ui.theme.PillShape
import com.financeapp.core.ui.theme.PurpleGradEnd
import com.financeapp.core.ui.theme.PurpleGradStart
import com.financeapp.core.utils.LocaleManager
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(onManageCategories: () -> Unit, vm: SettingsViewModel = hiltViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    var showPinDialog by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.nav_settings),
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
            SettingsGroup(stringResource(R.string.set_finance)) {
                Eyebrow(stringResource(R.string.set_base_currency))
                Spacer(Modifier.height(10.dp))
                PillSelector(
                    options = Currency.entries.map { it to "${it.symbol} ${it.code}" },
                    selected = settings.baseCurrency,
                    onSelect = vm::setBaseCurrency,
                )
                Spacer(Modifier.height(16.dp))
                RateField(stringResource(R.string.onb_rate_usd), settings.rateUsd) { vm.setRates(it, settings.rateEur) }
                Spacer(Modifier.height(10.dp))
                RateField(stringResource(R.string.onb_rate_eur), settings.rateEur) { vm.setRates(settings.rateUsd, it) }
            }
        }
        GroupGap()

        Reveal(3) {
            SettingsGroup(stringResource(R.string.set_security)) {
                ActionRow("fingerprint", stringResource(R.string.set_change_pin)) { showPinDialog = true }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Icon(materialIcon("fingerprint"), null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(12.dp))
                    Text(stringResource(R.string.set_biometric), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(checked = settings.biometricEnabled, onCheckedChange = { vm.setBiometric(it) })
                }
            }
        }
        GroupGap()

        Reveal(4) {
            SettingsGroup(stringResource(R.string.cat_manage_title)) {
                ActionRow("edit", stringResource(R.string.cat_manage_title), onClick = onManageCategories)
            }
        }
        Spacer(Modifier.height(28.dp))
    }

    if (showPinDialog) {
        ChangePinDialog(onDismiss = { showPinDialog = false }, onConfirm = { vm.changePin(it); showPinDialog = false })
    }
}

@Composable
private fun GroupGap() = Spacer(Modifier.height(16.dp))

@Composable
private fun Reveal(index: Int, content: @Composable () -> Unit) {
    val reduced = reducedMotion()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!reduced) delay(index * Motion.StaggerStep.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(Motion.Medium, easing = Motion.Emphasized)) +
            slideInVertically(tween(Motion.Medium, easing = Motion.Emphasized)) { it / 4 },
    ) { content() }
}

@Composable
private fun SettingsGroup(eyebrow: String, content: @Composable ColumnScope.() -> Unit) {
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 8.dp,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
    ) {
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
