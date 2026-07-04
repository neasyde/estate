package com.financeapp.feature.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.financeapp.R
import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.ui.anim.Motion
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.navigation.Routes
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(settings: AppSettings?, onFinished: (String) -> Unit) {
    var shown by remember { mutableStateOf(false) }
    var elapsed by remember { mutableStateOf(false) }
    val appear by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(Motion.Long, easing = Motion.Emphasized),
        label = "splashAppear",
    )
    LaunchedEffect(Unit) {
        shown = true
        delay(1800)
        elapsed = true
    }
    LaunchedEffect(elapsed, settings) {
        if (elapsed && settings != null) {
            val dest = when {
                !settings.onboardingCompleted -> Routes.ONBOARDING
                settings.pinHash != null -> Routes.LOCK
                else -> Routes.DASHBOARD
            }
            onFinished(dest)
        }
    }
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                alpha = appear
                val s = 0.92f + 0.08f * appear
                scaleX = s
                scaleY = s
            },
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(14.dp))
            Box(Modifier.width(48.dp).height(2.dp).background(MaterialTheme.colorScheme.primary))
            Spacer(Modifier.height(14.dp))
            Eyebrow(stringResource(R.string.brand_tagline))
        }
    }
}
