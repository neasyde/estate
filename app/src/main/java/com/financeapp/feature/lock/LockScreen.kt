package com.financeapp.feature.lock

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.R
import com.financeapp.core.ui.components.PinDots
import com.financeapp.core.ui.icons.materialIcon
import com.financeapp.core.utils.rememberHaptics
import kotlinx.coroutines.delay

@Composable
fun LockScreen(
    biometricEnabled: Boolean,
    onUnlocked: () -> Unit,
    vm: LockViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val haptics = rememberHaptics()
    val context = androidx.compose.ui.platform.LocalContext.current

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.lockedUntil) {
        while (state.lockedUntil - System.currentTimeMillis() > 0) {
            now = System.currentTimeMillis()
            delay(500)
        }
        now = System.currentTimeMillis()
    }
    val remaining = ((state.lockedUntil - now).coerceAtLeast(0L) / 1000L).toInt() + if (state.lockedUntil > now) 1 else 0
    val locked = state.lockedUntil > now

    LaunchedEffect(state.entered) {
        if (state.entered.length == 4 && !locked) {
            if (vm.submit(System.currentTimeMillis())) {
                haptics(true)
                onUnlocked()
            } else {
                haptics(false)
            }
        }
    }

    val shake = remember { Animatable(0f) }
    LaunchedEffect(state.error) {
        if (state.error) {
            shake.snapTo(0f)
            shake.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0; 16f at 100; -16f at 200; 8f at 300; 0f at 400
                },
            )
        }
    }

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier.size(88.dp).clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(materialIcon("wallet"), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.lock_enter_pin), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))
            PinDots(
                filled = state.entered.length,
                error = state.error,
                modifier = Modifier.offset(x = shake.value.dp),
            )
            Spacer(Modifier.height(16.dp))
            if (locked) {
                Text(stringResource(R.string.lock_locked_seconds, remaining), color = MaterialTheme.colorScheme.error)
            } else if (state.error) {
                Text(stringResource(R.string.lock_wrong_pin), color = MaterialTheme.colorScheme.error)
            } else {
                Spacer(Modifier.height(20.dp))
            }
            Spacer(Modifier.height(24.dp))
            Keypad(
                enabled = !locked,
                biometricEnabled = biometricEnabled && !locked,
                onDigit = { vm.onDigit(it) },
                onDelete = { vm.onDelete() },
                onBiometric = { showBiometricPrompt(context, onUnlocked) },
            )
        }
    }
}

@Composable
private fun Keypad(
    enabled: Boolean,
    biometricEnabled: Boolean,
    onDigit: (Char) -> Unit,
    onDelete: () -> Unit,
    onBiometric: () -> Unit,
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { d -> KeyButton(d) { if (enabled) onDigit(d[0]) } }
            }
            Spacer(Modifier.height(12.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                if (biometricEnabled) {
                    IconButton(onClick = onBiometric) {
                        Icon(materialIcon("fingerprint"), stringResource(R.string.lock_use_biometric), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            KeyButton("0") { if (enabled) onDigit('0') }
            Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                IconButton(onClick = onDelete) { Icon(materialIcon("arrow_back"), stringResource(R.string.action_delete)) }
            }
        }
    }
}

@Composable
private fun KeyButton(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.size(72.dp)) {
        Text(label, style = MaterialTheme.typography.headlineSmall)
    }
}

private fun showBiometricPrompt(context: Context, onSuccess: () -> Unit) {
    val activity = context as? FragmentActivity ?: return
    if (BiometricManager.from(context).canAuthenticate(BIOMETRIC_WEAK) != BiometricManager.BIOMETRIC_SUCCESS) return
    val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(context),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
        },
    )
    prompt.authenticate(
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.app_name))
            .setSubtitle(context.getString(R.string.lock_use_biometric))
            .setNegativeButtonText(context.getString(R.string.action_cancel))
            .setAllowedAuthenticators(BIOMETRIC_WEAK)
            .build(),
    )
}
