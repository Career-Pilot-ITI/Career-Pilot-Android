package com.iti.careerpilot.createchallenge.data.remote

import com.iti.common.error.FirebaseError
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.Challenge
import com.iti.core.model.Track

interface CreateChallengeRemoteDataSource {
    suspend fun getTracks(): CareerPilotResult<List<Track>, NetworkError>
    suspend fun validateQuestions(questions: List<String>): CareerPilotResult<Boolean, FirebaseError>
    suspend fun saveChallenge(challenge: Challenge): CareerPilotResult<Unit, FirebaseError>
}
