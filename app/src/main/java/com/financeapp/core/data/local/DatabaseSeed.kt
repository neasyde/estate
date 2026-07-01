package com.financeapp.core.data.local

import com.financeapp.core.data.local.entity.CategoryEntity

/**
 * Preset categories inserted on first DB creation.
 * `name` holds a string-resource KEY (resolved to a localized label in the UI).
 * `color` values are plain data (category swatches), not theme colors.
 */
object DatabaseSeed {
    fun categories(): List<CategoryEntity> = listOf(
        CategoryEntity(name = "cat_food", icon = "restaurant", color = 0xFFEF5350.toInt(), type = "EXPENSE"),
        CategoryEntity(name = "cat_transport", icon = "directions_car", color = 0xFF42A5F5.toInt(), type = "EXPENSE"),
        CategoryEntity(name = "cat_health", icon = "medical_services", color = 0xFF26A69A.toInt(), type = "EXPENSE"),
        CategoryEntity(name = "cat_entertainment", icon = "sports_esports", color = 0xFFAB47BC.toInt(), type = "EXPENSE"),
        CategoryEntity(name = "cat_clothing", icon = "checkroom", color = 0xFFEC407A.toInt(), type = "EXPENSE"),
        CategoryEntity(name = "cat_communication", icon = "phone", color = 0xFF5C6BC0.toInt(), type = "EXPENSE"),
        CategoryEntity(name = "cat_utilities", icon = "home", color = 0xFF8D6E63.toInt(), type = "EXPENSE"),
        CategoryEntity(name = "cat_education", icon = "school", color = 0xFF66BB6A.toInt(), type = "EXPENSE"),
        CategoryEntity(name = "cat_travel", icon = "flight", color = 0xFF29B6F6.toInt(), type = "EXPENSE"),
        CategoryEntity(name = "cat_other", icon = "more_horiz", color = 0xFF78909C.toInt(), type = "EXPENSE"),
        CategoryEntity(name = "cat_salary", icon = "work", color = 0xFF43A047.toInt(), type = "INCOME"),
        CategoryEntity(name = "cat_freelance", icon = "laptop", color = 0xFF7E57C2.toInt(), type = "INCOME"),
        CategoryEntity(name = "cat_investments", icon = "trending_up", color = 0xFF00897B.toInt(), type = "INCOME"),
        CategoryEntity(name = "cat_gifts", icon = "card_giftcard", color = 0xFFD81B60.toInt(), type = "INCOME"),
        CategoryEntity(name = "cat_other", icon = "more_horiz", color = 0xFF78909C.toInt(), type = "INCOME"),
    )
}
