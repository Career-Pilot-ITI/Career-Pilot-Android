package com.iti.careerpilot.ats.domain.repository

import com.iti.careerpilot.ats.domain.model.AtsScore
import com.iti.careerpilot.ats.domain.model.CoverLetter
import com.iti.careerpilot.ats.domain.model.CvOptimization
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.UserProfile
import com.iti.core.model.PdfFile
import kotlinx.coroutines.flow.StateFlow

interface AtsRepository {
    val userProfile: StateFlow<UserProfile>

    suspend fun replaceCurrentCv(
        file: PdfFile,
        onProgress: (Int) -> Unit,
    ): CareerPilotResult<Unit, NetworkError>

    suspend fun importJob(url: String): CareerPilotResult<JobWorkspace, NetworkError>

    suspend fun getWorkspace(workspaceId: Long): CareerPilotResult<JobWorkspace, NetworkError>

    suspend fun scoreCv(workspaceId: Long): CareerPilotResult<AtsScore, NetworkError>

    suspend fun optimizeCv(workspaceId: Long): CareerPilotResult<CvOptimization, NetworkError>

    suspend fun generateCoverLetter(workspaceId: Long): CareerPilotResult<CoverLetter, NetworkError>
}
