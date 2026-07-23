package com.iti.careerpilot.features.paywall.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoinBalanceResponseDto(
    @SerialName("balance") val balance: Int,
)
