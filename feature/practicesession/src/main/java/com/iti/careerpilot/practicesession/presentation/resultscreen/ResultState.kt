package com.iti.careerpilot.practicesession.presentation.resultscreen

import androidx.compose.runtime.Immutable
import com.iti.core.model.bodylanguage.BodyLanguageEvaluation
import com.iti.core.model.bodylanguage.BodyLanguageMetrics
import com.iti.core.model.bodylanguage.FallbackReason
import com.iti.careerpilot.practicesession.domain.models.SessionResult

@Immutable
data class ResultState(
    val isLoading: Boolean = false,
    val sessionId: Long? = null,
    val sessionResult: SessionResult? = null,
    val bodyLanguageMetrics: BodyLanguageMetrics? = null,
    val isEvaluatingBodyLanguage: Boolean = false,
    val bodyLanguageEvaluation: BodyLanguageEvaluation? = null,
    val bodyLanguageFallbackReason: FallbackReason? = null,
)