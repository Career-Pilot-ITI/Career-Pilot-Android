package com.iti.careerpilot.challengedashboard.data.remote

import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.Challenge
import com.iti.core.model.ChallengeSession
import com.iti.core.model.ChallengeVisibility

interface ChallengeDashboardRemoteDataSource {
    suspend fun getCreatedChallenges(creatorId: Long): CareerPilotResult<List<Challenge>, FirebaseError>
    suspend fun getTakenChallenges(participantId: Long): CareerPilotResult<List<ChallengeSession>, FirebaseError>
    suspend fun getChallengeSessions(challengeId: String): CareerPilotResult<List<ChallengeSession>, FirebaseError>
    suspend fun deleteChallenge(challengeId: String, visibility: ChallengeVisibility): CareerPilotResult<Unit, FirebaseError>
}
