package com.financeapp.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "recurring_rules",
    indices = [Index("templateTransactionId")],
)
data class RecurringRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateJson: String,
    val intervalType: String,
    val nextDate: Long,
    val autoAdd: Boolean = false,
    val enabled: Boolean = true,
    val templateTransactionId: Long? = null,
)
