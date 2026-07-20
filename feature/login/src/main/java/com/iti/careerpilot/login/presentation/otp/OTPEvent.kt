package com.iti.careerpilot.login.presentation.otp

sealed interface OTPEvent {
    data object NavigateToHome : OTPEvent
}
