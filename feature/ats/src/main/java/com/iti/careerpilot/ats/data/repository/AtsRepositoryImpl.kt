package com.iti.careerpilot.ats.data.repository

import com.iti.careerpilot.ats.data.mapper.toDomain
import com.iti.careerpilot.ats.data.remote.AtsRemoteDataSource
import com.iti.careerpilot.ats.domain.model.AtsScore
import com.iti.careerpilot.ats.domain.model.AiJob
import com.iti.careerpilot.ats.domain.model.CoverLetter
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.careerpilot.ats.domain.repository.AtsRepository
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.UserProfile
import com.iti.core.datastore.repo.UserProfileRepo
import com.iti.core.datastore.sync.UserProfileSync
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.cancellation.CancellationException

class AtsRepositoryImpl @Inject constructor(
    private val remoteDataSource: AtsRemoteDataSource,
    private val profileRepo: UserProfileRepo,
    private val profileSync: UserProfileSync,
) : AtsRepository {
    override val userProfile: StateFlow<UserProfile> = profileRepo.userProfile

    override suspend fun importJob(url: String) = mapResult(remoteDataSource.importJob(url)) { it.toDomain() }

    override suspend fun getWorkspace(workspaceId: Long) =
        mapResult(remoteDataSource.getWorkspace(workspaceId)) { it.toDomain() }

    override suspend fun scoreCv(workspaceId: Long): CareerPilotResult<AtsScore, NetworkError> =
        mapPaidResult(remoteDataSource.scoreCv(workspaceId)) { it.toDomain() }

    override suspend fun optimizeCv(workspaceId: Long): CareerPilotResult<AiJob, NetworkError> =
        mapPaidResult(remoteDataSource.optimizeCv(workspaceId)) { it.toDomain() }

    override suspend fun getAiJob(jobId: Long): CareerPilotResult<AiJob, NetworkError> =
        mapResult(remoteDataSource.getAiJob(jobId)) { it.toDomain() }

    override suspend fun generateCoverLetter(workspaceId: Long): CareerPilotResult<CoverLetter, NetworkError> =
        mapPaidResult(remoteDataSource.generateCoverLetter(workspaceId)) { it.toDomain() }

    private suspend fun <Remote, Domain> mapPaidResult(
        result: CareerPilotResult<Remote, NetworkError>,
        transform: (Remote) -> Domain,
    ): CareerPilotResult<Domain, NetworkError> {
        val mapped = mapResult(result, transform)
        if (mapped is CareerPilotResult.Success) synchronizeAccountState()
        return mapped
    }

    private suspend fun synchronizeAccountState() {
        try {
            profileSync.syncWalletBalance()
            profileSync.syncSubscriptionTier()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            // The paid operation succeeded; profile synchronization can recover later.
        }
    }

    private fun <Remote, Domain> mapResult(
        result: CareerPilotResult<Remote, NetworkError>,
        transform: (Remote) -> Domain,
    ): CareerPilotResult<Domain, NetworkError> = when (result) {
        is CareerPilotResult.Error -> result
        is CareerPilotResult.Success -> CareerPilotResult.Success(transform(result.data))
    }
}
