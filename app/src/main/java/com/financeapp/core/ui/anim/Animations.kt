package com.financeapp.core.ui.anim

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.financeapp.core.ui.LocalAnimationsEnabled

/** Shared motion language: one emphasized easing + a consistent duration scale. */
object Motion {
    val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    const val Short = 200
    const val Medium = 350
    const val Long = 450
    const val StaggerStep = 55
}

/**
 * True when animations should be suppressed — either the user turned them off in Settings,
 * or the OS animation scale is 0 (developer/accessibility "remove animations").
 */
@Composable
fun reducedMotion(): Boolean {
    if (!LocalAnimationsEnabled.current) return true
    val resolver = LocalContext.current.contentResolver
    val scale = runCatching {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    }.getOrDefault(1f)
    return scale == 0f
}

/** Animates a value from 0 up to [target] over [durationMillis] (emphasized). Respects reduced motion. */
@Composable
fun animatedCountUp(target: Double, durationMillis: Int = 700): Double {
    val reduced = reducedMotion()
    val anim = remember { Animatable(0f) }
    LaunchedEffect(target, reduced) {
        if (reduced) anim.snapTo(target.toFloat())
        else anim.animateTo(target.toFloat(), tween(durationMillis, easing = Motion.Emphasized))
    }
    return anim.value.toDouble()
}
