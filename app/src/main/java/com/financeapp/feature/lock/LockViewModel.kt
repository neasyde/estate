package com.financeapp.feature.lock

import androidx.lifecycle.ViewModel
import com.financeapp.core.domain.usecase.VerifyPinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class LockState(
    val entered: String = "",
    val error: Boolean = false,
    val attempts: Int = 0,
    val lockedUntil: Long = 0L,
)

@HiltViewModel
class LockViewModel @Inject constructor(
    private val verifyPin: VerifyPinUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(LockState())
    val state: StateFlow<LockState> = _state.asStateFlow()

    fun onDigit(d: Char) = _state.update {
        if (it.entered.length < 4) it.copy(entered = it.entered + d, error = false) else it
    }

    fun onDelete() = _state.update { it.copy(entered = it.entered.dropLast(1), error = false) }

    /** Returns true if the PIN is correct. Applies a 30s lockout after 5 failures. */
    suspend fun submit(now: Long): Boolean {
        val pin = _state.value.entered
        if (pin.length < 4) return false
        return if (verifyPin(pin)) {
            _state.update { LockState() }
            true
        } else {
            _state.update {
                val attempts = it.attempts + 1
                val locked = if (attempts >= 5) now + 30_000L else it.lockedUntil
                it.copy(entered = "", error = true, attempts = attempts, lockedUntil = locked)
            }
            false
        }
    }

    fun remainingLock(now: Long): Long = (_state.value.lockedUntil - now).coerceAtLeast(0L)
}
