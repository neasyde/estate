package com.financeapp.core.ui.anim

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

object AppAnim {
    const val STAGGER_MS = 60
    const val NAV_MS = 300
    const val PROGRESS_MS = 800
}

/** Animates a value from 0 up to [target] over [durationMillis] (EaseOutCubic). */
@Composable
fun animatedCountUp(target: Double, durationMillis: Int = 600): Double {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(target) {
        anim.animateTo(target.toFloat(), tween(durationMillis, easing = EaseOutCubic))
    }
    return anim.value.toDouble()
}
