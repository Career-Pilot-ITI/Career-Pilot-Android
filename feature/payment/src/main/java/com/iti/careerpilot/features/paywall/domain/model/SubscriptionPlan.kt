package com.iti.careerpilot.features.paywall.domain.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.iti.careerpilot.payment.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

object SubscriptionTier {
    fun normalizeTierId(rawTier: String?): String = when (rawTier?.lowercase()) {
        "pro", "max" -> "pro"
        "plus" -> "plus"
        else -> "free"
    }

    fun getTierLevel(rawTier: String?): Int = when (normalizeTierId(rawTier)) {
        "pro" -> 2
        "plus" -> 1
        else -> 0
    }

    @StringRes
    fun getNameRes(rawTier: String?): Int = when (normalizeTierId(rawTier)) {
        "pro" -> R.string.paywall_plan_max
        "plus" -> R.string.paywall_plan_plus
        else -> R.string.paywall_plan_free
    }
}

@Immutable
data class SubscriptionPlan(
    val id: String,
    @StringRes val nameRes: Int,
    val priceEgp: Int? = null,
    val originalPriceEgp: Int? = null,
    val isInitiallySelected: Boolean = false,
    val features: ImmutableList<Int> = persistentListOf(),
) {
    val tierLevel: Int
        get() = SubscriptionTier.getTierLevel(id)

    val discountPercentage: Int?
        get() = if (originalPriceEgp != null && priceEgp != null && originalPriceEgp > priceEgp) {
            ((1f - (priceEgp.toFloat() / originalPriceEgp.toFloat())) * 100).toInt()
        } else null
}
