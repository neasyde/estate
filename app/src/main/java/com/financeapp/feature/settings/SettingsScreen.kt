package com.financeapp.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.R
import com.financeapp.core.domain.model.AppLanguage
import com.financeapp.core.domain.model.ColorScheme
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.ThemeMode
import com.financeapp.core.ui.theme.OrangePrimary
import com.financeapp.core.ui.theme.PurplePrimary
import com.financeapp.core.utils.LocaleManager

@Composable
fun SettingsScreen(onManageCategories: () -> Unit, vm: SettingsViewModel = hiltViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    var showPinDialog by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
        SectionTitle(stringResource(R.string.set_appearance))
        LabeledRadio(stringResource(R.string.set_theme_system), settings.themeMode == ThemeMode.SYSTEM) { vm.setTheme(ThemeMode.SYSTEM) }
        LabeledRadio(stringResource(R.string.set_theme_light), settings.themeMode == ThemeMode.LIGHT) { vm.setTheme(ThemeMode.LIGHT) }
        LabeledRadio(stringResource(R.string.set_theme_dark), settings.themeMode == ThemeMode.DARK) { vm.setTheme(ThemeMode.DARK) }
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.set_scheme), style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SchemeCard(stringResource(R.string.set_scheme_purple), PurplePrimary, settings.colorScheme == ColorScheme.PURPLE) { vm.setScheme(ColorScheme.PURPLE) }
            SchemeCard(stringResource(R.string.set_scheme_orange), OrangePrimary, settings.colorScheme == ColorScheme.ORANGE) { vm.setScheme(ColorScheme.ORANGE) }
        }

        SectionDivider()
        SectionTitle(stringResource(R.string.set_language))
        LabeledRadio("Русский", settings.language == AppLanguage.RU) { vm.setLanguage(AppLanguage.RU); LocaleManager.apply(AppLanguage.RU) }
        LabeledRadio("English", settings.language == AppLanguage.EN) { vm.setLanguage(AppLanguage.EN); LocaleManager.apply(AppLanguage.EN) }

        SectionDivider()
        SectionTitle(stringResource(R.string.set_finance))
        Text(stringResource(R.string.set_base_currency), style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Currency.entries.forEach { c ->
                if (settings.baseCurrency == c) {
                    Button(onClick = { vm.setBaseCurrency(c) }, modifier = Modifier.weight(1f)) { Text("${c.symbol} ${c.code}") }
                } else {
                    OutlinedButton(onClick = { vm.setBaseCurrency(c) }, modifier = Modifier.weight(1f)) { Text("${c.symbol} ${c.code}") }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        RateField(stringResource(R.string.onb_rate_usd), settings.rateUsd) { vm.setRates(it, settings.rateEur) }
        Spacer(Modifier.height(8.dp))
        RateField(stringResource(R.string.onb_rate_eur), settings.rateEur) { vm.setRates(settings.rateUsd, it) }

        SectionDivider()
        SectionTitle(stringResource(R.string.set_security))
        OutlinedButton(onClick = { showPinDialog = true }) { Text(stringResource(R.string.set_change_pin)) }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.set_biometric))
            Spacer(Modifier.weight(1f))
            Switch(checked = settings.biometricEnabled, onCheckedChange = { vm.setBiometric(it) })
        }

        SectionDivider()
        SectionTitle(stringResource(R.string.cat_manage_title))
        OutlinedButton(onClick = onManageCategories, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.cat_manage_title))
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showPinDialog) {
        ChangePinDialog(onDismiss = { showPinDialog = false }, onConfirm = { vm.changePin(it); showPinDialog = false })
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun SectionDivider() {
    Spacer(Modifier.height(16.dp))
    HorizontalDivider()
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun LabeledRadio(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().selectable(selected = selected, onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.size(8.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SchemeCard(label: String, color: Color, selected: Boolean, onClick: () -> Unit) {
    val border = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Column(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(if (selected) 2.dp else 1.dp, border, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(color))
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
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
