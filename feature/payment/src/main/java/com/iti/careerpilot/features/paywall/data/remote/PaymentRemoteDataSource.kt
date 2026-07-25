package com.iti.careerpilot.features.paywall.data.remote

import com.iti.careerpilot.features.paywall.data.remote.dto.CheckoutResponseDto
import com.iti.careerpilot.features.paywall.data.remote.dto.CoinBalanceResponseDto
import com.iti.careerpilot.features.paywall.data.remote.dto.DowngradeSubscriptionRequestDto
import com.iti.careerpilot.features.paywall.data.remote.dto.PaymentInitiateRequestDto
import com.iti.careerpilot.features.paywall.data.remote.dto.SubscriptionResponseDto
import com.iti.careerpilot.features.paywall.data.remote.dto.TopUpRequestDto
import com.iti.careerpilot.features.paywall.data.remote.dto.UpgradeSubscriptionRequestDto

import com.iti.careerpilot.features.paywall.data.remote.dto.PaymentHistoryPageDto

interface PaymentRemoteDataSource {
    suspend fun getWalletBalance(): CoinBalanceResponseDto
    suspend fun topUpWallet(request: TopUpRequestDto): CheckoutResponseDto
    suspend fun initiatePayment(request: PaymentInitiateRequestDto): CheckoutResponseDto
    suspend fun getCurrentSubscription(): SubscriptionResponseDto
    suspend fun upgradeSubscription(request: UpgradeSubscriptionRequestDto): CheckoutResponseDto
    suspend fun downgradeSubscription(request: DowngradeSubscriptionRequestDto)
    suspend fun cancelSubscription()
    suspend fun getPaymentHistory(): PaymentHistoryPageDto
}
