package com.iti.careerpilot.challengedashboard.data.remote

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.safeFirebaseCall
import com.iti.core.model.Challenge
import com.iti.core.model.ChallengeSession
import com.iti.core.model.ChallengeVisibility
import com.iti.core.model.FirestoreCollections
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChallengeDashboardRemoteDataSourceImpl @Inject constructor() :
    ChallengeDashboardRemoteDataSource {

    private val firestore = Firebase.firestore

    override suspend fun getCreatedChallenges(creatorId: Long): CareerPilotResult<List<Challenge>, FirebaseError> =
        safeFirebaseCall {
            val public = firestore.collection(FirestoreCollections.PUBLIC_CHALLENGES)
                .whereEqualTo("creatorId", creatorId)
                .get()
                .await()
                .toObjects(Challenge::class.java)

            val private = firestore.collection(FirestoreCollections.PRIVATE_CHALLENGES)
                .whereEqualTo("creatorId", creatorId)
                .get()
                .await()
                .toObjects(Challenge::class.java)

            public + private
        }

    override suspend fun getTakenChallenges(participantId: Long): CareerPilotResult<List<ChallengeSession>, FirebaseError> =
        safeFirebaseCall {
            firestore.collection(FirestoreCollections.CHALLENGE_SESSIONS)
                .whereEqualTo("participantId", participantId)
                .get()
                .await()
                .toObjects(ChallengeSession::class.java)
        }

    override suspend fun getChallengeSessions(challengeId: String): CareerPilotResult<List<ChallengeSession>, FirebaseError> =
        safeFirebaseCall {
            firestore.collection(FirestoreCollections.CHALLENGE_SESSIONS)
                .whereEqualTo("challengeId", challengeId)
                .get()
                .await()
                .toObjects(ChallengeSession::class.java)
        }

    override suspend fun deleteChallenge(
        challengeId: String,
        visibility: ChallengeVisibility
    ): CareerPilotResult<Unit, FirebaseError> = safeFirebaseCall {
        val collection =
            if (visibility == ChallengeVisibility.PUBLIC) FirestoreCollections.PUBLIC_CHALLENGES
            else FirestoreCollections.PRIVATE_CHALLENGES
        firestore.collection(collection)
            .document(challengeId)
            .delete()
            .await()
    }
}
