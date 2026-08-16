package com.iti.careerpilot.challenges.data.remote

import com.iti.careerpilot.challengefirestore.Challenge
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult

interface ChallengesRemoteDataSource {
    suspend fun getPublicChallenges(): CareerPilotResult<List<Challenge>, FirebaseError>
    suspend fun checkChallengeExists(challengeId: String): CareerPilotResult<Boolean, FirebaseError>
}
