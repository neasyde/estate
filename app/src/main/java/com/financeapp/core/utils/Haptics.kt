package com.financeapp.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.financeapp.core.ui.LocalHapticsEnabled

/**
 * Returns a callback: pass `true` for a success haptic (LongPress),
 * `false` for an error/rejection haptic (TextHandleMove). No-op when the user
 * disabled haptics in Settings.
 */
@Composable
fun rememberHaptics(): (Boolean) -> Unit {
    val h = LocalHapticFeedback.current
    val enabled = LocalHapticsEnabled.current
    return { success ->
        if (enabled) {
            h.performHapticFeedback(
                if (success) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
            )
        }
    }
}
