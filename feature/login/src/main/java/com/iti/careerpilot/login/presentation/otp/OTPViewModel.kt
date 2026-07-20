package com.iti.careerpilot.login.presentation.otp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.login.domain.usecase.SendOtpUseCase
import com.iti.careerpilot.login.domain.usecase.VerifyOtpUseCase
import com.iti.core.datastore.CareerPilotPreferencesDataSource
import com.iti.common.util.countdownFlow
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.util.toUIText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class OTPViewModel @Inject constructor(
    private val verifyOtp: VerifyOtpUseCase,
    private val sendOtp: SendOtpUseCase,
    private val datastore: CareerPilotPreferencesDataSource,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(
        OTPState(phoneNumber = savedStateHandle[KEY_PHONE_NUMBER] ?: ""),
    )
    val state = _state.asStateFlow()

    private val _events = Channel<OTPEvent>()
    val events = _events.receiveAsFlow()

    private var resendTimerJob: Job? = null

    fun onAction(action: OTPAction) {
        when (action) {
            is OTPAction.PhoneNumberReceived -> initialize(action.phoneNumber)
            is OTPAction.CodeChanged -> onCodeChanged(action.code)
            is OTPAction.ResendClicked -> resend()
        }
    }

    private fun initialize(phoneNumber: String) {
        if (KEY_PHONE_NUMBER in savedStateHandle) return
        savedStateHandle[KEY_PHONE_NUMBER] = phoneNumber
        _state.update { it.copy(phoneNumber = phoneNumber) }
        startResendCountdown()
    }

    private fun onCodeChanged(input: String) {
        val digitsOnly = input.filter(Char::isDigit).take(OTP_LENGTH)
        _state.update { it.copy(code = digitsOnly, error = null) }
        if (digitsOnly.length == OTP_LENGTH) verify()
    }

    private fun verify() {
        val current = _state.value
        if (current.isLoading || current.isVerified) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            verifyOtp(current.phoneNumber, current.code)
                .onSuccess { session ->
                    viewModelScope.launch {
                        datastore.setToken(session.accessToken)
                        datastore.setRefreshToken(session.refreshToken)
                        datastore.setHasCompletedOnboarding(session.hasCompletedOnboarding)

                        resendTimerJob?.cancel()
                        _state.update { it.copy(isLoading = false, isVerified = true) }
                        delay(SUCCESS_DISMISS_MILLIS.milliseconds)
                        _events.send(OTPEvent.NavigateToHome)
                    }
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
            countdownFlow(RESEND_SECONDS).collect { remaining ->
                _state.update { it.copy(resendSecondsRemaining = remaining) }
            }
        }
    }

    companion object {
        const val OTP_LENGTH = 6
        private const val RESEND_SECONDS = 60
        private const val SUCCESS_DISMISS_MILLIS = 1_800L
        private const val KEY_PHONE_NUMBER = "otp_phone_number"
    }
}
