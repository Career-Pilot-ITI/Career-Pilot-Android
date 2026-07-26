package com.iti.careerpilot.features.paywall.data.mapper

import com.iti.careerpilot.features.paywall.data.remote.dto.CoinBalanceResponseDto
import com.iti.core.model.WalletBalance

fun CoinBalanceResponseDto.toDomain(): WalletBalance = WalletBalance(
    balance = this.balance
)
