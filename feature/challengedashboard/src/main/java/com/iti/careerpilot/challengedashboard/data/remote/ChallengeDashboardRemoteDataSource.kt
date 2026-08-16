package com.iti.careerpilot.challengedashboard.data.remote

import com.iti.careerpilot.challengefirestore.Challenge
import com.iti.careerpilot.challengefirestore.ChallengeSession
import com.iti.careerpilot.challengefirestore.ChallengeVisibility
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult

interface ChallengeDashboardRemoteDataSource {
    suspend fun getCreatedChallenges(creatorId: Long): CareerPilotResult<List<Challenge>, FirebaseError>
    suspend fun getTakenChallenges(participantId: Long): CareerPilotResult<List<ChallengeSession>, FirebaseError>
    suspend fun getChallengeSessions(challengeId: String): CareerPilotResult<List<ChallengeSession>, FirebaseError>
    suspend fun deleteChallenge(challengeId: String, visibility: ChallengeVisibility): CareerPilotResult<Unit, FirebaseError>
}
