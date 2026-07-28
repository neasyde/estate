package com.financeapp.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.financeapp.core.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY id")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets ORDER BY id")
    suspend fun allOnce(): List<BudgetEntity>

    @Query("DELETE FROM budgets")
    suspend fun deleteAll()

    @Upsert
    suspend fun upsert(e: BudgetEntity): Long

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun delete(id: Long)
}
