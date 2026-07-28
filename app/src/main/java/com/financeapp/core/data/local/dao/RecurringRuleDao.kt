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

    @Query("SELECT * FROM recurring_rules ORDER BY nextDate")
    suspend fun allOnce(): List<RecurringRuleEntity>

    @Query("DELETE FROM recurring_rules")
    suspend fun deleteAll()

    @Query("SELECT * FROM recurring_rules WHERE id = :id")
    suspend fun getById(id: Long): RecurringRuleEntity?

    @Query("SELECT * FROM recurring_rules WHERE nextDate <= :now")
    suspend fun due(now: Long): List<RecurringRuleEntity>

    /**
     * Find a rule whose [RecurringTemplate] JSON references the given transaction id.
     * Used to dedupe when the user edits a transaction that was already recurring — we
     * update the rule in place instead of creating a second one.
     */
    @Query("SELECT * FROM recurring_rules WHERE templateJson LIKE :pattern1 OR templateJson LIKE :pattern2 LIMIT 1")
    suspend fun findByLinkedTransactionId(pattern1: String, pattern2: String): RecurringRuleEntity?

    @Upsert
    suspend fun upsert(e: RecurringRuleEntity): Long

    @Query("DELETE FROM recurring_rules WHERE id = :id")
    suspend fun delete(id: Long)
}
