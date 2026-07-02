package com.financeapp.feature.placeholder

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.financeapp.core.ui.components.EmptyState

@Composable
fun PlaceholderScreen(iconName: String, message: String) {
    Surface(Modifier.fillMaxSize()) {
        EmptyState(iconName = iconName, title = message)
    }
}
