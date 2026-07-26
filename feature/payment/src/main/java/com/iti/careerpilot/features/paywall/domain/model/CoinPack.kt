package com.iti.careerpilot.features.paywall.domain.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.iti.careerpilot.payment.R

@Immutable
data class CoinPack(
    val id: String,
    val coins: Int,
    val priceEgp: Int,
    val originalPriceEgp: Int? = null,
    @StringRes val subtitleRes: Int? = null,
    val badge: CoinPackBadge? = null,
) {
    val discountPercentage: Int?
        get() = if (originalPriceEgp != null && originalPriceEgp > priceEgp) {
            ((1f - (priceEgp.toFloat() / originalPriceEgp.toFloat())) * 100).toInt()
        } else null
}

enum class CoinPackBadge(@StringRes val labelRes: Int) {
    MOST_POPULAR(R.string.paywall_badge_most_popular),
    BEST_VALUE(R.string.paywall_badge_best_value),
}
