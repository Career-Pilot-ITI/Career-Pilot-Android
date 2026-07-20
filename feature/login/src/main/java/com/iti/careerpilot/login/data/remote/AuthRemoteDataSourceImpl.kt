package com.iti.careerpilot.login.data.remote

import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.core.network.util.safeCall
import com.iti.careerpilot.login.data.remote.dto.ApiMessageResponse
import com.iti.careerpilot.login.data.remote.dto.OtpAuthResponse
import com.iti.careerpilot.login.data.remote.dto.SendOtpRequest
import com.iti.careerpilot.login.data.remote.dto.VerifyOtpRequest
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject

class AuthRemoteDataSourceImpl @Inject constructor(
    private val client: HttpClient,
) : AuthRemoteDataSource {

    override suspend fun sendOtp(
        request: SendOtpRequest,
    ): CareerPilotResult<ApiMessageResponse, NetworkError> =
        safeCall {
            client.post(Endpoints.SEND_OTP) {
                setBody(request)
            }
        }

    override suspend fun verifyOtp(
        request: VerifyOtpRequest,
    ): CareerPilotResult<OtpAuthResponse, NetworkError> =
        safeCall {
            client.post(Endpoints.VERIFY_OTP) {
                setBody(request)
            }
        }
}
