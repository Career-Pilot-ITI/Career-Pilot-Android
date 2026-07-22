package com.iti.careerpilot.login.data.remote

import com.iti.careerpilot.core.network.model.AuthTokensDto
import com.iti.careerpilot.login.data.remote.dto.ApiMessageResponse
import com.iti.careerpilot.login.data.remote.dto.AuthUserDto
import com.iti.careerpilot.login.data.remote.dto.OtpAuthResponse
import com.iti.careerpilot.login.data.remote.dto.SendOtpRequest
import com.iti.careerpilot.login.data.remote.dto.VerifyOtpRequest
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.fakeDelay
import com.iti.common.util.shouldFail
import javax.inject.Inject

class FakeAuthRemoteDataSource @Inject constructor() : AuthRemoteDataSource {
    override suspend fun sendOtp(request: SendOtpRequest): CareerPilotResult<ApiMessageResponse, NetworkError> {
        fakeDelay()
        if (shouldFail()) return CareerPilotResult.Error(NetworkError.FAKE_SERVER_ERROR)
        return CareerPilotResult.Success(ApiMessageResponse(message = "OTP sent successfully", success = true))
    }

    override suspend fun verifyOtp(request: VerifyOtpRequest): CareerPilotResult<OtpAuthResponse, NetworkError> {
        fakeDelay()
        if (shouldFail()) return CareerPilotResult.Error(NetworkError.FAKE_SERVER_ERROR)
        return CareerPilotResult.Success(
            OtpAuthResponse(
                authTokens = AuthTokensDto(
                    accessToken = "fake_access_token",
                    refreshToken = "fake_refresh_token",
                    expiresIn = 3600L
                ),
                user = AuthUserDto(
                    id = 1L,
                    phoneNumber = request.phoneNumber,
                    newUser = false
                )
            )
        )
    }
}
