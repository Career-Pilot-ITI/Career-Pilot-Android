package com.iti.careerpilot.reports.data.repository

import com.iti.careerpilot.reports.data.datasource.remote.ReportsRemoteDataSource
import com.iti.careerpilot.reports.data.mapper.toDomain
import com.iti.careerpilot.reports.data.mapper.toHistoryDomainOrNull
import com.iti.careerpilot.reports.domain.model.InterviewSessionSummary
import com.iti.careerpilot.reports.domain.model.QuestionBreakdown
import com.iti.careerpilot.reports.domain.model.ReportDetails
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
    override suspend fun getSessionHistory(): CareerPilotResult<List<InterviewSessionSummary>, NetworkError> =
        mapRemoteResult(remoteDataSource::getSessions) { sessions ->
            sessions.mapNotNull { it.toHistoryDomainOrNull() }
        }

    override suspend fun getReportDetails(
        sessionId: String,
    ): CareerPilotResult<ReportDetails, NetworkError> {
        val id = sessionId.toLongOrNull()
            ?: return CareerPilotResult.Error(NetworkError.BAD_REQUEST)
        return withContext(ioDispatcher) {
            when (val sessionResult = remoteDataSource.getSession(id)) {
                is CareerPilotResult.Error -> CareerPilotResult.Error(sessionResult.error)
                is CareerPilotResult.Success -> {
                    if (!sessionResult.data.status.equals(COMPLETED_STATUS, ignoreCase = true)) {
                        CareerPilotResult.Error(NetworkError.CONFLICT)
                    } else {
                        mapRemoteResult({ remoteDataSource.getFeedback(id) }) { feedback ->
                            feedback.toDomain(sessionResult.data)
                        }
                    }
                }
            }
        }
    }

    override suspend fun getQuestionBreakdown(
        sessionId: String,
    ): CareerPilotResult<QuestionBreakdown, NetworkError> {
        val id = sessionId.toLongOrNull()
            ?: return CareerPilotResult.Error(NetworkError.BAD_REQUEST)
        return mapRemoteResult({ remoteDataSource.getQuestions(id) }) { questions ->
            questions.toDomain(id)
        }
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

    private companion object {
        const val COMPLETED_STATUS = "COMPLETED"
    }
}
