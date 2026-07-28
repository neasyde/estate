package com.financeapp.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.R
import com.financeapp.core.domain.model.AppLanguage
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.ui.anim.pressScale
import com.financeapp.core.ui.icons.MaterialIcon
import com.financeapp.core.ui.icons.symbol
import com.financeapp.core.ui.theme.IndigoPrimary
import com.financeapp.core.ui.theme.OrangeGradStart
import com.financeapp.core.ui.theme.PurpleGradStart
import com.financeapp.core.ui.theme.TerracottaPrimary
import com.financeapp.core.utils.DateUtils
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
    var showSkipConfirm by remember { mutableStateOf(false) }

    if (showSkipConfirm) {
        AlertDialog(
            onDismissRequest = { showSkipConfirm = false },
            containerColor = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.onb_skip_title)) },
            text = { Text(stringResource(R.string.onb_skip_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    showSkipConfirm = false
                    scope.launch { vm.finish(); onDone(state.pin.length == 4) }
                }) { Text(stringResource(R.string.onb_skip_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showSkipConfirm = false }) {
                    Text(stringResource(R.string.onb_skip_discard))
                }
            },
        )
    }

    val onbLocaleContext = LocaleManager.contextWithLocale(LocalContext.current, state.language)
    LaunchedEffect(state.language) {
        DateUtils.setLocale(java.util.Locale(state.language.tag))
    }
    CompositionLocalProvider(LocalContext provides onbLocaleContext) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp)) {
        // Progress indicator (dots)
        Row(
            Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(3) { i ->
                val on = pager.currentPage == i
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (on) 12.dp else 5.dp)
                        .clip(CircleShape)
                        .background(
                            if (on) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                        ),
                )
            }
        }

        HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { page ->
            when (page) {
                0 -> SlideWelcome(state.language, onLang = { vm.setLanguage(it) })
                1 -> SlideCurrency(
                    currency = state.baseCurrency,
                    rateUsd = state.rateUsd, rateEur = state.rateEur,
                    rateCny = state.rateCny, rateKzt = state.rateKzt,
                    onCurrency = vm::setCurrency,
                    onUsd = vm::setRateUsd, onEur = vm::setRateEur,
                    onCny = vm::setRateCny, onKzt = vm::setRateKzt,
                )
                else -> SlideSecurity(
                    pin = state.pin,
                    biometric = state.biometric,
                    biometricAvailable = vm.biometricAvailable,
                    onPin = vm::setPin,
                    onBiometric = vm::toggleBiometric,
                )
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = {
                if (vm.hasUnsavedEdits()) {
                    showSkipConfirm = true
                } else {
                    scope.launch { vm.finish(); onDone(state.pin.length == 4) }
                }
            }) {
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
}

@Composable
private fun SlideWelcome(language: AppLanguage, onLang: (AppLanguage) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Spacer(Modifier.height(16.dp))
        // Brand mark in a tinted circle
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            PurpleGradStart.copy(alpha = 0.2f),
                            PurpleGradStart.copy(alpha = 0.05f),
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            MaterialIcon(symbol("wallet"), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.onb_welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.onb_welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(40.dp))
        Text(
            stringResource(R.string.onb_language),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(16.dp))
        LanguageChip(
            label = stringResource(R.string.lang_ru),
            subtitle = if (language == AppLanguage.RU) stringResource(R.string.onb_lang_selected) else null,
            selected = language == AppLanguage.RU,
        ) { onLang(AppLanguage.RU) }
        Spacer(Modifier.height(12.dp))
        LanguageChip(
            label = stringResource(R.string.lang_en),
            subtitle = if (language == AppLanguage.EN) stringResource(R.string.onb_lang_selected) else null,
            selected = language == AppLanguage.EN,
        ) { onLang(AppLanguage.EN) }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun SlideCurrency(
    currency: Currency,
    rateUsd: String, rateEur: String, rateCny: String, rateKzt: String,
    onCurrency: (Currency) -> Unit,
    onUsd: (String) -> Unit, onEur: (String) -> Unit,
    onCny: (String) -> Unit, onKzt: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var showApiKey by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.onb_currency_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.onb_currency_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(28.dp))

        // Currency selector card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 0.dp,
        ) {
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Big colored symbol circle
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(PurpleGradStart, OrangeGradStart),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            currency.symbol,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            currency.localeDisplayName(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            currency.code,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    MaterialIcon(
                        symbol(if (expanded) "expand_less" else "expand_more"),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                AnimatedVisibility(visible = expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Currency.entries.filter { it != currency }.forEach { c ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onCurrency(c); expanded = false }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    c.symbol,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.width(36.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(c.localeDisplayName(), style = MaterialTheme.typography.bodyMedium)
                                    Text(c.code, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (c == currency) {
                                    MaterialIcon(symbol("check"), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        // Exchange rates section
        Text(
            stringResource(R.string.onb_rates_auto),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        RateField(
            label = stringResource(R.string.onb_rate_usd),
            value = rateUsd,
            symbol = "$",
            onValueChange = onUsd,
        )
        Spacer(Modifier.height(12.dp))
        RateField(
            label = stringResource(R.string.onb_rate_eur),
            value = rateEur,
            symbol = "€",
            onValueChange = onEur,
        )
        Spacer(Modifier.height(12.dp))
        RateField(
            label = stringResource(R.string.onb_rate_cny),
            value = rateCny,
            symbol = "¥",
            onValueChange = onCny,
        )
        Spacer(Modifier.height(12.dp))
        RateField(
            label = stringResource(R.string.onb_rate_kzt),
            value = rateKzt,
            symbol = "₸",
            onValueChange = onKzt,
        )
        Spacer(Modifier.height(24.dp))

        // API key (collapsed by default, for power users)
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { showApiKey = !showApiKey }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.onb_api_key_toggle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            MaterialIcon(
                symbol(if (showApiKey) "expand_less" else "expand_more"),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        AnimatedVisibility(visible = showApiKey) {
            Column {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = "", // Placeholder — the key is managed in Settings.
                    onValueChange = { /* Not editable here; only shown for discoverability. */ },
                    label = { Text(stringResource(R.string.set_api_key)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.onb_api_key_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SlideSecurity(
    pin: String,
    biometric: Boolean,
    biometricAvailable: Boolean,
    onPin: (String) -> Unit,
    onBiometric: (Boolean) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            IndigoPrimary.copy(alpha = 0.2f),
                            IndigoPrimary.copy(alpha = 0.05f),
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            MaterialIcon(symbol("fingerprint"), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.onb_security_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.onb_security_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = onPin,
            label = { Text(stringResource(R.string.lock_enter_pin)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(0.6f),
            singleLine = true,
        )
        if (pin.isNotEmpty() && pin.length < 4) {
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.onb_pin_hint, 4 - pin.length),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(32.dp))
        // Biometric toggle — shown only when the device supports it.
        if (biometricAvailable) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 0.dp,
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MaterialIcon(
                        symbol("fingerprint"),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.onb_enable_biometric),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            stringResource(R.string.onb_biometric_sub),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = biometric, onCheckedChange = onBiometric)
                }
            }
        }
    }
}

@Composable
private fun LanguageChip(label: String, subtitle: String? = null, selected: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp),
            )
            .pressScale(interaction),
    ) {
        Box(Modifier.fillMaxSize().padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    ),
                    color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (subtitle != null) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun RateField(label: String, value: String, symbol: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        leadingIcon = {
            Text(
                symbol,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}
