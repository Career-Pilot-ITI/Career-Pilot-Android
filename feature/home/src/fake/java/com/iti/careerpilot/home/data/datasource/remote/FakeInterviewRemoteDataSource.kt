package com.iti.careerpilot.home.data.datasource.remote

import com.iti.careerpilot.home.data.datasource.remote.dto.TrackDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.fakeDelay
import com.iti.common.util.shouldFail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeInterviewRemoteDataSource @Inject constructor() : InterviewRemoteDataSource {

    override suspend fun getTracks(): CareerPilotResult<List<TrackDto>, NetworkError> {
        fakeDelay()
        if (shouldFail()) return CareerPilotResult.Error(NetworkError.FAKE_SERVER_ERROR)

        return CareerPilotResult.Success(
            listOf(
                TrackDto(
                    id = 1L,
                    name = "Android Development",
                    description = "Focuses on building apps for the Android platform.",
                    isActive = true
                ),
                TrackDto(
                    id = 2L,
                    name = "Backend Development",
                    description = "Focuses on server-side logic and databases.",
                    isActive = true
                ),
                TrackDto(
                    id = 3L,
                    name = "UI/UX Design",
                    description = "Focuses on product design and user experience.",
                    isActive = true
                )
            )
        )
    }
}
