package com.financeapp.feature.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.domain.usecase.VerifyPinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LockState(
    val entered: String = "",
    val error: Boolean = false,
    val attempts: Int = 0,
    val lockedUntil: Long = 0L,
    val loading: Boolean = false,
)

@HiltViewModel
class LockViewModel @Inject constructor(
    private val verifyPin: VerifyPinUseCase,
    private val repo: SettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(LockState())
    val state: StateFlow<LockState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val s = repo.settings.first()
            val lu = s.lockoutUntil
            val att = s.failedAttempts
            if (lu > 0 && lu <= System.currentTimeMillis()) {
                repo.setFailedAttempts(0)
                repo.setLockoutUntil(0L)
                _state.update { LockState() }
            } else {
                _state.update { LockState(attempts = att, lockedUntil = lu) }
            }
        }
    }

    fun onDigit(d: Char) = _state.update {
        if (it.entered.length < 4) it.copy(entered = it.entered + d, error = false) else it
    }

    fun onDelete() = _state.update { it.copy(entered = it.entered.dropLast(1), error = false) }

    suspend fun submit(now: Long): Boolean {
        val pin = _state.value.entered
        if (pin.length < 4) return false

        _state.update { it.copy(loading = true) }
        try {
            val s = repo.settings.first()
            if (s.lockoutUntil > now) {
                _state.update { it.copy(loading = false) }
                return false
            }

            return if (verifyPin(pin)) {
                repo.setFailedAttempts(0)
                repo.setLockoutUntil(0L)
                true
            } else {
                val newAttempts = _state.value.attempts + 1
                val lockoutSeconds = when {
                    newAttempts >= 20 -> 3600L
                    newAttempts >= 15 -> 900L
                    newAttempts >= 10 -> 300L
                    newAttempts >= 7 -> 120L
                    newAttempts >= 5 -> 30L
                    else -> 0L
                }
                val newLockedUntil = if (lockoutSeconds > 0) now + lockoutSeconds * 1000L else _state.value.lockedUntil
                repo.setFailedAttempts(newAttempts)
                repo.setLockoutUntil(newLockedUntil)
                _state.update { it.copy(entered = "", error = true, attempts = newAttempts, lockedUntil = newLockedUntil, loading = false) }
                false
            }
        } catch (e: Exception) {
            _state.update { it.copy(loading = false) }
            throw e
        }
    }

    fun remainingLock(now: Long): Long = (_state.value.lockedUntil - now).coerceAtLeast(0L)
}
