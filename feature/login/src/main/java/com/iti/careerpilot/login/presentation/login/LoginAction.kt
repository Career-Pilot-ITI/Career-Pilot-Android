package com.iti.careerpilot.login.presentation.login

sealed interface LoginAction {
    data class PhoneNumberChanged(val phoneNumber: String) : LoginAction
    data class RegionChanged(val regionCode: String) : LoginAction
    data object SendOtpClicked : LoginAction
    data object StopIsLoading: LoginAction
}
