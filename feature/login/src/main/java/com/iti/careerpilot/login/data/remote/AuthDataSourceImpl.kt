package com.iti.careerpilot.login.data.remote

import com.iti.careerpilot.login.data.remote.dto.ApiMessageResponse
import com.iti.careerpilot.login.data.remote.dto.OtpAuthResponse
import com.iti.careerpilot.login.data.remote.dto.SendOtpRequest
import com.iti.careerpilot.login.data.remote.dto.VerifyOtpRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject

class AuthDataSourceImpl @Inject constructor(
    private val client: HttpClient,
) : AuthDataSource {

    override suspend fun sendOtp(request: SendOtpRequest): ApiMessageResponse =
        client.post("api/v1/otp/send") {
            setBody(request)
        }.body()

    override suspend fun verifyOtp(request: VerifyOtpRequest): OtpAuthResponse =
        client.post("api/v1/otp/verify") {
            setBody(request)
        }.body()
}

