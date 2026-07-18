package com.iti.careerpilot.login.presentation.otp

sealed interface OTPAction {
    data class PhoneNumberReceived(val phoneNumber: String) : OTPAction
    data class CodeChanged(val code: String) : OTPAction
    data object ResendClicked : OTPAction
}
