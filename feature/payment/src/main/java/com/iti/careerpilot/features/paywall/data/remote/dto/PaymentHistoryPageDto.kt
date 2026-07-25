package com.iti.careerpilot.features.paywall.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentHistoryPageDto(
    @SerialName("content") val content: List<PaymentTransactionResponseDto> = emptyList()
)
