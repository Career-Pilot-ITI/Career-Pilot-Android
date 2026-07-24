package com.iti.careerpilot.reports.presentation.screen.breakdown.uimodels

import androidx.compose.runtime.Immutable
import com.iti.careerpilot.reports.domain.model.QuestionBreakdown
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

@Immutable
data class QuestionBreakdownUiModel(
    val sessionId: String,
    val questions: PersistentList<QuestionUiModel>,
)

@Immutable
data class QuestionUiModel(
    val id: String,
    val index: Int,
    val question: String,
    val score: Int,
    val fillerWordCount: Int,
    val durationSeconds: Int,
    val coachFeedback: String,
    val transcript: String,
    val fillerWords: PersistentList<String>,
)

fun QuestionBreakdown.toUiModel(): QuestionBreakdownUiModel = QuestionBreakdownUiModel(
    sessionId = sessionId,
    questions = questions.map { question ->
        QuestionUiModel(
            id = question.id,
            index = question.index,
            question = question.question,
            score = question.score,
            fillerWordCount = question.fillerWordCount,
            durationSeconds = question.durationSeconds,
            coachFeedback = question.coachFeedback,
            transcript = question.transcript,
            fillerWords = question.fillerWords.toPersistentList(),
        )
    }.toPersistentList(),
)
