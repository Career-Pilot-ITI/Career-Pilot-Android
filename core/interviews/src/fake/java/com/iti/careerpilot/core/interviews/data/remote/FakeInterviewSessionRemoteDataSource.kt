package com.iti.careerpilot.core.interviews.data.remote

import com.iti.careerpilot.core.interviews.data.remote.dto.InterviewSessionDto
import com.iti.careerpilot.core.interviews.data.remote.dto.InterviewSessionPageDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.fakeDelay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeInterviewSessionRemoteDataSource @Inject constructor() : InterviewSessionRemoteDataSource {

    override suspend fun getSessions(
        page: Int,
        size: Int,
    ): CareerPilotResult<InterviewSessionPageDto, NetworkError> {
        fakeDelay()
        if (page < 0 || size <= 0) {
            return CareerPilotResult.Error(NetworkError.BAD_REQUEST)
        }

        val all = FakeInterviewSessions.sessions
        val totalElements = all.size
        val totalPages = if (totalElements == 0) 0 else (totalElements + size - 1) / size
        val fromIndex = minOf(page.toLong() * size, totalElements.toLong()).toInt()
        val toIndex = minOf(fromIndex.toLong() + size, totalElements.toLong()).toInt()

        return CareerPilotResult.Success(
            InterviewSessionPageDto(
                content = all.subList(fromIndex, toIndex),
                number = page,
                size = size,
                totalPages = totalPages,
                totalElements = totalElements.toLong(),
                first = page == 0,
                last = totalPages == 0 || page >= totalPages - 1,
            ),
        )
    }

    override suspend fun getSession(
        sessionId: Long,
    ): CareerPilotResult<InterviewSessionDto, NetworkError> {
        fakeDelay()
        return FakeInterviewSessions.findById(sessionId)
            ?.let { CareerPilotResult.Success(it) }
            ?: CareerPilotResult.Error(NetworkError.BAD_REQUEST)
    }
}
