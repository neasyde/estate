package com.financeapp.core.ui.icons

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

/** Returns the icon name as-is (kept for backward compatibility with call sites using symbol("...")). */
fun symbol(name: String): String = name

private fun resolveIconId(name: String, packageName: String, resources: android.content.res.Resources): Int =
    resources.getIdentifier("symbol_$name", "drawable", packageName)

@Composable
fun MaterialIcon(
    name: String,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    contentDescription: String? = null,
) {
    val context = LocalContext.current
    val fallbackId = remember { resolveIconId("more_horiz", context.packageName, context.resources) }
    val resId = remember(name) { val r = resolveIconId(name, context.packageName, context.resources); if (r == 0) fallbackId else r }
    val resolvedTint = if (tint == Color.Unspecified) LocalContentColor.current else tint
    Icon(
        painter = painterResource(id = resId),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = resolvedTint,
    )
}

/** Curated icon names offered in the category icon picker (finance-relevant set). */
val pickableIcons: List<String> = listOf(
    "restaurant", "fastfood", "local_cafe", "local_bar",
    "directions_car", "directions_bus", "train", "flight", "local_gas_station", "hotel",
    "fitness_center", "sports_esports", "movie", "music_note", "checkroom",
    "wifi", "computer",
    "home", "apartment", "bolt", "water_drop", "school", "work", "business_center",
    "trending_up", "account_balance", "credit_card", "savings", "wallet", "payments",
    "celebration", "pets", "favorite",
    "local_hospital", "local_pharmacy", "medical_services",
    "park", "eco", "parking",
    "build", "brush", "palette",
    "folder", "description",
    "person", "group",
    "notifications", "flag", "label",
    "analytics", "bar_chart", "category",
    "more_horiz", "star",
)
