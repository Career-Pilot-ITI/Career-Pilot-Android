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
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.util.safeFirebaseCall
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import java.time.Instant
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

    override suspend fun createSession(
        challenge: Challenge,
        participantId: Long,
        participantEmail: String,
        participantName: String
    ): CareerPilotResult<ChallengeSession, FirebaseError> = safeFirebaseCall {
        val sessionId = firestore.collection(FirestoreCollections.CHALLENGE_SESSIONS).document().id
        val firstQuestion = challenge.questions.firstOrNull()
        val now = Instant.now().toString()

        val session = ChallengeSession(
            sessionId = sessionId,
            challengeId = challenge.id,
            challengeTitle = challenge.trackName,
            participantId = participantId,
            participantEmail = participantEmail,
            participantName = participantName,
            trackName = challenge.trackName,
            status = "IN_PROGRESS",
            targetDurationMinutes = challenge.questions.size * 2, // 2 mins per question
            maxQuestions = challenge.questions.size,
            answeredCount = 0,
            startedAt = now,
            updatedAt = now,
            timestamp = System.currentTimeMillis(),
            currentQuestion = firstQuestion?.let {
                ChallengeCurrentQuestion(
                    id = it.id,
                    questionText = it.text,
                    questionOrder = 1,
                    createdAt = now
                )
            }
        )

        firestore.collection(FirestoreCollections.CHALLENGE_SESSIONS)
            .document(sessionId)
            .set(session)
            .await()

        session
    }

    override suspend fun submitAnswer(
        sessionId: String,
        questionResult: ChallengeQuestionResult,
        audioBytes: ByteArray?,
        mimeType: String?
    ): CareerPilotResult<ChallengeSession, FirebaseError> = safeFirebaseCall {
        val docRef = firestore.collection(FirestoreCollections.CHALLENGE_SESSIONS).document(sessionId)
        val session = docRef.get().await().toObject<ChallengeSession>()
            ?: throw Exception("Session not found")

        // 1. Score the answer using Gemini (Multimodal)
        val scoringPrompt = """
            Score the following technical interview answer. 
            Analyze both the provided transcript and the audio recording for:
            - Content Relevance (alignment with the question)
            - Clarity (articulation and technical accuracy)
            - Confidence (tone and flow)
            - Pacing (speech rate and pauses)
            - Filler Words (frequency of 'uh', 'um', etc.)
            
            Question: ${questionResult.questionText}
            Transcript: ${questionResult.userTranscript}
            
            Provide scores (0-100) and a short coaching tip.
            Return ONLY a JSON object:
            {
              "contentRelevance": number,
              "clarity": number,
              "confidence": number,
              "pacing": number,
              "fillerWords": number,
              "overallScore": number,
              "coachingTip": "string"
            }
        """.trimIndent()

        val response = if (audioBytes != null) {
            model.generateContent(
                listOf(
                    content {
                        inlineData(audioBytes, mimeType ?: "audio/wav")
                        text(scoringPrompt)
                    }
                )
            )
        } else {
            model.generateContent(scoringPrompt)
        }

        val text = response.text ?: throw Exception("Empty AI response")
        val score = json.decodeFromString<ChallengeScore>(cleanJson(text)).copy(
            createdAt = Instant.now().toString()
        )

        // 2. Update question result with score
        val updatedQuestionResult = questionResult.copy(score = score)
        val updatedResults = session.results + updatedQuestionResult
        val nextOrder = session.answeredCount + 1
        
        // Find next question from original challenge
        val challenge = when (val challengeResult = getChallenge(session.challengeId)) {
            is CareerPilotResult.Success -> challengeResult.data
            is CareerPilotResult.Error -> throw Exception("Challenge not found")
        }
        val nextQuestion = challenge.questions.getOrNull(session.answeredCount + 1)

        val isCompleted = nextQuestion == null
        val now = Instant.now().toString()

        // 3. Calculate overall scores if completed
        var finalSession = session.copy(
            results = updatedResults,
            answeredCount = session.answeredCount + 1,
            updatedAt = now,
            status = if (isCompleted) "COMPLETED" else "IN_PROGRESS",
            currentQuestion = nextQuestion?.let {
                ChallengeCurrentQuestion(
                    id = it.id,
                    questionText = it.text,
                    questionOrder = nextOrder,
                    createdAt = now
                )
            }
        )

        if (isCompleted) {
            val avgClarity = updatedResults.map { it.score?.clarity ?: 0 }.average().toInt()
            val avgConfidence = updatedResults.map { it.score?.confidence ?: 0 }.average().toInt()
            val avgPacing = updatedResults.map { it.score?.pacing ?: 0 }.average().toInt()
            val avgFiller = updatedResults.map { it.score?.fillerWords ?: 0 }.average().toInt()
            val avgRelevance = updatedResults.map { it.score?.contentRelevance ?: 0 }.average().toInt()
            val avgOverall = updatedResults.map { it.score?.overallScore ?: 0 }.average().toInt()

            finalSession = finalSession.copy(
                clarityScore = avgClarity,
                confidenceScore = avgConfidence,
                pacingScore = avgPacing,
                fillerWordsScore = avgFiller,
                contentRelevanceScore = avgRelevance,
                overallScore = avgOverall,
                coachingTips = updatedResults.mapNotNull { it.score?.coachingTip }.filter { it.isNotBlank() }
            )
        }

        docRef.set(finalSession).await()
        finalSession
    }

    override suspend fun getSession(sessionId: String): CareerPilotResult<ChallengeSession, FirebaseError> =
        safeFirebaseCall {
            firestore.collection(FirestoreCollections.CHALLENGE_SESSIONS)
                .document(sessionId)
                .get()
                .await()
                .toObject<ChallengeSession>() ?: throw Exception("Session not found")
        }

    override suspend fun updateSession(session: ChallengeSession): CareerPilotResult<Unit, FirebaseError> =
        safeFirebaseCall {
            firestore.collection(FirestoreCollections.CHALLENGE_SESSIONS)
                .document(session.sessionId)
                .set(session)
                .await()
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
        private const val MODEL_NAME = "gemini-3.5-flash-lite"
        private const val SYSTEM_INSTRUCTION = """
            You are a technical interview expert. Your task is to validate a list of interview questions.
            A valid technical question should be related to programming, software engineering, system design, data science, or other IT fields.
            General questions like "What is your name?" or "How are you?" are NOT valid technical questions for this purpose.
            Return 'isValid: true' only if ALL questions in the list are technical.
            Otherwise, return 'isValid: false' and provide a reason.
        """
    }
}
