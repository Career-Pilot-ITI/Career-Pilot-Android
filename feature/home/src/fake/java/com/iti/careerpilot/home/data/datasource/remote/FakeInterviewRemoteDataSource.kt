package com.iti.careerpilot.home.data.datasource.remote

import com.iti.careerpilot.home.data.datasource.remote.dto.InterviewSessionDto
import com.iti.careerpilot.home.data.datasource.remote.dto.TrackDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.fakeDelay
import com.iti.common.util.shouldFail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeInterviewRemoteDataSource @Inject constructor() : InterviewRemoteDataSource {

    override suspend fun getInterviewSessions(): CareerPilotResult<List<InterviewSessionDto>, NetworkError> {
        fakeDelay()
        if (shouldFail()) return CareerPilotResult.Error(NetworkError.FAKE_SERVER_ERROR)

        return CareerPilotResult.Success(
            listOf(
                InterviewSessionDto(
                    id = 1L,
                    trackId = 1L,
                    trackName = "Android Developer",
                    status = "COMPLETED",
                    overallScore = 85,
                    durationSeconds = 600,
                    targetDurationMinutes = 15,
                    maxQuestions = 10,
                    startedAt = "2023-10-20T10:00:00Z",
                    completedAt = "2023-10-20T10:10:00Z",
                    createdAt = "2023-10-20T10:00:00Z"
                ),
                InterviewSessionDto(
                    id = 2L,
                    trackId = 2L,
                    trackName = "iOS Developer",
                    status = "IN_PROGRESS",
                    overallScore = null,
                    durationSeconds = 300,
                    targetDurationMinutes = 20,
                    maxQuestions = 12,
                    startedAt = "2023-10-21T11:00:00Z",
                    completedAt = null,
                    createdAt = "2023-10-21T11:00:00Z"
                )
            )
        )
    }

    override suspend fun getTracks(): CareerPilotResult<List<TrackDto>, NetworkError> {
        fakeDelay()
        if (shouldFail()) return CareerPilotResult.Error(NetworkError.FAKE_SERVER_ERROR)

        return CareerPilotResult.Success(
            listOf(
                TrackDto(
                    id = 1L,
                    name = "Android Developer",
                    description = "Focuses on building apps for the Android platform.",
                    isActive = true
                ),
                TrackDto(
                    id = 2L,
                    name = "iOS Developer",
                    description = "Focuses on building apps for the iOS platform.",
                    isActive = true
                ),
                TrackDto(
                    id = 3L,
                    name = "Backend Developer",
                    description = "Focuses on server-side logic and databases.",
                    isActive = true
                )
            )
        )
    }
}
