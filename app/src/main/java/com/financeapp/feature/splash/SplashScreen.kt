package com.financeapp.feature.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.financeapp.R
import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.ui.icons.materialIcon
import com.financeapp.navigation.Routes
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(settings: AppSettings?, onFinished: (String) -> Unit) {
    var elapsed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
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
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(112.dp).clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(materialIcon("wallet"), null, tint = Color.White, modifier = Modifier.size(64.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.app_name),
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}
