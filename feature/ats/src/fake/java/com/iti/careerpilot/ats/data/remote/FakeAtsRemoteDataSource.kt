package com.iti.careerpilot.ats.data.remote

import com.iti.careerpilot.ats.data.dto.AtsScoreDto
import com.iti.careerpilot.ats.data.dto.AtsSectionScoreDto
import com.iti.careerpilot.ats.data.dto.AiJobDto
import com.iti.careerpilot.ats.data.dto.CoverLetterDto
import com.iti.careerpilot.ats.data.dto.CvOptimizationDto
import com.iti.careerpilot.ats.data.dto.CvOptimizationSectionDto
import com.iti.careerpilot.ats.data.dto.CvSectionImprovementDto
import com.iti.careerpilot.ats.data.dto.JobDto
import com.iti.careerpilot.ats.data.dto.JobWorkspaceDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.util.fakeDelay
import javax.inject.Inject

class FakeAtsRemoteDataSource @Inject constructor() : AtsRemoteDataSource {
    override suspend fun importJob(url: String): CareerPilotResult<JobWorkspaceDto, NetworkError> {
        fakeAtsDelay()
        return if (url.startsWith("https://")) CareerPilotResult.Success(WORKSPACE)
        else CareerPilotResult.Error(NetworkError.BAD_REQUEST)
    }

    override suspend fun getWorkspace(workspaceId: Long) = workspaceResult(workspaceId, WORKSPACE)

    override suspend fun scoreCv(workspaceId: Long) = workspaceResult(workspaceId, SCORE)

    override suspend fun optimizeCv(workspaceId: Long) = workspaceResult(workspaceId, PENDING_OPTIMIZATION)

    override suspend fun getAiJob(jobId: Long): CareerPilotResult<AiJobDto, NetworkError> {
        fakeAtsDelay()
        return if (jobId == COMPLETED_OPTIMIZATION.id) {
            CareerPilotResult.Success(COMPLETED_OPTIMIZATION)
        } else {
            CareerPilotResult.Error(NetworkError.NOT_FOUND)
        }
    }

    override suspend fun generateCoverLetter(workspaceId: Long) = workspaceResult(workspaceId, COVER_LETTER)

    private suspend fun <T> workspaceResult(
        workspaceId: Long,
        value: T,
    ): CareerPilotResult<T, NetworkError> {
        fakeAtsDelay()
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
                sourceUrl = "https://www.linkedin.com/jobs/view/123456789",
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
        val PENDING_OPTIMIZATION = AiJobDto(
            id = 42L,
            workspaceId = WORKSPACE.id,
            type = "CV_OPTIMIZE",
            status = "PENDING",
            progressPercentage = 0,
            currentStep = "Queued",
        )
        val COMPLETED_OPTIMIZATION = PENDING_OPTIMIZATION.copy(
            status = "COMPLETED",
            progressPercentage = 100,
            currentStep = "CV Optimization completed successfully!",
            result = CvOptimizationDto(
                sections = listOf(
                    CvOptimizationSectionDto(
                        name = "Experience",
                        score = 82,
                        improvements = listOf(
                            CvSectionImprovementDto(
                                original = "Worked on backend APIs.",
                                improved = "Delivered measurable backend API improvements.",
                                reason = "Adds measurable impact.",
                            ),
                        ),
                    ),
                    CvOptimizationSectionDto(
                        name = "Skills",
                        score = 90,
                    ),
                ),
                recommendedTracks = listOf("Frontend Engineering", "System Design"),
                coinCost = 3,
            ),
        )
        val COVER_LETTER = CoverLetterDto(
            coverLetter = "Dear Hiring Manager,\n\nI am excited to apply for the Senior Frontend Engineer role.",
            approachTips = """
                1. Add one recent product impact metric before sending.
                2. Mention the architecture decisions behind your strongest project.
                3. Contact the hiring manager with a short, role-specific message.
            """.trimIndent(),
            coinCost = 2,
        )

        const val FAKE_DELAY_MIN_MS = 50L
        const val FAKE_DELAY_MAX_MS = 150L
    }

    private suspend fun fakeAtsDelay() = fakeDelay(
        min = FAKE_DELAY_MIN_MS,
        max = FAKE_DELAY_MAX_MS,
    )
}
