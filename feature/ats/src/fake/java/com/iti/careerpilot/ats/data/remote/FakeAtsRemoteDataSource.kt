package com.iti.careerpilot.ats.data.remote

import com.iti.careerpilot.ats.data.dto.AtsScoreDto
import com.iti.careerpilot.ats.data.dto.AtsSectionScoreDto
import com.iti.careerpilot.ats.data.dto.CoverLetterDto
import com.iti.careerpilot.ats.data.dto.CvOptimizationDto
import com.iti.careerpilot.ats.data.dto.JobDto
import com.iti.careerpilot.ats.data.dto.JobWorkspaceDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.fakeDelay
import com.iti.core.model.PdfFile
import javax.inject.Inject

class FakeAtsRemoteDataSource @Inject constructor() : AtsRemoteDataSource {
    override suspend fun replaceCurrentCv(
        file: PdfFile,
        onProgress: (Int) -> Unit,
    ): CareerPilotResult<String, NetworkError> {
        if (file.bytes.isEmpty()) return CareerPilotResult.Error(NetworkError.BAD_REQUEST)
        onProgress(35)
        fakeDelay()
        onProgress(100)
        return CareerPilotResult.Success("https://cdn.careerpilot.test/cv/${file.name}")
    }

    override suspend fun importJob(url: String): CareerPilotResult<JobWorkspaceDto, NetworkError> {
        fakeDelay()
        return if (url.startsWith("https://")) CareerPilotResult.Success(WORKSPACE)
        else CareerPilotResult.Error(NetworkError.BAD_REQUEST)
    }

    override suspend fun getWorkspace(workspaceId: Long) = workspaceResult(workspaceId, WORKSPACE)

    override suspend fun scoreCv(workspaceId: Long) = workspaceResult(workspaceId, SCORE)

    override suspend fun optimizeCv(workspaceId: Long) = workspaceResult(workspaceId, OPTIMIZATION)

    override suspend fun generateCoverLetter(workspaceId: Long) = workspaceResult(workspaceId, COVER_LETTER)

    private suspend fun <T> workspaceResult(
        workspaceId: Long,
        value: T,
    ): CareerPilotResult<T, NetworkError> {
        fakeDelay()
        return if (workspaceId == WORKSPACE.id) CareerPilotResult.Success(value)
        else CareerPilotResult.Error(NetworkError.NOT_FOUND)
    }

    private companion object {
        val WORKSPACE = JobWorkspaceDto(
            id = 1L,
            job = JobDto(
                id = 11L,
                title = "Senior Frontend Engineer",
                companyName = "Google",
                location = "Cairo, EG",
                description = "Lead accessible product development with designers and backend engineers.",
                employmentType = "Full-time",
                seniorityLevel = "Senior",
                requiredSkills = listOf("TypeScript", "Node.js", "System Design", "AWS", "REST APIs"),
                preferredSkills = listOf("GraphQL"),
                technologies = listOf("Kubernetes", "Go", "GraphQL"),
                responsibilities = listOf("Lead delivery of user-facing features"),
                qualifications = listOf("5+ years of relevant experience"),
                experienceYears = 5,
                sourceUrl = "https://example.com/jobs/11",
                sourceType = "DIRECT",
            ),
            status = "IMPORTED",
        )
        val SCORE = AtsScoreDto(
            overallScore = 78,
            matchPercentage = 78,
            matchedSkills = listOf("TypeScript", "Node.js", "System Design", "AWS", "REST APIs"),
            missingRequiredSkills = listOf("Kubernetes"),
            missingPreferredSkills = listOf("GraphQL"),
            strengths = listOf("Strong frontend architecture experience", "Leadership at scale"),
            weaknesses = listOf("Container orchestration exposure is not shown"),
            sections = listOf(
                AtsSectionScoreDto("Projects", 59, "Add measurable outcomes."),
                AtsSectionScoreDto("Skills", 76, "Skills are relevant and clear."),
                AtsSectionScoreDto("Work Experience", 88, "Experience aligns well."),
            ),
            recommendations = listOf("Add Kubernetes with a brief hands-on description"),
            coinCost = 2,
            cvScoreUpdatedAt = "2026-08-11T09:41:00Z",
        )
        val OPTIMIZATION = CvOptimizationDto(
            optimizedCv = "Senior engineer with measurable experience delivering accessible products at scale.",
            recommendedTracks = listOf("Frontend Engineering", "System Design"),
            coinCost = 3,
        )
        val COVER_LETTER = CoverLetterDto(
            coverLetter = "Dear Hiring Manager,\n\nI am excited to apply for the Senior Frontend Engineer role.",
            approachTips = "Add one recent product impact metric before sending.",
            coinCost = 2,
        )
    }
}
