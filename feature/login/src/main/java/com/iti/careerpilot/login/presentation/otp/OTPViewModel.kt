package com.iti.careerpilot.login.presentation.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.login.domain.usecase.SendOtpUseCase
import com.iti.careerpilot.login.domain.usecase.VerifyOtpUseCase
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.util.toUIText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

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

    private var resendTimerJob: Job? = null
    private var initialized = false

    fun onAction(action: OTPAction) {
        when (action) {
            is OTPAction.PhoneNumberReceived -> initialize(action.phoneNumber)
            is OTPAction.CodeChanged -> onCodeChanged(action.code)
            is OTPAction.ResendClicked -> resend()
        }
    }

    private fun initialize(phoneNumber: String) {
        if (initialized) return
        initialized = true
        _state.update { it.copy(phoneNumber = phoneNumber) }
        startResendCountdown()
    }

    private fun onCodeChanged(input: String) {
        val sanitized = input.filter(Char::isDigit).take(OTP_LENGTH)
        _state.update { it.copy(code = sanitized, error = null) }
        if (sanitized.length == OTP_LENGTH) verify()
    }

    private fun verify() {
        val current = _state.value
        if (current.isLoading || current.isVerified) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            verifyOtp(current.phoneNumber, current.code)
                .onSuccess {
                    // TODO: save session tokens for authenticated calls
                    resendTimerJob?.cancel()
                    _state.update { it.copy(isLoading = false, isVerified = true) }
                    delay(SUCCESS_DISMISS_MILLIS.milliseconds)
                    _events.send(OTPEvent.NavigateToHome)
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, error = error.toUIText(), code = "") }
                }
        }
    }

    private fun resend() {
        if (!_state.value.canResend) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            sendOtp(_state.value.phoneNumber)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, code = "") }
                    startResendCountdown()
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, error = error.toUIText()) }
                }
        }
    }

    private fun startResendCountdown() {
        resendTimerJob?.cancel()
        resendTimerJob = viewModelScope.launch {
            _state.update { it.copy(resendSecondsRemaining = RESEND_SECONDS) }
            while (_state.value.resendSecondsRemaining > 0) {
                delay(1_000L.milliseconds)
                _state.update { it.copy(resendSecondsRemaining = it.resendSecondsRemaining - 1) }
            }
        }
    }

    companion object {
        const val OTP_LENGTH = 6
        private const val RESEND_SECONDS = 60
        private const val SUCCESS_DISMISS_MILLIS = 1_800L
    }
}
