package com.iti.careerpilot.features.paywall.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckoutResponseDto(
    @SerialName("checkoutUrl") val checkoutUrl: String,
    @SerialName("merchantOrderId") val merchantOrderId: String,
)
