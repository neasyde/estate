package com.financeapp.core.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

/** Maps a stored Material icon name to an [ImageVector]; unknown names fall back to more_horiz. */
fun materialIcon(name: String): ImageVector = when (name) {
    "restaurant" -> Icons.Filled.Restaurant
    "directions_car" -> Icons.Filled.DirectionsCar
    "medical_services" -> Icons.Filled.MedicalServices
    "sports_esports" -> Icons.Filled.SportsEsports
    "checkroom" -> Icons.Filled.Checkroom
    "phone" -> Icons.Filled.Phone
    "home" -> Icons.Filled.Home
    "school" -> Icons.Filled.School
    "flight" -> Icons.Filled.Flight
    "work" -> Icons.Filled.Work
    "laptop" -> Icons.Filled.Laptop
    "trending_up" -> Icons.Filled.TrendingUp
    "card_giftcard" -> Icons.Filled.CardGiftcard
    "receipt_long" -> Icons.Filled.ReceiptLong
    "savings" -> Icons.Filled.Savings
    "bar_chart" -> Icons.Filled.BarChart
    "settings" -> Icons.Filled.Settings
    "visibility" -> Icons.Filled.Visibility
    "visibility_off" -> Icons.Filled.VisibilityOff
    "add" -> Icons.Filled.Add
    "fingerprint" -> Icons.Filled.Fingerprint
    "delete" -> Icons.Filled.Delete
    "edit" -> Icons.Filled.Edit
    "content_copy" -> Icons.Filled.ContentCopy
    "search" -> Icons.Filled.Search
    "notifications" -> Icons.Filled.Notifications
    "close" -> Icons.Filled.Close
    "arrow_back" -> Icons.Filled.ArrowBack
    "wallet" -> Icons.Filled.Wallet
    else -> Icons.Filled.MoreHoriz
}
