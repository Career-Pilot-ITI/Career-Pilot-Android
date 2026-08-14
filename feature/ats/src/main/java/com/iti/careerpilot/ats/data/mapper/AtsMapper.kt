package com.iti.careerpilot.ats.data.mapper

import com.iti.careerpilot.ats.data.dto.AtsScoreDto
import com.iti.careerpilot.ats.data.dto.AiJobDto
import com.iti.careerpilot.ats.data.dto.CoverLetterDto
import com.iti.careerpilot.ats.data.dto.CvOptimizationDto
import com.iti.careerpilot.ats.data.dto.JobDto
import com.iti.careerpilot.ats.data.dto.JobWorkspaceDto
import com.iti.careerpilot.ats.domain.model.AtsScore
import com.iti.careerpilot.ats.domain.model.AtsSectionScore
import com.iti.careerpilot.ats.domain.model.AiJob
import com.iti.careerpilot.ats.domain.model.AiJobStatus
import com.iti.careerpilot.ats.domain.model.AiJobType
import com.iti.careerpilot.ats.domain.model.CoverLetter
import com.iti.careerpilot.ats.domain.model.CvOptimization
import com.iti.careerpilot.ats.domain.model.CvOptimizationSection
import com.iti.careerpilot.ats.domain.model.CvSectionImprovement
import com.iti.careerpilot.ats.domain.model.JobListing
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

internal fun JobWorkspaceDto.toDomain() = JobWorkspace(
    id = id,
    job = job.toDomain(),
    status = status.orEmpty(),
    cvScore = cvScore?.coerceIn(SCORE_RANGE),
    cvScoreUpdatedAt = cvScoreUpdatedAt,
    cvOptimizedText = cvOptimizedText,
    coverLetterText = coverLetterText,
    lastInterviewSessionId = lastInterviewSessionId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun JobDto.toDomain() = JobListing(
    id = id,
    title = title.orEmpty(),
    companyName = companyName.orEmpty(),
    location = location.orEmpty(),
    description = description.orEmpty(),
    employmentType = employmentType.nonBlankOrNull(),
    seniorityLevel = seniorityLevel.nonBlankOrNull(),
    requiredSkills = requiredSkills.nonBlankValues(),
    preferredSkills = preferredSkills.nonBlankValues(),
    responsibilities = responsibilities.nonBlankValues(),
    qualifications = qualifications.nonBlankValues(),
    technologies = technologies.nonBlankValues(),
    salaryMin = salaryMin,
    salaryMax = salaryMax,
    currency = currency.nonBlankOrNull(),
    experienceYears = experienceYears?.coerceAtLeast(0),
    educationLevel = educationLevel.nonBlankOrNull(),
    applicationUrl = applicationUrl.nonBlankOrNull(),
    sourceUrl = sourceUrl.nonBlankOrNull(),
    sourceType = sourceType.nonBlankOrNull(),
    companyLogoUrl = companyLogoUrl.nonBlankOrNull(),
    postedLabel = postedLabel.nonBlankOrNull(),
    applicantsLabel = applicantsLabel.nonBlankOrNull(),
)

internal fun AtsScoreDto.toDomain() = AtsScore(
    overallScore = overallScore.coerceIn(SCORE_RANGE),
    matchPercentage = matchPercentage.coerceIn(SCORE_RANGE),
    matchedSkills = matchedSkills.nonBlankValues(),
    missingRequiredSkills = missingRequiredSkills.nonBlankValues(),
    missingPreferredSkills = missingPreferredSkills.nonBlankValues(),
    strengths = strengths.nonBlankValues(),
    weaknesses = weaknesses.nonBlankValues(),
    sections = sections
        .map { section ->
            AtsSectionScore(
                section = section.section.orEmpty(),
                score = section.score.coerceIn(SCORE_RANGE),
                feedback = section.feedback.orEmpty(),
            )
        }
        .filter { it.section.isNotBlank() }
        .toImmutableList(),
    recommendations = recommendations.nonBlankValues(),
    coinCost = coinCost?.coerceAtLeast(0),
    cvScoreUpdatedAt = cvScoreUpdatedAt,
)

internal fun CvOptimizationDto.toDomain() = CvOptimization(
    sections = sections
        .map { section ->
            CvOptimizationSection(
                name = section.name.orEmpty(),
                score = section.score.coerceIn(SCORE_RANGE),
                improvements = section.improvements
                    .map { improvement ->
                        CvSectionImprovement(
                            original = improvement.original.orEmpty(),
                            improved = improvement.improved.orEmpty(),
                            reason = improvement.reason.orEmpty(),
                        )
                    }
                    .toImmutableList(),
            )
        }
        .filter { it.name.isNotBlank() }
        .toImmutableList(),
    recommendedTracks = recommendedTracks.nonBlankValues(),
    coinCost = coinCost?.coerceAtLeast(0),
)

internal fun AiJobDto.toDomain(): AiJob {
    val mappedStatus = status.toAiJobStatus()
    return AiJob(
        id = id,
        workspaceId = workspaceId,
        type = type.toAiJobType(),
        status = mappedStatus,
        progressPercentage = progressPercentage.coerceIn(SCORE_RANGE),
        currentStep = currentStep.orEmpty(),
        result = result?.takeIf { mappedStatus == AiJobStatus.COMPLETED }?.toDomain(),
        errorMessage = errorMessage.nonBlankOrNull(),
        createdAt = createdAt,
        startedAt = startedAt,
        completedAt = completedAt,
    )
}

internal fun CoverLetterDto.toDomain() = CoverLetter(
    body = coverLetter.orEmpty(),
    approachTips = approachTips.nonBlankOrNull(),
    coinCost = coinCost?.coerceAtLeast(0),
)

private fun String?.nonBlankOrNull() = this?.trim()?.takeIf(String::isNotEmpty)

private fun List<String>.nonBlankValues(): ImmutableList<String> =
    map(String::trim).filter(String::isNotEmpty).toImmutableList()

private fun String?.toAiJobType() = when (this?.uppercase()) {
    "CV_OPTIMIZE" -> AiJobType.CV_OPTIMIZE
    else -> AiJobType.UNKNOWN
}

private fun String?.toAiJobStatus() = when (this?.uppercase()) {
    "PENDING" -> AiJobStatus.PENDING
    "PROCESSING" -> AiJobStatus.PROCESSING
    "COMPLETED" -> AiJobStatus.COMPLETED
    "FAILED" -> AiJobStatus.FAILED
    else -> AiJobStatus.UNKNOWN
}

private val SCORE_RANGE = 0..100
