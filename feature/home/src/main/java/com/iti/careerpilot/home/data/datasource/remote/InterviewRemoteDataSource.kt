package com.iti.careerpilot.home.data.datasource.remote

import com.iti.careerpilot.home.data.datasource.remote.dto.InterviewSessionDto
import com.iti.careerpilot.home.data.datasource.remote.dto.TrackDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult

interface InterviewRemoteDataSource {

    suspend fun getInterviewSessions(): CareerPilotResult<List<InterviewSessionDto>, NetworkError>

    suspend fun getTracks(): CareerPilotResult<List<TrackDto>, NetworkError>
}
