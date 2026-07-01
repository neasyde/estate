package com.financeapp.core.data.mapper

import com.financeapp.core.data.local.entity.CategoryEntity
import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType

fun CategoryEntity.toDomain() = Category(
    id = id,
    name = name,
    icon = icon,
    color = color,
    type = CategoryType.valueOf(type),
    isCustom = isCustom,
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    name = name,
    icon = icon,
    color = color,
    type = type.name,
    isCustom = isCustom,
)
