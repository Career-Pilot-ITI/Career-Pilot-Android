package com.iti.careerpilot.login.data.repository

import com.iti.careerpilot.login.data.mapper.toDomain
import com.iti.careerpilot.login.data.remote.AuthRemoteDataSource
import com.iti.careerpilot.login.data.remote.dto.SendOtpRequest
import com.iti.careerpilot.login.data.remote.dto.VerifyOtpRequest
import com.iti.careerpilot.login.domain.model.AuthSession
import com.iti.careerpilot.login.domain.repository.AuthRepository
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remote: AuthRemoteDataSource,
) : AuthRepository {

    override suspend fun sendOtp(phoneNumber: String): CareerPilotResult<Unit, NetworkError> =
        when (val result = remote.sendOtp(SendOtpRequest(phoneNumber = phoneNumber))) {
            is CareerPilotResult.Success -> CareerPilotResult.Success(Unit)
            is CareerPilotResult.Error -> result
        }

    override suspend fun verifyOtp(
        phoneNumber: String,
        code: String,
    ): CareerPilotResult<AuthSession, NetworkError> =
        when (
            val result = remote.verifyOtp(VerifyOtpRequest(phoneNumber = phoneNumber, code = code))
        ) {
            is CareerPilotResult.Success -> CareerPilotResult.Success(result.data.toDomain())

            is CareerPilotResult.Error -> CareerPilotResult.Error(
                if (result.error == NetworkError.GONE) NetworkError.OTP_EXPIRED else result.error
            )
        }
}
