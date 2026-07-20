package com.iti.careerpilot.login.domain.usecase

import com.iti.careerpilot.login.domain.model.AuthSession
import com.iti.careerpilot.login.domain.repository.AuthRepository
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        phoneNumber: String,
        code: String,
    ): CareerPilotResult<AuthSession, NetworkError> =
        authRepository.verifyOtp(phoneNumber, code)
}
