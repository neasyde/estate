package com.financeapp.core.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
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
    "warning" -> Icons.Filled.Warning
    "fastfood" -> Icons.Filled.Fastfood
    "local_cafe" -> Icons.Filled.LocalCafe
    "local_bar" -> Icons.Filled.LocalBar
    "fitness_center" -> Icons.Filled.FitnessCenter
    "pets" -> Icons.Filled.Pets
    "movie" -> Icons.Filled.Movie
    "music_note" -> Icons.Filled.MusicNote
    "local_gas_station" -> Icons.Filled.LocalGasStation
    "bolt" -> Icons.Filled.Bolt
    "wifi" -> Icons.Filled.Wifi
    "credit_card" -> Icons.Filled.CreditCard
    "account_balance" -> Icons.Filled.AccountBalance
    "redeem" -> Icons.Filled.Redeem
    "celebration" -> Icons.Filled.Celebration
    "spa" -> Icons.Filled.Spa
    "park" -> Icons.Filled.Park
    "directions_bus" -> Icons.Filled.DirectionsBus
    "train" -> Icons.Filled.Train
    "local_hospital" -> Icons.Filled.LocalHospital
    "menu_book" -> Icons.Filled.MenuBook
    "sports_soccer" -> Icons.Filled.SportsSoccer
    "brush" -> Icons.Filled.Brush
    "build" -> Icons.Filled.Build
    "cake" -> Icons.Filled.Cake
    "computer" -> Icons.Filled.Computer
    "headphones" -> Icons.Filled.Headphones
    "favorite" -> Icons.Filled.Favorite
    "star" -> Icons.Filled.Star
    "water_drop" -> Icons.Filled.WaterDrop
    else -> Icons.Filled.MoreHoriz
}

/** Icon names offered in the category icon picker. */
val pickableIcons: List<String> = listOf(
    "restaurant", "fastfood", "local_cafe", "local_bar", "directions_car", "directions_bus",
    "train", "local_gas_station", "medical_services", "local_hospital", "fitness_center", "spa",
    "sports_esports", "sports_soccer", "movie", "music_note", "headphones", "celebration",
    "checkroom", "phone", "wifi", "home", "bolt", "water_drop", "school", "menu_book",
    "flight", "park", "pets", "cake", "redeem", "card_giftcard", "work", "laptop", "computer",
    "trending_up", "account_balance", "credit_card", "savings", "wallet", "build", "brush",
    "favorite", "star", "more_horiz",
)
