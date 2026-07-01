package com.financeapp.core.domain.usecase

import com.financeapp.core.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repo: TransactionRepository,
) {
    suspend operator fun invoke(id: Long) = repo.delete(id)
}
