package com.iti.careerpilot.login.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendOtpRequest(
    @SerialName("phoneNumber") val phoneNumber: String,
)

@Serializable
data class VerifyOtpRequest(
    @SerialName("phoneNumber") val phoneNumber: String,
    @SerialName("code") val code: String,
)
