package com.iti.careerpilot.login.data.remote

import com.iti.careerpilot.login.data.remote.dto.ApiMessageResponse
import com.iti.careerpilot.login.data.remote.dto.OtpAuthResponse
import com.iti.careerpilot.login.data.remote.dto.SendOtpRequest
import com.iti.careerpilot.login.data.remote.dto.VerifyOtpRequest

interface AuthDataSource {
    suspend fun sendOtp(request: SendOtpRequest): ApiMessageResponse

    suspend fun verifyOtp(request: VerifyOtpRequest): OtpAuthResponse
}