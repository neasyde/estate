package com.financeapp.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.financeapp.core.data.local.entity.RecurringRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringRuleDao {
    @Query("SELECT * FROM recurring_rules ORDER BY nextDate")
    fun observeAll(): Flow<List<RecurringRuleEntity>>

    @Query("SELECT * FROM recurring_rules WHERE nextDate <= :now")
    suspend fun due(now: Long): List<RecurringRuleEntity>

    @Upsert
    suspend fun upsert(e: RecurringRuleEntity): Long

    @Query("DELETE FROM recurring_rules WHERE id = :id")
    suspend fun delete(id: Long)
}
