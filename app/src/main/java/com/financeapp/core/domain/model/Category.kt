package com.financeapp.core.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val color: Int,
    val type: CategoryType,
    val isCustom: Boolean = false,
    val isHidden: Boolean = false,
    val sortOrder: Int = 0,
)
