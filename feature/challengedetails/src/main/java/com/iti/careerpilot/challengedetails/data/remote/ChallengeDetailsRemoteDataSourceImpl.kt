package com.iti.careerpilot.challengedetails.data.remote

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.safeFirebaseCall
import com.iti.core.model.Challenge
import com.iti.core.model.FirestoreCollections
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChallengeDetailsRemoteDataSourceImpl @Inject constructor() : ChallengeDetailsRemoteDataSource {

    private val firestore = Firebase.firestore

    override suspend fun getChallenge(challengeId: String): CareerPilotResult<Challenge, FirebaseError> = safeFirebaseCall {
        // Try public first
        var snapshot = firestore.collection(FirestoreCollections.PUBLIC_CHALLENGES)
            .document(challengeId)
            .get()
            .await()

        if (!snapshot.exists()) {
            // Try private
            snapshot = firestore.collection(FirestoreCollections.PRIVATE_CHALLENGES)
                .document(challengeId)
                .get()
                .await()
        }

        if (snapshot.exists()) {
            snapshot.toObject(Challenge::class.java) ?: throw Exception("Failed to parse challenge")
        } else {
            throw Exception("Challenge not found")
        }
    }
}
