package com.iti.careerpilot.challenges.domain.repository

import com.iti.careerpilot.challengefirestore.Challenge
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult

interface ChallengesRepository {
    suspend fun getPublicChallenges(): CareerPilotResult<List<Challenge>, FirebaseError>
    suspend fun checkChallengeExists(challengeId: String): CareerPilotResult<Boolean, FirebaseError>
}
