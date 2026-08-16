package com.iti.careerpilot.challengefirestore

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.safeFirebaseCall
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ChallengeFirestoreDataSourceImpl @Inject constructor(
    private val json: Json
) : ChallengeFirestoreDataSource {

    private val firestore = Firebase.firestore
    private val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
        modelName = MODEL_NAME,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        },
        systemInstruction = content { text(SYSTEM_INSTRUCTION) }
    )

    override suspend fun getPublicChallenges(): CareerPilotResult<List<Challenge>, FirebaseError> =
        safeFirebaseCall {
            val snapshot = firestore.collection(FirestoreCollections.PUBLIC_CHALLENGES)
                .get()
                .await()
            snapshot.toObjects<Challenge>()
        }

    override suspend fun checkChallengeExists(challengeId: String): CareerPilotResult<Boolean, FirebaseError> =
        safeFirebaseCall {
            var doc = firestore.collection(FirestoreCollections.PUBLIC_CHALLENGES)
                .document(challengeId)
                .get()
                .await()

            if (!doc.exists()) {
                doc = firestore.collection(FirestoreCollections.PRIVATE_CHALLENGES)
                    .document(challengeId)
                    .get()
                    .await()
            }
            doc.exists()
        }

    override suspend fun saveChallenge(challenge: Challenge): CareerPilotResult<Unit, FirebaseError> =
        safeFirebaseCall {
            val collectionName = getCollectionName(challenge.visibility)
            firestore.collection(collectionName)
                .document(challenge.id)
                .set(challenge)
                .await()
        }

    override suspend fun getChallenge(challengeId: String): CareerPilotResult<Challenge, FirebaseError> =
        safeFirebaseCall {
            var snapshot = firestore.collection(FirestoreCollections.PUBLIC_CHALLENGES)
                .document(challengeId)
                .get()
                .await()

            if (!snapshot.exists()) {
                snapshot = firestore.collection(FirestoreCollections.PRIVATE_CHALLENGES)
                    .document(challengeId)
                    .get()
                    .await()
            }

            snapshot.toObject<Challenge>() ?: throw Exception("Empty response")
        }

    override suspend fun deleteChallenge(
        challengeId: String,
        visibility: ChallengeVisibility
    ): CareerPilotResult<Unit, FirebaseError> = safeFirebaseCall {
        val collection = getCollectionName(visibility)
        firestore.collection(collection).document(challengeId).delete().await()
    }

    override suspend fun getCreatedChallenges(creatorId: Long): CareerPilotResult<List<Challenge>, FirebaseError> =
        safeFirebaseCall {
            val public = firestore.collection(FirestoreCollections.PUBLIC_CHALLENGES)
                .whereEqualTo("creatorId", creatorId)
                .get()
                .await()
                .toObjects<Challenge>()

            val private = firestore.collection(FirestoreCollections.PRIVATE_CHALLENGES)
                .whereEqualTo("creatorId", creatorId)
                .get()
                .await()
                .toObjects<Challenge>()

            public + private
        }

    override suspend fun getTakenChallenges(participantId: Long): CareerPilotResult<List<ChallengeSession>, FirebaseError> =
        safeFirebaseCall {
            firestore.collection(FirestoreCollections.CHALLENGE_SESSIONS)
                .whereEqualTo("participantId", participantId)
                .get()
                .await()
                .toObjects<ChallengeSession>()
        }

    override suspend fun getChallengeSessions(challengeId: String): CareerPilotResult<List<ChallengeSession>, FirebaseError> =
        safeFirebaseCall {
            firestore.collection(FirestoreCollections.CHALLENGE_SESSIONS)
                .whereEqualTo("challengeId", challengeId)
                .get()
                .await()
                .toObjects<ChallengeSession>()
        }

    override suspend fun validateQuestions(questions: List<String>): CareerPilotResult<Boolean, FirebaseError> =
        safeFirebaseCall {
            val prompt = """
            Validate if the following list of questions are valid technical interview questions:
            ${questions.joinToString(separator = "\n") { "- $it" }}
            
            Return a JSON object:
            {
              "isValid": boolean,
              "reason": "string (optional, if not valid)"
            }
        """.trimIndent()

            val response = model.generateContent(prompt)
            val text = response.text  ?: throw Exception("Empty AI response")
            val result = json.decodeFromString<AiValidationResponse>(cleanJson(text))
            result.isValid
        }

    override fun generateChallengeId(visibility: ChallengeVisibility): String {
        return firestore.collection(getCollectionName(visibility)).document().id.uppercase()
    }

    private fun getCollectionName(visibility: ChallengeVisibility): String {
        return if (visibility == ChallengeVisibility.PUBLIC) {
            FirestoreCollections.PUBLIC_CHALLENGES
        } else {
            FirestoreCollections.PRIVATE_CHALLENGES
        }
    }

    private fun cleanJson(jsonString: String): String {
        return jsonString
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    @kotlinx.serialization.Serializable
    private data class AiValidationResponse(
        val isValid: Boolean,
        val reason: String? = null
    )

    companion object {
        private const val MODEL_NAME = "gemini-3.1-flash-lite"
        private const val SYSTEM_INSTRUCTION = """
            You are a technical interview expert. Your task is to validate a list of interview questions.
            A valid technical question should be related to programming, software engineering, system design, data science, or other IT fields.
            General questions like "What is your name?" or "How are you?" are NOT valid technical questions for this purpose.
            Return 'isValid: true' only if ALL questions in the list are technical.
            Otherwise, return 'isValid: false' and provide a reason.
        """
    }
}
