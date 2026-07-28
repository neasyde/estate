package com.financeapp.core.ui.anim

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Material 3 Expressive-style tactile feedback: springs the element down slightly
 * while pressed. Pair with `clickable(interactionSource = src, ...)`.
 */
@Composable
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.98f,
): Modifier {
    if (reducedMotion()) return this
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = Motion.springPress,
        label = "pressScale",
    )
    return this.graphicsLayer { scaleX = scale; scaleY = scale }
}
