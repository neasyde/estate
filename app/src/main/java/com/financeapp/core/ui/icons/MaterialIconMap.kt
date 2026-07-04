package com.financeapp.core.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Laptop
import androidx.compose.material.icons.rounded.LocalBar
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Redeem
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector

/** Maps a stored Material icon name to a rounded [ImageVector]; unknown names fall back to more_horiz. */
fun materialIcon(name: String): ImageVector = when (name) {
    "restaurant" -> Icons.Rounded.Restaurant
    "directions_car" -> Icons.Rounded.DirectionsCar
    "medical_services" -> Icons.Rounded.MedicalServices
    "sports_esports" -> Icons.Rounded.SportsEsports
    "checkroom" -> Icons.Rounded.Checkroom
    "phone" -> Icons.Rounded.Phone
    "home" -> Icons.Rounded.Home
    "school" -> Icons.Rounded.School
    "flight" -> Icons.Rounded.Flight
    "work" -> Icons.Rounded.Work
    "laptop" -> Icons.Rounded.Laptop
    "trending_up" -> Icons.AutoMirrored.Rounded.TrendingUp
    "card_giftcard" -> Icons.Rounded.CardGiftcard
    "receipt_long" -> Icons.AutoMirrored.Rounded.ReceiptLong
    "savings" -> Icons.Rounded.Savings
    "bar_chart" -> Icons.Rounded.BarChart
    "settings" -> Icons.Rounded.Settings
    "visibility" -> Icons.Rounded.Visibility
    "visibility_off" -> Icons.Rounded.VisibilityOff
    "add" -> Icons.Rounded.Add
    "fingerprint" -> Icons.Rounded.Fingerprint
    "delete" -> Icons.Rounded.Delete
    "edit" -> Icons.Rounded.Edit
    "content_copy" -> Icons.Rounded.ContentCopy
    "search" -> Icons.Rounded.Search
    "notifications" -> Icons.Rounded.Notifications
    "refresh" -> Icons.Rounded.Refresh
    "autorenew" -> Icons.Rounded.Autorenew
    "expand_more" -> Icons.Rounded.ExpandMore
    "expand_less" -> Icons.Rounded.ExpandLess
    "lock" -> Icons.Rounded.Lock
    "palette" -> Icons.Rounded.Palette
    "vibration" -> Icons.Rounded.Vibration
    "animation" -> Icons.Rounded.Animation
    "schedule" -> Icons.Rounded.Schedule
    "info" -> Icons.Rounded.Info
    "tune" -> Icons.Rounded.Tune
    "delete_forever" -> Icons.Rounded.DeleteForever
    "close" -> Icons.Rounded.Close
    "chevron_right" -> Icons.Rounded.ChevronRight
    "download" -> Icons.Rounded.Download
    "upload" -> Icons.Rounded.Upload
    "arrow_back" -> Icons.AutoMirrored.Rounded.ArrowBack
    "arrow_upward" -> Icons.Rounded.ArrowUpward
    "arrow_downward" -> Icons.Rounded.ArrowDownward
    "wallet" -> Icons.Rounded.Wallet
    "warning" -> Icons.Rounded.Warning
    "fastfood" -> Icons.Rounded.Fastfood
    "local_cafe" -> Icons.Rounded.LocalCafe
    "local_bar" -> Icons.Rounded.LocalBar
    "fitness_center" -> Icons.Rounded.FitnessCenter
    "pets" -> Icons.Rounded.Pets
    "movie" -> Icons.Rounded.Movie
    "music_note" -> Icons.Rounded.MusicNote
    "local_gas_station" -> Icons.Rounded.LocalGasStation
    "bolt" -> Icons.Rounded.Bolt
    "wifi" -> Icons.Rounded.Wifi
    "credit_card" -> Icons.Rounded.CreditCard
    "account_balance" -> Icons.Rounded.AccountBalance
    "redeem" -> Icons.Rounded.Redeem
    "celebration" -> Icons.Rounded.Celebration
    "spa" -> Icons.Rounded.Spa
    "park" -> Icons.Rounded.Park
    "directions_bus" -> Icons.Rounded.DirectionsBus
    "train" -> Icons.Rounded.Train
    "local_hospital" -> Icons.Rounded.LocalHospital
    "menu_book" -> Icons.AutoMirrored.Rounded.MenuBook
    "sports_soccer" -> Icons.Rounded.SportsSoccer
    "brush" -> Icons.Rounded.Brush
    "build" -> Icons.Rounded.Build
    "cake" -> Icons.Rounded.Cake
    "computer" -> Icons.Rounded.Computer
    "headphones" -> Icons.Rounded.Headphones
    "favorite" -> Icons.Rounded.Favorite
    "star" -> Icons.Rounded.Star
    "water_drop" -> Icons.Rounded.WaterDrop
    else -> Icons.Rounded.MoreHoriz
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
