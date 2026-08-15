package com.iti.careerpilot.createchallenge.domain.repository

import com.iti.common.error.FirebaseError
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.Challenge
import com.iti.core.model.Track

interface CreateChallengeRepository {
    suspend fun getTracks(): CareerPilotResult<List<Track>, NetworkError>
    suspend fun validateQuestions(questions: List<String>): CareerPilotResult<Boolean, FirebaseError>
    suspend fun createChallenge(challenge: Challenge): CareerPilotResult<Unit, FirebaseError>
}
