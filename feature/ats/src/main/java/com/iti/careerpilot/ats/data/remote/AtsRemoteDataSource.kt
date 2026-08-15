package com.iti.careerpilot.ats.data.remote

import com.iti.careerpilot.ats.data.dto.AtsScoreDto
import com.iti.careerpilot.ats.data.dto.AiJobDto
import com.iti.careerpilot.ats.data.dto.CoverLetterDto
import com.iti.careerpilot.ats.data.dto.JobWorkspaceDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.PdfFile

interface AtsRemoteDataSource {
    suspend fun replaceCurrentCv(
        file: PdfFile,
        onProgress: (Int) -> Unit,
    ): CareerPilotResult<String, NetworkError>

    suspend fun importJob(url: String): CareerPilotResult<JobWorkspaceDto, NetworkError>
    suspend fun getWorkspace(workspaceId: Long): CareerPilotResult<JobWorkspaceDto, NetworkError>
    suspend fun scoreCv(workspaceId: Long): CareerPilotResult<AtsScoreDto, NetworkError>
    suspend fun optimizeCv(workspaceId: Long): CareerPilotResult<AiJobDto, NetworkError>
    suspend fun getAiJob(jobId: Long): CareerPilotResult<AiJobDto, NetworkError>
    suspend fun generateCoverLetter(workspaceId: Long): CareerPilotResult<CoverLetterDto, NetworkError>
}
