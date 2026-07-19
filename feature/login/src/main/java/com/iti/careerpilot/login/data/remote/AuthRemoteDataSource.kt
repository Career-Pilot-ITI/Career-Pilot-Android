package com.iti.careerpilot.login.data.remote

import com.iti.careerpilot.login.data.remote.dto.ApiMessageResponse
import com.iti.careerpilot.login.data.remote.dto.OtpAuthResponse
import com.iti.careerpilot.login.data.remote.dto.SendOtpRequest
import com.iti.careerpilot.login.data.remote.dto.VerifyOtpRequest
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult

interface AuthRemoteDataSource {
    suspend fun sendOtp(request: SendOtpRequest): CareerPilotResult<ApiMessageResponse, NetworkError>

    suspend fun verifyOtp(request: VerifyOtpRequest): CareerPilotResult<OtpAuthResponse, NetworkError>
}
