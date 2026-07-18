package com.iti.careerpilot.login.domain.usecase

import com.iti.careerpilot.login.domain.repository.AuthRepository
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import javax.inject.Inject

class SendOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(phoneNumber: String): CareerPilotResult<Unit, NetworkError> =
        authRepository.sendOtp(phoneNumber)
}
