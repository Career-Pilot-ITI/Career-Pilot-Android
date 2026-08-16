package com.iti.careerpilot.challenges.data.remote

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.safeFirebaseCall
import com.iti.core.model.Challenge
import com.iti.core.model.FirestoreCollections
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChallengesRemoteDataSourceImpl @Inject constructor() : ChallengesRemoteDataSource {

    private val firestore = Firebase.firestore

    override suspend fun getPublicChallenges(): CareerPilotResult<List<Challenge>, FirebaseError> = safeFirebaseCall {
        val snapshot = firestore.collection(FirestoreCollections.PUBLIC_CHALLENGES)
            .get()
            .await()
        snapshot.toObjects(Challenge::class.java)
    }

    override suspend fun checkChallengeExists(challengeId: String): CareerPilotResult<Boolean, FirebaseError> = safeFirebaseCall {
        // Check public first
        var doc = firestore.collection(FirestoreCollections.PUBLIC_CHALLENGES)
            .document(challengeId)
            .get()
            .await()
            
        if (!doc.exists()) {
            // Check private
            doc = firestore.collection(FirestoreCollections.PRIVATE_CHALLENGES)
                .document(challengeId)
                .get()
                .await()
        }
        
        doc.exists()
    }
}
