package com.financeapp.core.domain.usecase

import com.financeapp.core.domain.model.Transaction
import com.financeapp.core.domain.repository.TransactionRepository
import javax.inject.Inject

class SaveTransactionUseCase @Inject constructor(
    private val repo: TransactionRepository,
) {
    suspend operator fun invoke(t: Transaction): Long = repo.upsert(t)
}
