package com.iti.careerpilot.ai.domain

import com.iti.careerpilot.ai.cache.InMemorySessionCache
import com.iti.careerpilot.ai.evaluator.BodyLanguageAiEvaluator
import com.iti.core.model.bodylanguage.BodyLanguageEvaluation
import com.iti.core.model.bodylanguage.BodyLanguageMetrics
import com.iti.core.model.bodylanguage.FallbackReason
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single-responsibility Use Case for evaluating video interview body language.
 *
 * Ephemeral flow:
 * 1. Checks [InMemorySessionCache] for existing evaluation (e.g. rotation during result viewing).
 * 2. Reads Remote Config kill-switch via [BodyLanguageAiFeatureToggle].
 * 3. Evaluates telemetry via Firebase Vertex AI (with automatic heuristic fallback on errors/offline/timeout).
 * 4. Caches result in-memory for the duration of the result screen lifecycle.
 */
@Singleton
class EvaluateBodyLanguageUseCase @Inject constructor(
    private val evaluator: BodyLanguageAiEvaluator,
    private val cache: InMemorySessionCache,
) {

    suspend operator fun invoke(
        sessionId: Long,
        metrics: BodyLanguageMetrics,
    ): Pair<BodyLanguageEvaluation, FallbackReason?> = invoke(sessionId.toString(), metrics)

    suspend operator fun invoke(
        sessionId: String,
        metrics: BodyLanguageMetrics,
    ): Pair<BodyLanguageEvaluation, FallbackReason?> {
        val cached = cache.getEvaluation(sessionId)
        if (cached != null) {
            return cached
        }

        val result = evaluator.evaluate(metrics)
        cache.put(
            sessionId = sessionId,
            evaluation = result.first,
            fallbackReason = result.second,
            metrics = metrics,
        )
        return result
    }
}
