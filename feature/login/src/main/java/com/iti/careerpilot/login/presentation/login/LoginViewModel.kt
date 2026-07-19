package com.iti.careerpilot.login.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.login.domain.usecase.SendOtpUseCase
import com.iti.careerpilot.login.domain.usecase.ValidatePhoneNumberUseCase
import com.iti.careerpilot.login.presentation.login.mapper.toUIText
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.util.countdownFlow
import com.iti.common.util.toUIText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sendOtp: SendOtpUseCase,
    private val validatePhoneNumber: ValidatePhoneNumberUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = LoginState()
        )

    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()

    private var cooldownJob: Job? = null

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.PhoneNumberChanged ->
                _state.update { it.copy(phoneNumber = action.phoneNumber, error = null) }

            is LoginAction.RegionChanged ->
                _state.update { it.copy(regionCode = action.regionCode) }

            is LoginAction.SendOtpClicked -> sendOtp()
        }
    }

    private fun sendOtp() {
        val current = _state.value
        if (current.isLoading || current.isInCooldown) return

        val fullPhoneNumber = when (
            val result = validatePhoneNumber(current.phoneNumber, current.regionCode)
        ) {
            is CareerPilotResult.Error -> {
                _state.update { it.copy(error = result.error.toUIText()) }
                return
            }

            is CareerPilotResult.Success -> result.data
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            sendOtp(fullPhoneNumber)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(LoginEvent.NavigateToOtp(fullPhoneNumber))
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, error = error.toUIText()) }
                    if (error == NetworkError.TOO_MANY_REQUESTS) startCooldown()
                }
        }
    }

    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            countdownFlow(COOLDOWN_SECONDS).collect { remaining ->
                _state.update { it.copy(cooldownSecondsRemaining = remaining) }
            }
        }
    }

    companion object {
        private const val COOLDOWN_SECONDS = 60
    }
}
