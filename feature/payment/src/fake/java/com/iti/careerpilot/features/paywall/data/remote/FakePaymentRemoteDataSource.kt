package com.iti.careerpilot.features.paywall.data.remote

import com.iti.careerpilot.features.paywall.data.remote.PaymentRemoteDataSource
import com.iti.careerpilot.features.paywall.data.remote.dto.CheckoutResponseDto
import com.iti.careerpilot.features.paywall.data.remote.dto.CoinBalanceResponseDto
import com.iti.careerpilot.features.paywall.data.remote.dto.DowngradeSubscriptionRequestDto
import com.iti.careerpilot.features.paywall.data.remote.dto.PaymentInitiateRequestDto
import com.iti.careerpilot.features.paywall.data.remote.dto.SubscriptionResponseDto
import com.iti.careerpilot.features.paywall.data.remote.dto.TopUpRequestDto
import com.iti.careerpilot.features.paywall.data.remote.dto.UpgradeSubscriptionRequestDto
import com.iti.common.util.fakeDelay
import com.iti.common.util.shouldFail
import javax.inject.Inject

class FakePaymentRemoteDataSource @Inject constructor() : PaymentRemoteDataSource {

    private var currentBalance: Int = 250
    private var currentTier: String = "FREE"
    private var pendingTier: String? = null
    private var isSubscriptionActive: Boolean = true

    override suspend fun getWalletBalance(): CoinBalanceResponseDto {
        fakeDelay()
        if (shouldFail()) throw Exception("Fake network error")
        return CoinBalanceResponseDto(balance = currentBalance)
    }

    override suspend fun topUpWallet(request: TopUpRequestDto): CheckoutResponseDto {
        fakeDelay()
        if (shouldFail()) throw Exception("Fake network error")
        currentBalance += request.coinPackSize
        return CheckoutResponseDto(
            checkoutUrl = "https://fake.payment/checkout?pack=${request.coinPackSize}",
            merchantOrderId = "fake_topup_${System.currentTimeMillis()}"
        )
    }

    override suspend fun initiatePayment(request: PaymentInitiateRequestDto): CheckoutResponseDto {
        fakeDelay()
        if (shouldFail()) throw Exception("Fake network error")
        request.coinPackSize?.let { currentBalance += it }
        request.tier?.let { currentTier = it }
        return CheckoutResponseDto(
            checkoutUrl = "https://fake.payment/checkout?amount=${request.amount}",
            merchantOrderId = "fake_payment_${System.currentTimeMillis()}"
        )
    }

    override suspend fun getCurrentSubscription(): SubscriptionResponseDto {
        fakeDelay()
        if (shouldFail()) throw Exception("Fake network error")
        return SubscriptionResponseDto(
            tier = currentTier,
            isActive = isSubscriptionActive,
            startedAt = "2026-01-01T00:00:00Z",
            renewalDate = "2026-12-31T23:59:59Z",
            pendingTier = pendingTier
        )
    }

    override suspend fun upgradeSubscription(request: UpgradeSubscriptionRequestDto): CheckoutResponseDto {
        fakeDelay()
        if (shouldFail()) throw Exception("Fake network error")
        currentTier = request.tier
        isSubscriptionActive = true
        pendingTier = null
        return CheckoutResponseDto(
            checkoutUrl = "https://fake.payment/checkout?tier=${request.tier}",
            merchantOrderId = "fake_upgrade_${System.currentTimeMillis()}"
        )
    }

    override suspend fun downgradeSubscription(request: DowngradeSubscriptionRequestDto) {
        fakeDelay()
        if (shouldFail()) throw Exception("Fake network error")
        pendingTier = request.tier
    }

    override suspend fun cancelSubscription() {
        fakeDelay()
        if (shouldFail()) throw Exception("Fake network error")
        isSubscriptionActive = false
    }
}
