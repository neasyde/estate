package com.financeapp.navigation

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.financeapp.R
import com.financeapp.core.ui.icons.MaterialIcon
import com.financeapp.core.ui.icons.symbol
import com.financeapp.core.utils.rememberHaptics

private data class BottomItem(val route: String, val icon: String, @StringRes val label: Int)

private val bottomItems = listOf(
    BottomItem(Routes.DASHBOARD, "home", R.string.nav_dashboard),
    BottomItem(Routes.TRANSACTIONS, "receipt_long", R.string.nav_transactions),
    BottomItem(Routes.BUDGETS, "savings", R.string.nav_budgets),
    BottomItem(Routes.ANALYTICS, "bar_chart", R.string.nav_analytics),
    BottomItem(Routes.MORE_HUB, "apps", R.string.nav_more),
)

@Composable
fun BottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    val haptics = rememberHaptics()
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
    ) {
        bottomItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { if (currentRoute != item.route) haptics(true); onNavigate(item.route) },
                icon = { MaterialIcon(symbol(item.icon), contentDescription = null) },
                label = { Text(stringResource(item.label)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
