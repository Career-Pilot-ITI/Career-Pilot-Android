package com.iti.careerpilot.login.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SendOtpRequest(
    val phoneNumber: String,
)

@Serializable
data class VerifyOtpRequest(
    val phoneNumber: String,
    val code: String,
)
