package com.iti.careerpilot.home.domain.repository

import com.iti.careerpilot.home.domain.model.InterviewSession
import com.iti.careerpilot.home.domain.model.InterviewTrack
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult

interface InterviewRepository {

    suspend fun getInterviewSessions(): CareerPilotResult<List<InterviewSession>, NetworkError>

    suspend fun getTracks(): CareerPilotResult<List<InterviewTrack>, NetworkError>
}
