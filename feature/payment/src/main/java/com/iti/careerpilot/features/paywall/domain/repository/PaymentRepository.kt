package com.iti.careerpilot.features.paywall.domain.repository

import com.iti.core.model.*
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult

interface PaymentRepository {
    suspend fun getWalletBalance(): CareerPilotResult<WalletBalance, NetworkError>
    suspend fun topUpWallet(coinPackSize: Int, currency: String, method: String): CareerPilotResult<CheckoutSession, NetworkError>
    suspend fun initiatePayment(amount: Double, currency: String, method: String, provider: String, purchaseType: String, coinPackSize: Int?, tier: String?): CareerPilotResult<CheckoutSession, NetworkError>
    suspend fun getCurrentSubscription(): CareerPilotResult<SubscriptionInfo, NetworkError>
    suspend fun upgradeSubscription(tier: String, currency: String, method: String): CareerPilotResult<CheckoutSession, NetworkError>
    suspend fun downgradeSubscription(tier: String): CareerPilotResult<Unit, NetworkError>
    suspend fun cancelSubscription(): CareerPilotResult<Unit, NetworkError>
}
