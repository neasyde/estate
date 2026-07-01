package com.financeapp.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Returns a callback: pass `true` for a success haptic (LongPress),
 * `false` for an error/rejection haptic (TextHandleMove).
 */
@Composable
fun rememberHaptics(): (Boolean) -> Unit {
    val h = LocalHapticFeedback.current
    return { success ->
        h.performHapticFeedback(
            if (success) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
        )
    }
}
