package com.iti.careerpilot.features.paywall.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentTransactionResponseDto(
    @SerialName("id") val id: Long,
    @SerialName("amount") val amount: Double,
    @SerialName("currency") val currency: String,
    @SerialName("status") val status: String,
    @SerialName("paymentMethod") val paymentMethod: String,
    @SerialName("provider") val provider: String,
    @SerialName("merchantOrderId") val merchantOrderId: String,
    @SerialName("providerTransactionId") val providerTransactionId: String? = null,
    @SerialName("failureReason") val failureReason: String? = null,
    @SerialName("coinPackSize") val coinPackSize: Int? = null,
    @SerialName("tierPurchased") val tierPurchased: String? = null,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("confirmedAt") val confirmedAt: String? = null,
)
