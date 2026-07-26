package com.iti.core.model

data class SubscriptionInfo(
    val tier: String, 
    val isActive: Boolean, 
    val startedAt: String?, 
    val renewalDate: String?, 
    val cancelledAt: String?, 
    val pendingTier: String?
)
