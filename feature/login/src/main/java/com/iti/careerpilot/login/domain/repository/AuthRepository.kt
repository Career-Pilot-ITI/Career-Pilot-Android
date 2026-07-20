package com.iti.careerpilot.login.domain.repository

import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.careerpilot.login.domain.model.AuthSession

interface AuthRepository {

    suspend fun sendOtp(phoneNumber: String): CareerPilotResult<Unit, NetworkError>

    suspend fun verifyOtp(phoneNumber: String, code: String): CareerPilotResult<AuthSession, NetworkError>
}
