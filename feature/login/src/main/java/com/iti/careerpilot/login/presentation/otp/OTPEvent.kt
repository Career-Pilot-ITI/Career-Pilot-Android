package com.iti.careerpilot.login.presentation.otp

sealed interface OTPEvent {
    data class NavigateToHome(val isUserRegistered: Boolean) : OTPEvent
}
