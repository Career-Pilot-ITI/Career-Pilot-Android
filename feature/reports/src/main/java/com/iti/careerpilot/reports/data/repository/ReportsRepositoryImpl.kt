package com.iti.careerpilot.reports.data.repository

import com.iti.careerpilot.reports.data.datasource.remote.ReportsRemoteDataSource
import com.iti.careerpilot.reports.data.mapper.toDomain
import com.iti.careerpilot.reports.data.mapper.toHistoryDomain
import com.iti.careerpilot.reports.domain.model.QuestionBreakdown
import com.iti.careerpilot.reports.domain.model.ReportDetails
import com.iti.careerpilot.reports.domain.model.SessionHistoryPage
import com.iti.careerpilot.reports.domain.repository.ReportsRepository
import com.iti.common.dispatcher.CareerPilotDispatchers
import com.iti.common.dispatcher.Dispatcher
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException

class ReportsRepositoryImpl @Inject constructor(
    private val remoteDataSource: ReportsRemoteDataSource,
    @param:Dispatcher(CareerPilotDispatchers.IO)
    private val ioDispatcher: CoroutineDispatcher,
) : ReportsRepository {
    override suspend fun getSessionHistoryPage(
        page: Int,
        size: Int,
    ): CareerPilotResult<SessionHistoryPage, NetworkError> =
        mapRemoteResult(
            call = { remoteDataSource.getSessions(page = page, size = size) },
            transform = { response -> response.toHistoryDomain() },
        )

    override suspend fun getReportDetails(
        sessionId: Long,
    ): CareerPilotResult<ReportDetails, NetworkError> = withContext(ioDispatcher) {
        when (val sessionResult = remoteDataSource.getSession(sessionId)) {
            is CareerPilotResult.Error -> CareerPilotResult.Error(sessionResult.error)
            is CareerPilotResult.Success -> {
                mapRemoteResult({ remoteDataSource.getFeedback(sessionId) }) { feedback ->
                    feedback.toDomain(sessionResult.data)
                }
            }
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
    ): CareerPilotResult<Domain, NetworkError> = withContext(ioDispatcher) {
        when (val result = call()) {
            is CareerPilotResult.Error -> CareerPilotResult.Error(result.error)
            is CareerPilotResult.Success -> try {
                CareerPilotResult.Success(transform(result.data))
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: SerializationException) {
                CareerPilotResult.Error(NetworkError.SERIALIZATION)
            } catch (_: Exception) {
                CareerPilotResult.Error(NetworkError.UNKNOWN)
            }
        }
    }
}
