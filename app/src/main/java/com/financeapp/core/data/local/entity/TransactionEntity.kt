package com.financeapp.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val currency: String,
    val type: String,
    val categoryId: Long?,
    val note: String?,
    val date: Long,
    val recurringRuleId: Long?,
)
