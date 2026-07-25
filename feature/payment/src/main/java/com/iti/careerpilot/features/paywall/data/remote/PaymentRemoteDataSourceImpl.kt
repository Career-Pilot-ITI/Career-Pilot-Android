package com.iti.careerpilot.features.paywall.data.remote

import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.features.paywall.data.remote.dto.CheckoutResponseDto
import com.iti.careerpilot.features.paywall.data.remote.dto.CoinBalanceResponseDto
import com.iti.careerpilot.features.paywall.data.remote.dto.DowngradeSubscriptionRequestDto
import com.iti.careerpilot.features.paywall.data.remote.dto.PaymentInitiateRequestDto
import com.iti.careerpilot.features.paywall.data.remote.dto.SubscriptionResponseDto
import com.iti.careerpilot.features.paywall.data.remote.dto.TopUpRequestDto
import com.iti.careerpilot.features.paywall.data.remote.dto.PaymentHistoryPageDto
import com.iti.careerpilot.features.paywall.data.remote.dto.UpgradeSubscriptionRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class PaymentRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient,
) : PaymentRemoteDataSource {
    override suspend fun getWalletBalance(): CoinBalanceResponseDto {
        return httpClient.get(Endpoints.WALLET_BALANCE) {
            contentType(ContentType.Application.Json)
        }.body()
    }

    override suspend fun topUpWallet(request: TopUpRequestDto): CheckoutResponseDto {
        return httpClient.post(Endpoints.WALLET_TOP_UP) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun initiatePayment(request: PaymentInitiateRequestDto): CheckoutResponseDto {
        return httpClient.post(Endpoints.PAYMENT_INITIATE) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun getCurrentSubscription(): SubscriptionResponseDto {
        return httpClient.get(Endpoints.SUBSCRIPTION_CURRENT) {
            contentType(ContentType.Application.Json)
        }.body()
    }

    override suspend fun upgradeSubscription(request: UpgradeSubscriptionRequestDto): CheckoutResponseDto {
        return httpClient.post(Endpoints.SUBSCRIPTION_UPGRADE) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun downgradeSubscription(request: DowngradeSubscriptionRequestDto) {
        httpClient.post(Endpoints.SUBSCRIPTION_DOWNGRADE) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun cancelSubscription() {
        httpClient.post(Endpoints.SUBSCRIPTION_CANCEL) {
            contentType(ContentType.Application.Json)
        }
    }

    override suspend fun getPaymentHistory(): PaymentHistoryPageDto {
        return httpClient.get("${Endpoints.PAYMENT_HISTORY}?page=0&size=5") {
            contentType(ContentType.Application.Json)
        }.body()
    }
}
