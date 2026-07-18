package com.iti.careerpilot.login.data.repository

import com.iti.careerpilot.login.data.mapper.toDomain
import com.iti.careerpilot.login.data.remote.AuthDataSource
import com.iti.careerpilot.login.data.remote.dto.SendOtpRequest
import com.iti.careerpilot.login.data.remote.dto.VerifyOtpRequest
import com.iti.careerpilot.login.domain.model.AuthSession
import com.iti.careerpilot.login.domain.repository.AuthRepository
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val dataSource: AuthDataSource,
) : AuthRepository {

    override suspend fun sendOtp(phoneNumber: String): CareerPilotResult<Unit, NetworkError> =
        safeCall {
            dataSource.sendOtp(SendOtpRequest(phoneNumber = phoneNumber))
        }

    override suspend fun verifyOtp(
        phoneNumber: String,
        code: String,
    ): CareerPilotResult<AuthSession, NetworkError> =
        safeCall {
            dataSource.verifyOtp(VerifyOtpRequest(phoneNumber = phoneNumber, code = code)).toDomain()
        }

    inline fun <T> safeCall(block: () -> T): CareerPilotResult<T, NetworkError> =
        try {
            CareerPilotResult.Success(block())
        } catch (_: HttpRequestTimeoutException) {
            CareerPilotResult.Error(NetworkError.TIME_OUT)
        } catch (e: ClientRequestException) {
            CareerPilotResult.Error(
                when (e.response.status) {
                    HttpStatusCode.TooManyRequests -> NetworkError.TOO_MANY_REQUESTS
                    HttpStatusCode.Gone -> NetworkError.OTP_EXPIRED
                    else -> NetworkError.BAD_REQUEST
                }
            )
        } catch (_: ServerResponseException) {
            CareerPilotResult.Error(NetworkError.SERVER)
        } catch (_: SerializationException) {
            CareerPilotResult.Error(NetworkError.SERIALIZATION)
        } catch (_: IOException) {
            CareerPilotResult.Error(NetworkError.NO_INTERNET)
        } catch (_: Exception) {
            CareerPilotResult.Error(NetworkError.UNKNOWN)
        }
}
