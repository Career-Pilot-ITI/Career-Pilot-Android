package com.iti.careerpilot.features.paywall.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentInitiateRequestDto(
    @SerialName("amount") val amount: Double,
    @SerialName("currency") val currency: String,
    @SerialName("method") val method: String,
    @SerialName("provider") val provider: String,
    @SerialName("purchaseType") val purchaseType: String,
    @SerialName("coinPackSize") val coinPackSize: Int? = null,
    @SerialName("tier") val tier: String? = null,
)
