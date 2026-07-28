package com.iti.core.model

import kotlinx.serialization.Serializable

@Serializable
data class FeatureQuota(
    val feature: FeatureKey,
    val remaining: Int?,
    val max: Int?,
    val coinCost: Int? = null
)
