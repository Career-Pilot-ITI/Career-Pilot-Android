package com.iti.careerpilot.reports.data.mapper

import com.iti.careerpilot.core.interviews.domain.model.InterviewSession
import com.iti.careerpilot.reports.data.datasource.remote.dto.FeedbackReportDto
import com.iti.careerpilot.reports.data.datasource.remote.dto.SessionQuestionDto
import com.iti.careerpilot.reports.domain.model.CoachingImpact
import com.iti.careerpilot.reports.domain.model.CoachingSuggestion
import com.iti.careerpilot.reports.domain.model.PerformanceMetrics
import com.iti.careerpilot.reports.domain.model.PerformanceTier
import com.iti.careerpilot.reports.domain.model.QuestionBreakdown
import com.iti.careerpilot.reports.domain.model.QuestionReport
import com.iti.careerpilot.reports.domain.model.ReportDetails
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.serialization.SerializationException

internal fun FeedbackReportDto.toDomain(
    session: InterviewSession,
): ReportDetails {
    validateScore(overallScore)
    val metricValues = listOf(
        clarityScore,
        confidenceScore,
        pacingScore,
        fillerWordsScore,
        contentRelevanceScore,
    )
    metricValues.forEach(::validateScore)
    val nonBlankTips = coachingTips.filter(String::isNotBlank)
    return ReportDetails(
        sessionId = sessionId,
        completedAt = session.occurredAt
            ?: parseBackendTimestamp(generatedAt ?: createdAt),
        overallScore = overallScore,
        performanceTier = overallScore.toPerformanceTier(),
        topPercent = null,
        metrics = PerformanceMetrics(
            clarity = clarityScore,
            confidence = confidenceScore,
            pacing = pacingScore,
            fillerWords = fillerWordsScore,
            content = contentRelevanceScore,
        ),
        coachingSuggestions = nonBlankTips.mapIndexed { index, tip ->
            CoachingSuggestion(
                id = "$sessionId-suggestion-${index + 1}",
                ordinal = index + 1,
                description = tip,
                impact = impactFor(index = index, count = nonBlankTips.size),
            )
        },
    )
}

internal fun List<SessionQuestionDto>.toDomain(
    sessionId: Long,
): QuestionBreakdown = QuestionBreakdown(
    sessionId = sessionId,
    questions = sortedBy(SessionQuestionDto::questionOrder).map { question ->
        val transcript = question.userTranscript.orEmpty()
        val fillerWords = findFillerWords(transcript)
        question.score?.overallScore?.let(::validateScore)
        QuestionReport(
            id = question.id.toString(),
            index = question.questionOrder,
            question = question.questionText,
            score = question.score?.overallScore ?: 0,
            fillerWordCount = countFillerWords(transcript),
            durationSeconds = ((question.durationMs ?: 0L) / MILLIS_PER_SECOND)
                .coerceAtMost(Int.MAX_VALUE.toLong())
                .toInt(),
            coachFeedback = question.score?.coachingTip.orEmpty(),
            transcript = transcript,
            fillerWords = fillerWords,
        )
    },
)

internal fun findFillerWords(transcript: String): List<String> = FILLER_WORDS.filter { word ->
    fillerWordRegex(word).containsMatchIn(transcript)
}

internal fun countFillerWords(transcript: String): Int = FILLER_WORDS.sumOf { word ->
    fillerWordRegex(word).findAll(transcript).count()
}

private fun fillerWordRegex(word: String): Regex = Regex(
    pattern = "(?i)(?<!\\p{L})${Regex.escape(word)}(?!\\p{L})",
)

private fun impactFor(index: Int, count: Int): CoachingImpact {
    if (count <= 0) return CoachingImpact.LOW
    return when (index * IMPACT_GROUP_COUNT / count) {
        0 -> CoachingImpact.HIGH
        1 -> CoachingImpact.MEDIUM
        else -> CoachingImpact.LOW
    }
}

private fun Int.toPerformanceTier(): PerformanceTier = when {
    this >= STRONG_SCORE -> PerformanceTier.STRONG
    this >= GOOD_SCORE -> PerformanceTier.GOOD
    else -> PerformanceTier.NEEDS_IMPROVEMENT
}

private fun parseBackendTimestamp(value: String): Instant =
    parseBackendTimestampOrNull(value)
        ?: throw SerializationException("Invalid report timestamp")

private fun parseBackendTimestampOrNull(value: String?): Instant? {
    if (value.isNullOrBlank()) return null
    return runCatching { Instant.parse(value) }
        .recoverCatching { LocalDateTime.parse(value).toInstant(ZoneOffset.UTC) }
        .getOrNull()
}

private fun validateScore(value: Int) {
    if (value !in MIN_SCORE..MAX_SCORE) {
        throw SerializationException("A report score is out of range")
    }
}

private const val MILLIS_PER_SECOND = 1_000L
private const val IMPACT_GROUP_COUNT = 3
private const val STRONG_SCORE = 80
private const val GOOD_SCORE = 70
private const val MIN_SCORE = 0
private const val MAX_SCORE = 100
private val FILLER_WORDS = listOf("uh", "um", "you know", "basically")
