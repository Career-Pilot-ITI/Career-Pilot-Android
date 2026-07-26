package com.iti.careerpilot.features.paywall.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopUpRequestDto(
    @SerialName("coinPackSize") val coinPackSize: Int,
    @SerialName("currency") val currency: String,
    @SerialName("method") val method: String,
)
