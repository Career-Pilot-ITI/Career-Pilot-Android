package com.iti.careerpilot.features.paywall.data.repository

import android.util.Log
import com.iti.careerpilot.features.paywall.data.mapper.*
import com.iti.careerpilot.features.paywall.data.remote.PaymentRemoteDataSource
import com.iti.careerpilot.features.paywall.data.remote.dto.*
import com.iti.core.model.*
import com.iti.careerpilot.features.paywall.domain.repository.PaymentRepository
import com.iti.common.dispatcher.CareerPilotDispatchers.IO
import com.iti.common.dispatcher.Dispatcher
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import com.iti.core.datastore.repo.UserProfileRepo
import java.nio.channels.UnresolvedAddressException
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val remoteDataSource: PaymentRemoteDataSource,
    private val userProfileRepo: UserProfileRepo,
    @param:Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : PaymentRepository {

    override suspend fun getWalletBalance(): CareerPilotResult<WalletBalance, NetworkError> =
        safeNetworkCall {
            remoteDataSource.getWalletBalance().toDomain()
        }

    override suspend fun topUpWallet(
        coinPackSize: Int,
        currency: String,
        method: String
    ): CareerPilotResult<CheckoutSession, NetworkError> =
        safeNetworkCall {
            remoteDataSource.topUpWallet(
                TopUpRequestDto(
                    coinPackSize = coinPackSize,
                    currency = currency,
                    method = method
                )
            ).toDomain()
        }

    override suspend fun initiatePayment(
        amount: Double,
        currency: String,
        method: String,
        provider: String,
        purchaseType: String,
        coinPackSize: Int?,
        tier: String?
    ): CareerPilotResult<CheckoutSession, NetworkError> =
        safeNetworkCall {
            remoteDataSource.initiatePayment(
                PaymentInitiateRequestDto(
                    amount = amount,
                    currency = currency,
                    method = method,
                    provider = provider,
                    purchaseType = purchaseType,
                    coinPackSize = coinPackSize,
                    tier = tier
                )
            ).toDomain()
        }

    override suspend fun getCurrentSubscription(): CareerPilotResult<SubscriptionInfo, NetworkError> {
        return when (val result = safeNetworkCall { remoteDataSource.getCurrentSubscription().toDomain() }) {
            is CareerPilotResult.Success -> result
            is CareerPilotResult.Error -> {
                val cachedProfile = userProfileRepo.readUserProfile()
                if (cachedProfile.account.subscriptionTier.isNotBlank()) {
                    CareerPilotResult.Success(
                        SubscriptionInfo(
                            tier = cachedProfile.account.subscriptionTier,
                            isActive = true,
                            startedAt = null,
                            renewalDate = null,
                            cancelledAt = null,
                            pendingTier = null
                        )
                    )
                } else {
                    result
                }
            }
        }
    }

    override suspend fun upgradeSubscription(
        tier: String,
        currency: String,
        method: String
    ): CareerPilotResult<CheckoutSession, NetworkError> =
        safeNetworkCall {
            val mappedTier = when (tier.uppercase()) {
                "MAX", "PRO" -> "PRO"
                "PLUS" -> "PLUS"
                else -> "FREE"
            }
            remoteDataSource.upgradeSubscription(
                UpgradeSubscriptionRequestDto(
                    tier = mappedTier,
                    currency = currency,
                    method = method
                )
            ).toDomain()
        }

    override suspend fun downgradeSubscription(
        tier: String
    ): CareerPilotResult<Unit, NetworkError> =
        safeNetworkCall {
            val mappedTier = when (tier.uppercase()) {
                "MAX", "PRO" -> "PRO"
                "PLUS" -> "PLUS"
                else -> "FREE"
            }
            remoteDataSource.downgradeSubscription(
                DowngradeSubscriptionRequestDto(
                    tier = mappedTier
                )
            )
        }

    override suspend fun cancelSubscription(): CareerPilotResult<Unit, NetworkError> =
        safeNetworkCall {
            remoteDataSource.cancelSubscription()
        }

    override suspend fun getSubscriptionTiers(): CareerPilotResult<Map<String, Double>, NetworkError> =
        safeNetworkCall {
            remoteDataSource.getSubscriptionTiers()
        }

    override suspend fun getCoinPacks(): CareerPilotResult<Map<Int, Double>, NetworkError> =
        safeNetworkCall {
            remoteDataSource.getCoinPacks()
        }

    override suspend fun confirmPayment(merchantOrderId: String): CareerPilotResult<Unit, NetworkError> =
        safeNetworkCall {
            remoteDataSource.confirmPayment(merchantOrderId)
        }

    private suspend fun <T> safeNetworkCall(
        block: suspend () -> T,
    ): CareerPilotResult<T, NetworkError> =
        withContext(ioDispatcher) {
            try {
                CareerPilotResult.Success(
                    data = block(),
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: ClientRequestException) {
                logClientRequestException(exception)

                val error = when (exception.response.status.value) {
                    401 -> NetworkError.UNAUTHORIZED
                    403 -> NetworkError.FORBIDDEN
                    404 -> NetworkError.NOT_FOUND
                    409 -> NetworkError.CONFLICT
                    else -> NetworkError.BAD_REQUEST
                }
                CareerPilotResult.Error(error = error)
            } catch (_: ServerResponseException) {
                CareerPilotResult.Error(
                    error = NetworkError.SERVER,
                )
            } catch (_: SerializationException) {
                CareerPilotResult.Error(
                    error = NetworkError.SERIALIZATION,
                )
            } catch (_: UnresolvedAddressException) {
                CareerPilotResult.Error(
                    error = NetworkError.NO_INTERNET,
                )
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Unexpected payment repository error",
                    exception,
                )

                CareerPilotResult.Error(
                    error = NetworkError.UNKNOWN,
                )
            }
        }

    private suspend fun logClientRequestException(
        exception: ClientRequestException,
    ) {
        val responseBody = runCatching {
            exception.response.bodyAsText()
        }.getOrNull()

        Log.e(
            TAG,
            buildString {
                append("Client request failed. ")
                append("Status: ${exception.response.status}")

                if (!responseBody.isNullOrBlank()) {
                    append(", Body: $responseBody")
                }
            },
            exception,
        )
    }

    private companion object {
        const val TAG = "PaymentRepository"
    }
}
