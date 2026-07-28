package com.financeapp.core.domain.usecase

import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.utils.PinHasher
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class VerifyPinUseCase @Inject constructor(
    private val repo: SettingsRepository,
) {
    suspend operator fun invoke(pin: String): Boolean {
        val settings = repo.settings.first()

        val lockoutUntil = settings.lockoutUntil
        if (lockoutUntil > System.currentTimeMillis()) {
            return false
        }

        val stored = settings.pinHash ?: return false
        val ok = PinHasher.verify(pin, stored)
        if (ok && PinHasher.needsMigration(stored)) {
            repo.setPinHash(PinHasher.hash(pin))
        }
        return ok
    }
}
