package com.iti.careerpilot.login.presentation.otp

import com.iti.common.util.UIText

data class OTPState(
    val phoneNumber: String = "",
    val code: String = "",
    val isVerifyingOtp: Boolean = false,
    val isResendingOtp: Boolean = false,
    val error: UIText? = null,
    val resendSecondsRemaining: Int = 0,
    val isVerified: Boolean = false,
) {
    val canResend: Boolean get() = resendSecondsRemaining == 0 && !isVerifyingOtp
}
