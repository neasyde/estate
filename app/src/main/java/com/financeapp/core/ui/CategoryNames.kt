package com.financeapp.core.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.financeapp.R
import com.financeapp.core.domain.model.Category

@StringRes
fun categoryNameRes(key: String): Int? = when (key) {
    "cat_food" -> R.string.cat_food
    "cat_transport" -> R.string.cat_transport
    "cat_health" -> R.string.cat_health
    "cat_entertainment" -> R.string.cat_entertainment
    "cat_clothing" -> R.string.cat_clothing
    "cat_communication" -> R.string.cat_communication
    "cat_utilities" -> R.string.cat_utilities
    "cat_education" -> R.string.cat_education
    "cat_travel" -> R.string.cat_travel
    "cat_other" -> R.string.cat_other
    "cat_salary" -> R.string.cat_salary
    "cat_freelance" -> R.string.cat_freelance
    "cat_investments" -> R.string.cat_investments
    "cat_gifts" -> R.string.cat_gifts
    else -> null
}

/** System categories store a resource key in [Category.name]; custom ones store a literal name. */
@Composable
fun categoryDisplayName(category: Category): String {
    if (category.isCustom) return category.name
    val res = categoryNameRes(category.name)
    return if (res != null) stringResource(res) else category.name
}
