package com.iti.careerpilot.features.paywall.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionResponseDto(
    @SerialName("tier") val tier: String,
    @SerialName("isActive") val isActive: Boolean,
    @SerialName("startedAt") val startedAt: String? = null,
    @SerialName("renewalDate") val renewalDate: String? = null,
    @SerialName("cancelledAt") val cancelledAt: String? = null,
    @SerialName("pendingTier") val pendingTier: String? = null,
)
