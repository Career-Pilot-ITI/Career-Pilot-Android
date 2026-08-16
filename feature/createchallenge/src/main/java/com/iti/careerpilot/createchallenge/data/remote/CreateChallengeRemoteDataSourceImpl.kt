package com.iti.careerpilot.createchallenge.data.remote

import com.iti.careerpilot.challengefirestore.Challenge
import com.iti.careerpilot.challengefirestore.ChallengeFirestoreDataSource
import com.iti.careerpilot.challengefirestore.ChallengeVisibility
import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.core.network.util.safeCall
import com.iti.common.error.FirebaseError
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.result.map
import com.iti.core.model.Track
import com.iti.core.model.TrackDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import javax.inject.Inject

class CreateChallengeRemoteDataSourceImpl @Inject constructor(
    private val client: HttpClient,
    private val firestoreDataSource: ChallengeFirestoreDataSource
) : CreateChallengeRemoteDataSource {

    override suspend fun getTracks(): CareerPilotResult<List<Track>, NetworkError> = safeCall<List<TrackDto>> {
        client.get(Endpoints.GET_TRACKS)
    }.map { trackList ->
        trackList.mapNotNull { dto ->
            dto.id?.let { id ->
                dto.name?.let { name ->
                    Track(
                        id, name
                    )
                }
            }
        }
    }

    override suspend fun validateQuestions(questions: List<String>): CareerPilotResult<Boolean, FirebaseError> =
        firestoreDataSource.validateQuestions(questions)

    override suspend fun saveChallenge(challenge: Challenge): CareerPilotResult<Unit, FirebaseError> {
        return firestoreDataSource.saveChallenge(challenge)
    }

    override suspend fun getChallenge(challengeId: String): CareerPilotResult<Challenge, FirebaseError> {
        return firestoreDataSource.getChallenge(challengeId)
    }

    override suspend fun deleteChallenge(challengeId: String, visibility: ChallengeVisibility): CareerPilotResult<Unit, FirebaseError> {
        return firestoreDataSource.deleteChallenge(challengeId, visibility)
    }

    override fun generateChallengeId(visibility: ChallengeVisibility): String {
        return firestoreDataSource.generateChallengeId(visibility)
    }
}
