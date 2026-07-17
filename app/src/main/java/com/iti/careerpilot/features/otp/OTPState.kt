package com.iti.careerpilot.features.otp

data class OTPState(
    val paramOne: String = "default",
    val paramTwo: List<String> = emptyList(),
)