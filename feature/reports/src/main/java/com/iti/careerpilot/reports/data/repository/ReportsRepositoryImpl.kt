package com.iti.careerpilot.reports.data.repository

import com.iti.careerpilot.core.network.util.toNetworkError
import com.iti.careerpilot.reports.data.datasource.remote.ReportsDataNotFoundException
import com.iti.careerpilot.reports.data.datasource.remote.DummyReportsRemoteDataSource
import com.iti.careerpilot.reports.data.mapper.toDomain
import com.iti.careerpilot.reports.domain.model.InterviewSessionSummary
import com.iti.careerpilot.reports.domain.model.QuestionBreakdown
import com.iti.careerpilot.reports.domain.model.ReportDetails
import com.iti.careerpilot.reports.domain.repository.ReportsRepository
import com.iti.common.dispatcher.CareerPilotDispatchers
import com.iti.common.dispatcher.Dispatcher
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.ContentConvertException
import java.io.IOException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException

class ReportsRepositoryImpl @Inject constructor(
    private val remoteDataSource: DummyReportsRemoteDataSource,
    @param:Dispatcher(CareerPilotDispatchers.IO)
    private val ioDispatcher: CoroutineDispatcher,
) : ReportsRepository {
    override suspend fun getSessionHistory(): CareerPilotResult<List<InterviewSessionSummary>, NetworkError> =
        safeReportsCall {
            remoteDataSource.getSessionHistory().map { it.toDomain() }
        }

    override suspend fun getReportDetails(
        sessionId: String,
    ): CareerPilotResult<ReportDetails, NetworkError> = safeReportsCall {
        remoteDataSource.getReportDetails(sessionId).toDomain()
    }

    override suspend fun getQuestionBreakdown(
        sessionId: String,
    ): CareerPilotResult<QuestionBreakdown, NetworkError> = safeReportsCall {
        remoteDataSource.getQuestionBreakdown(sessionId).toDomain()
    }

    private suspend fun <T> safeReportsCall(
        block: suspend () -> T,
    ): CareerPilotResult<T, NetworkError> = withContext(ioDispatcher) {
        try {
            CareerPilotResult.Success(block())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: ReportsDataNotFoundException) {
            CareerPilotResult.Error(NetworkError.NOT_FOUND)
        } catch (exception: ResponseException) {
            CareerPilotResult.Error(exception.toNetworkError())
        } catch (_: HttpRequestTimeoutException) {
            CareerPilotResult.Error(NetworkError.TIME_OUT)
        } catch (_: ConnectTimeoutException) {
            CareerPilotResult.Error(NetworkError.TIME_OUT)
        } catch (_: SocketTimeoutException) {
            CareerPilotResult.Error(NetworkError.TIME_OUT)
        } catch (_: UnresolvedAddressException) {
            CareerPilotResult.Error(NetworkError.NO_INTERNET)
        } catch (_: UnknownHostException) {
            CareerPilotResult.Error(NetworkError.NO_INTERNET)
        } catch (_: IOException) {
            CareerPilotResult.Error(NetworkError.NO_INTERNET)
        } catch (_: SerializationException) {
            CareerPilotResult.Error(NetworkError.SERIALIZATION)
        } catch (_: ContentConvertException) {
            CareerPilotResult.Error(NetworkError.SERIALIZATION)
        } catch (_: Exception) {
            CareerPilotResult.Error(NetworkError.UNKNOWN)
        }
    }
}
