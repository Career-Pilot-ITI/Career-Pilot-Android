package com.iti.careerpilot.reports.data.mapper

import com.iti.careerpilot.reports.data.datasource.remote.dto.CoachingSuggestionDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.PerformanceMetricsDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.QuestionBreakdownDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.QuestionReportDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.ReportDetailsDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.SessionSummaryDto
import com.iti.careerpilot.reports.domain.model.CoachingImpact
import com.iti.careerpilot.reports.domain.model.CoachingSuggestion
import com.iti.careerpilot.reports.domain.model.CoachingSuggestionType
import com.iti.careerpilot.reports.domain.model.InterviewSessionSummary
import com.iti.careerpilot.reports.domain.model.PerformanceMetrics
import com.iti.careerpilot.reports.domain.model.PerformanceTier
import com.iti.careerpilot.reports.domain.model.QuestionBreakdown
import com.iti.careerpilot.reports.domain.model.QuestionReport
import com.iti.careerpilot.reports.domain.model.ReportDetails
import java.time.Instant
import kotlinx.serialization.SerializationException

fun SessionSummaryDto.toDomain(): InterviewSessionSummary {
    validateIdentifier(id)
    validateScore(score)
    validateNonNegative(durationMinutes, "durationMinutes")
    validateNonNegative(questionCount, "questionCount")
    return InterviewSessionSummary(
        id = id,
        score = score,
        category = category,
        completedAt = parseInstant(completedAt),
        durationMinutes = durationMinutes,
        questionCount = questionCount,
    )
}

fun ReportDetailsDto.toDomain(): ReportDetails {
    validateIdentifier(sessionId)
    validateScore(overallScore)
    validateScore(topPercent)
    return ReportDetails(
        sessionId = sessionId,
        completedAt = parseInstant(completedAt),
        overallScore = overallScore,
        performanceTier = performanceTier.toPerformanceTier(),
        topPercent = topPercent,
        metrics = metrics.toDomain(),
        coachingSuggestions = coachingSuggestions.map(CoachingSuggestionDto::toDomain),
    )
}

fun QuestionBreakdownDto.toDomain(): QuestionBreakdown {
    validateIdentifier(sessionId)
    return QuestionBreakdown(
        sessionId = sessionId,
        questions = questions.map(QuestionReportDto::toDomain),
    )
}

private fun PerformanceMetricsDto.toDomain(): PerformanceMetrics {
    listOf(clarity, confidence, pacing, fillerWords, content).forEach(::validateScore)
    return PerformanceMetrics(clarity, confidence, pacing, fillerWords, content)
}

private fun CoachingSuggestionDto.toDomain(): CoachingSuggestion {
    validateIdentifier(id)
    return CoachingSuggestion(
        id = id,
        type = when (type.lowercase()) {
            "filler_words" -> CoachingSuggestionType.FILLER_WORDS
            "technical_depth" -> CoachingSuggestionType.TECHNICAL_DEPTH
            "story_structure" -> CoachingSuggestionType.STORY_STRUCTURE
            else -> CoachingSuggestionType.OTHER
        },
        title = title,
        description = description,
        impact = when (impact.lowercase()) {
            "high" -> CoachingImpact.HIGH
            "medium" -> CoachingImpact.MEDIUM
            "low" -> CoachingImpact.LOW
            else -> throw SerializationException("Unknown coaching impact")
        },
    )
}

private fun QuestionReportDto.toDomain(): QuestionReport {
    validateIdentifier(id)
    validateNonNegative(index, "index")
    validateScore(score)
    validateNonNegative(fillerWordCount, "fillerWordCount")
    validateNonNegative(durationSeconds, "durationSeconds")
    return QuestionReport(
        id = id,
        index = index,
        question = question,
        score = score,
        fillerWordCount = fillerWordCount,
        durationSeconds = durationSeconds,
        coachFeedback = coachFeedback,
        transcript = transcript,
        fillerWords = fillerWords,
    )
}

private fun String.toPerformanceTier(): PerformanceTier = when (lowercase()) {
    "strong" -> PerformanceTier.STRONG
    "good" -> PerformanceTier.GOOD
    "needs_improvement" -> PerformanceTier.NEEDS_IMPROVEMENT
    else -> throw SerializationException("Unknown performance tier")
}

private fun parseInstant(value: String): Instant = try {
    Instant.parse(value)
} catch (exception: Exception) {
    throw SerializationException("Invalid report timestamp", exception)
}

private fun validateIdentifier(value: String) {
    if (value.isBlank()) throw SerializationException("A report identifier is blank")
}

private fun validateScore(value: Int) {
    if (value !in 0..100) throw SerializationException("A report score is out of range")
}

private fun validateNonNegative(
    value: Int,
    field: String,
) {
    if (value < 0) throw SerializationException("$field cannot be negative")
}
