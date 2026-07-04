package com.financeapp.feature.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.R
import com.financeapp.core.domain.model.AppLanguage
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.ui.icons.materialIcon
import com.financeapp.core.utils.LocaleManager
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onDone: (hasPin: Boolean) -> Unit,
    vm: OnboardingViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val pager = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { page ->
            when (page) {
                0 -> SlideWelcome(state.language, onLang = { vm.setLanguage(it); LocaleManager.apply(it) })
                1 -> SlideCurrency(
                    currency = state.baseCurrency, rateUsd = state.rateUsd, rateEur = state.rateEur,
                    onCurrency = vm::setCurrency, onUsd = vm::setRateUsd, onEur = vm::setRateEur,
                )
                else -> SlideSecurity(
                    pin = state.pin, biometric = state.biometric,
                    onPin = vm::setPin, onBiometric = vm::toggleBiometric,
                )
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(3) { i ->
                val on = pager.currentPage == i
                Box(
                    Modifier.padding(4.dp).size(if (on) 10.dp else 8.dp).clip(CircleShape)
                        .background(
                            if (on) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                        ),
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { scope.launch { vm.finish(); onDone(state.pin.length == 4) } }) {
                Text(stringResource(R.string.action_skip))
            }
            Button(onClick = {
                if (pager.currentPage < 2) {
                    scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                } else {
                    scope.launch { vm.finish(); onDone(state.pin.length == 4) }
                }
            }) {
                Text(stringResource(if (pager.currentPage < 2) R.string.action_next else R.string.action_done))
            }
        }
    }
}

@Composable
private fun SlideWelcome(language: AppLanguage, onLang: (AppLanguage) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(24.dp))
        Icon(materialIcon("wallet"), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(88.dp))
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.onb_welcome_title), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.onb_welcome_subtitle), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))
        Text(stringResource(R.string.onb_language), style = MaterialTheme.typography.titleMedium)
        RadioRow("Русский", language == AppLanguage.RU) { onLang(AppLanguage.RU) }
        RadioRow("English", language == AppLanguage.EN) { onLang(AppLanguage.EN) }
    }
}

@Composable
private fun SlideCurrency(
    currency: Currency, rateUsd: String, rateEur: String,
    onCurrency: (Currency) -> Unit, onUsd: (String) -> Unit, onEur: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.onb_currency_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.onb_currency_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Currency.entries.forEach { c ->
                val sel = c == currency
                if (sel) {
                    Button(onClick = { onCurrency(c) }, modifier = Modifier.weight(1f)) {
                        Text("${c.symbol} ${c.code}")
                    }
                } else {
                    OutlinedButton(onClick = { onCurrency(c) }, modifier = Modifier.weight(1f)) {
                        Text("${c.symbol} ${c.code}")
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.onb_rates_auto),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = rateUsd, onValueChange = onUsd, modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.onb_rate_usd)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = rateEur, onValueChange = onEur, modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.onb_rate_eur)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}

@Composable
private fun SlideSecurity(
    pin: String, biometric: Boolean,
    onPin: (String) -> Unit, onBiometric: (Boolean) -> Unit,
) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(24.dp))
        Icon(materialIcon("fingerprint"), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.onb_security_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.onb_security_subtitle), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = pin, onValueChange = onPin,
            label = { Text(stringResource(R.string.lock_enter_pin)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        )
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.onb_enable_biometric))
            Spacer(Modifier.weight(1f))
            Switch(checked = biometric, onCheckedChange = onBiometric)
        }
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().selectable(selected = selected, onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.size(8.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
