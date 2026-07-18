package com.iti.careerpilot.login.presentation.login

sealed interface LoginEvent {
    data class NavigateToOtp(val phoneNumber: String) : LoginEvent
}
