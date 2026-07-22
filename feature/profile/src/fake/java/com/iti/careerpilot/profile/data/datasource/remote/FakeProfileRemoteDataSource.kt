package com.iti.careerpilot.profile.data.datasource.remote

import com.iti.careerpilot.core.network.model.UserProfileDto
import com.iti.careerpilot.profile.domain.datasource.remote.ProfileRemoteDataSource
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.fakeDelay
import com.iti.common.util.shouldFail
import javax.inject.Inject

class FakeProfileRemoteDataSource @Inject constructor() : ProfileRemoteDataSource {
    override suspend fun getProfile(): CareerPilotResult<UserProfileDto, NetworkError> {
        fakeDelay()
        if (shouldFail()) return CareerPilotResult.Error(NetworkError.FAKE_SERVER_ERROR)
        return CareerPilotResult.Success(
            UserProfileDto(
                id = 1L,
                displayName = "Fake User",
                username = "fake_user",
                email = "fake@example.com",
                onboardingCompleted = true
            )
        )
    }
}
