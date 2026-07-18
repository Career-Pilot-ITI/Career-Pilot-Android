package com.iti.careerpilot.login.presentation.otp

import com.iti.common.util.UIText

data class OTPState(
    val phoneNumber: String = "",
    val code: String = "",
    val isLoading: Boolean = false,
    val error: UIText? = null,
)
