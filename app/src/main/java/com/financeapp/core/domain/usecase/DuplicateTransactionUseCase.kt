package com.financeapp.core.domain.usecase

import com.financeapp.core.domain.repository.TransactionRepository
import javax.inject.Inject

class DuplicateTransactionUseCase @Inject constructor(
    private val repo: TransactionRepository,
) {
    suspend operator fun invoke(id: Long, now: Long): Long? {
        val src = repo.getById(id) ?: return null
        return repo.upsert(src.copy(id = 0, date = now, recurringRuleId = null))
    }
}
