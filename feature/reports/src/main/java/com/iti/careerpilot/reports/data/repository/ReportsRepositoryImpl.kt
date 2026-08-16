package com.iti.careerpilot.reports.data.repository

import com.iti.careerpilot.core.interviews.domain.repository.InterviewSessionRepository
import com.iti.careerpilot.reports.data.datasource.remote.ReportsRemoteDataSource
import com.iti.careerpilot.reports.data.mapper.toDomain
import com.iti.careerpilot.reports.domain.model.QuestionBreakdown
import com.iti.careerpilot.reports.domain.model.ReportDetails
import com.iti.careerpilot.reports.domain.repository.ReportsRepository
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.SerializationException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class ReportsRepositoryImpl @Inject constructor(
    private val remoteDataSource: ReportsRemoteDataSource,
    private val sessionRepository: InterviewSessionRepository,
) : ReportsRepository {

    override suspend fun getReportDetails(
        sessionId: Long,
    ): CareerPilotResult<ReportDetails, NetworkError> = coroutineScope {
        val sessionDeferred = async { sessionRepository.getSession(sessionId) }
        val feedbackDeferred = async { remoteDataSource.getFeedback(sessionId) }

        val sessionResult = sessionDeferred.await()
        val feedbackResult = feedbackDeferred.await()

        when {
            sessionResult is CareerPilotResult.Error -> CareerPilotResult.Error(sessionResult.error)
            feedbackResult is CareerPilotResult.Error -> CareerPilotResult.Error(feedbackResult.error)
            sessionResult is CareerPilotResult.Success && feedbackResult is CareerPilotResult.Success -> {
                mapCatching { feedbackResult.data.toDomain(sessionResult.data) }
            }
            else -> CareerPilotResult.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun getQuestionBreakdown(
        sessionId: Long,
    ): CareerPilotResult<QuestionBreakdown, NetworkError> =
        mapRemoteResult({ remoteDataSource.getQuestions(sessionId) }) { questions ->
            questions.toDomain(sessionId)
        }

    private suspend fun <Remote, Domain> mapRemoteResult(
        call: suspend () -> CareerPilotResult<Remote, NetworkError>,
        transform: (Remote) -> Domain,
    ): CareerPilotResult<Domain, NetworkError> =
        when (val result = call()) {
            is CareerPilotResult.Error -> CareerPilotResult.Error(result.error)
            is CareerPilotResult.Success -> mapCatching { transform(result.data) }
        }

    private inline fun <T> mapCatching(
        transform: () -> T,
    ): CareerPilotResult<T, NetworkError> = try {
        CareerPilotResult.Success(transform())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: SerializationException) {
        CareerPilotResult.Error(NetworkError.SERIALIZATION)
    } catch (_: Exception) {
        CareerPilotResult.Error(NetworkError.UNKNOWN)
    }
}
