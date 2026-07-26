package com.iti.careerpilot.features.paywall.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DowngradeSubscriptionRequestDto(
    @SerialName("tier") val tier: String,
)
