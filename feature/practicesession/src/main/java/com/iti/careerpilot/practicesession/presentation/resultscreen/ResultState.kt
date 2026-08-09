package com.iti.careerpilot.practicesession.presentation.resultscreen

import androidx.compose.runtime.Immutable
import com.iti.core.model.bodylanguage.BodyLanguageEvaluation
import com.iti.core.model.bodylanguage.BodyLanguageMetrics
import com.iti.core.model.bodylanguage.FallbackReason
import com.iti.careerpilot.practicesession.domain.models.SessionResult

@Immutable
sealed interface BodyLanguageUiState {
    data object Idle : BodyLanguageUiState
    data object Loading : BodyLanguageUiState
    data class Success(val evaluation: BodyLanguageEvaluation) : BodyLanguageUiState
    data class FallbackUsed(
        val evaluation: BodyLanguageEvaluation,
        val reason: FallbackReason,
    ) : BodyLanguageUiState
}

@Immutable
data class ResultState(
    val isLoading: Boolean = false,
    val sessionId: Long? = null,
    val sessionResult: SessionResult? = null,
    val bodyLanguageMetrics: BodyLanguageMetrics? = null,
    val isEvaluatingBodyLanguage: Boolean = false,
    val bodyLanguageEvaluation: BodyLanguageEvaluation? = null,
    val bodyLanguageFallbackReason: FallbackReason? = null,
) {
    val bodyLanguageUiState: BodyLanguageUiState
        get() = when {
            isEvaluatingBodyLanguage -> BodyLanguageUiState.Loading
            bodyLanguageEvaluation != null && bodyLanguageFallbackReason != null ->
                BodyLanguageUiState.FallbackUsed(bodyLanguageEvaluation, bodyLanguageFallbackReason)
            bodyLanguageEvaluation != null ->
                BodyLanguageUiState.Success(bodyLanguageEvaluation)
            bodyLanguageMetrics != null ->
                BodyLanguageUiState.Loading
            else ->
                BodyLanguageUiState.Idle
        }
}