package com.financeapp.core.ui.anim

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/** The shared "fade + gentle rise" enter used for staggered content across screens. */
fun revealEnter(): EnterTransition =
    fadeIn(tween(Motion.Medium, easing = Motion.Emphasized)) +
        slideInVertically(tween(Motion.Medium, easing = Motion.Emphasized)) { it / 5 }

/**
 * Staggered reveal wrapper: fades + rises [content] into place, delaying by [index] steps so
 * lists/sections cascade. Honors reduced motion (shows content immediately). Single source of
 * truth — replaces the per-screen copies that previously duplicated this logic.
 */
@Composable
fun Reveal(index: Int = 0, content: @Composable () -> Unit) {
    val reduced = reducedMotion()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!reduced) delay(index * Motion.StaggerStep.toLong())
        visible = true
    }
    if (reduced) {
        if (visible) content()
    } else {
        AnimatedVisibility(visible = visible, enter = revealEnter()) { content() }
    }
}
