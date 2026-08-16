package com.iti.careerpilot.core.access.data.remote

import com.iti.careerpilot.core.access.data.remote.dto.SubscriptionStatusDto
import com.iti.careerpilot.core.network.Endpoints
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
internal data class WalletBalanceDto(
    @SerialName("balance") val balance: Int = 0
)

class AccessRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient
) : AccessRemoteDataSource {

    override suspend fun getSubscriptionStatus(): SubscriptionStatusDto {
        return httpClient.get(Endpoints.SUBSCRIPTION_CURRENT).body()
    }

    override suspend fun getWalletBalance(): Int {
        return runCatching {
            httpClient.get(Endpoints.WALLET_BALANCE).body<WalletBalanceDto>().balance
        }.getOrDefault(0)
    }
}
