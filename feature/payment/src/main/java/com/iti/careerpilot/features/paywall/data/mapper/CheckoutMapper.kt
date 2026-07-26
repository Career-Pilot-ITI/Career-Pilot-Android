package com.iti.careerpilot.features.paywall.data.mapper

import com.iti.careerpilot.features.paywall.data.remote.dto.CheckoutResponseDto
import com.iti.core.model.CheckoutSession

fun CheckoutResponseDto.toDomain(): CheckoutSession = CheckoutSession(
    checkoutUrl = this.checkoutUrl,
    merchantOrderId = this.merchantOrderId
)
