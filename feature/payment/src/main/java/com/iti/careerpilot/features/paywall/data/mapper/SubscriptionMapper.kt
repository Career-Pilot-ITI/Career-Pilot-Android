package com.iti.careerpilot.features.paywall.data.mapper

import com.iti.careerpilot.features.paywall.data.remote.dto.SubscriptionResponseDto
import com.iti.core.model.SubscriptionInfo

fun SubscriptionResponseDto.toDomain(): SubscriptionInfo = SubscriptionInfo(
    tier = this.tier,
    isActive = this.isActive,
    startedAt = this.startedAt,
    renewalDate = this.renewalDate,
    cancelledAt = this.cancelledAt,
    pendingTier = this.pendingTier
)
