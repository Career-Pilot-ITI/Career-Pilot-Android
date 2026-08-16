package com.iti.careerpilot.challengefirestore

import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult

interface ChallengeFirestoreDataSource {
    suspend fun getPublicChallenges(): CareerPilotResult<List<Challenge>, FirebaseError>
    suspend fun checkChallengeExists(challengeId: String): CareerPilotResult<Boolean, FirebaseError>
    suspend fun saveChallenge(challenge: Challenge): CareerPilotResult<Unit, FirebaseError>
    suspend fun getChallenge(challengeId: String): CareerPilotResult<Challenge, FirebaseError>
    suspend fun deleteChallenge(challengeId: String, visibility: ChallengeVisibility): CareerPilotResult<Unit, FirebaseError>
    suspend fun getCreatedChallenges(creatorId: Long): CareerPilotResult<List<Challenge>, FirebaseError>
    suspend fun getTakenChallenges(participantId: Long): CareerPilotResult<List<ChallengeSession>, FirebaseError>
    suspend fun getChallengeSessions(challengeId: String): CareerPilotResult<List<ChallengeSession>, FirebaseError>
    suspend fun validateQuestions(questions: List<String>): CareerPilotResult<Boolean, FirebaseError>
    fun generateChallengeId(visibility: ChallengeVisibility): String
}
