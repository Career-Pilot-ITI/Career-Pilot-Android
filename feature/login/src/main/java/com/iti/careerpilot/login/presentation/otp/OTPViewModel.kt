package com.iti.careerpilot.login.presentation.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.login.domain.usecase.SendOtpUseCase
import com.iti.careerpilot.login.domain.usecase.VerifyOtpUseCase
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.util.toUIText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OTPViewModel @Inject constructor(
    private val verifyOtp: VerifyOtpUseCase,
    private val sendOtp: SendOtpUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(OTPState())
    val state = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = OTPState()
        )

    private val _events = Channel<OTPEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: OTPAction) {
        when (action) {
            is OTPAction.PhoneNumberReceived ->
                _state.update { it.copy(phoneNumber = action.phoneNumber) }

            is OTPAction.CodeChanged ->
                _state.update { it.copy(code = action.code, error = null) }

            is OTPAction.VerifyClicked -> verify()

            is OTPAction.ResendClicked -> resend()
        }
    }

    private fun verify() {
        val current = _state.value
        if (current.code.isBlank() || current.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            verifyOtp(current.phoneNumber, current.code.trim())
                .onSuccess {
                    // TODO: persist session tokens
                    _state.update { it.copy(isLoading = false) }
                    _events.send(OTPEvent.NavigateToHome)
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, error = error.toUIText()) }
                }
        }
    }

    private fun resend() {
        val current = _state.value
        if (current.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            sendOtp(current.phoneNumber)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, code = "") }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, error = error.toUIText()) }
                }
        }
    }
}
