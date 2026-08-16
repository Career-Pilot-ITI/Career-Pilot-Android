package com.iti.careerpilot.createchallenge.domain.repository

import com.iti.careerpilot.challengefirestore.Challenge
import com.iti.careerpilot.challengefirestore.ChallengeVisibility
import com.iti.common.error.FirebaseError
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.Track

interface CreateChallengeRepository {
    suspend fun getTracks(): CareerPilotResult<List<Track>, NetworkError>
    suspend fun validateQuestions(questions: List<String>): CareerPilotResult<Boolean, FirebaseError>
    suspend fun createChallenge(challenge: Challenge): CareerPilotResult<Unit, FirebaseError>
    suspend fun getChallenge(challengeId: String): CareerPilotResult<Challenge, FirebaseError>
    suspend fun deleteChallenge(challengeId: String, visibility: ChallengeVisibility): CareerPilotResult<Unit, FirebaseError>
    fun generateChallengeId(visibility: ChallengeVisibility): String
}
