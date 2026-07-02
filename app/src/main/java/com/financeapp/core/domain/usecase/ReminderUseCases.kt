package com.financeapp.core.domain.usecase

import com.financeapp.core.domain.model.Reminder
import com.financeapp.core.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRemindersUseCase @Inject constructor(private val repo: ReminderRepository) {
    operator fun invoke(): Flow<List<Reminder>> = repo.observeAll()
}

class SaveReminderUseCase @Inject constructor(private val repo: ReminderRepository) {
    suspend operator fun invoke(r: Reminder): Long = repo.upsert(r)
}

class DeleteReminderUseCase @Inject constructor(private val repo: ReminderRepository) {
    suspend operator fun invoke(id: Long) = repo.delete(id)
}
