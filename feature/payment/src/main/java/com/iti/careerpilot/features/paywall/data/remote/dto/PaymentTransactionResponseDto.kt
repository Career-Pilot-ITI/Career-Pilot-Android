package com.iti.careerpilot.features.paywall.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentTransactionResponseDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("amount") val amount: Double? = null,
    @SerialName("currency") val currency: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("paymentMethod") val paymentMethod: String? = null,
    @SerialName("provider") val provider: String? = null,
    @SerialName("merchantOrderId") val merchantOrderId: String? = null,
    @SerialName("providerTransactionId") val providerTransactionId: String? = null,
    @SerialName("failureReason") val failureReason: String? = null,
    @SerialName("coinPackSize") val coinPackSize: Int? = null,
    @SerialName("tierPurchased") val tierPurchased: String? = null,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("confirmedAt") val confirmedAt: String? = null,
)
