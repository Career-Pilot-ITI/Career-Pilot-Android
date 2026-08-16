package com.iti.careerpilot.ai.cache

import com.iti.core.model.bodylanguage.BodyLanguageEvaluation
import com.iti.core.model.bodylanguage.BodyLanguageMetrics
import com.iti.core.model.bodylanguage.FallbackReason
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

data class CachedSessionResult(
    val evaluation: BodyLanguageEvaluation? = null,
    val fallbackReason: FallbackReason? = null,
    val metrics: BodyLanguageMetrics? = null,
)

/**
 * Ephemeral in-memory storage for AI body language evaluations during active practice session viewing.
 *
 * NOTE: As per product requirements, video body language evaluations are strictly ephemeral
 * and are never persisted to a local database or remote backend. Once the user leaves the result
 * screen or the app process is terminated, this data is cleared.
 */
@Singleton
class InMemorySessionCache @Inject constructor() {

    private val cache = ConcurrentHashMap<String, CachedSessionResult>()

    fun getEvaluation(sessionId: Long): Pair<BodyLanguageEvaluation, FallbackReason?>? =
        getEvaluation(sessionId.toString())

    fun getEvaluation(sessionId: String): Pair<BodyLanguageEvaluation, FallbackReason?>? {
        val entry = cache[sessionId] ?: return null
        val eval = entry.evaluation ?: return null
        return Pair(eval, entry.fallbackReason)
    }

    fun getMetrics(sessionId: Long): BodyLanguageMetrics? = getMetrics(sessionId.toString())
    
    fun getMetrics(sessionId: String): BodyLanguageMetrics? = cache[sessionId]?.metrics

    fun put(
        sessionId: Long,
        evaluation: BodyLanguageEvaluation,
        fallbackReason: FallbackReason? = null,
        metrics: BodyLanguageMetrics? = null,
    ) = put(sessionId.toString(), evaluation, fallbackReason, metrics)

    fun put(
        sessionId: String,
        evaluation: BodyLanguageEvaluation,
        fallbackReason: FallbackReason? = null,
        metrics: BodyLanguageMetrics? = null,
    ) {
        val existing = cache[sessionId]
        cache[sessionId] = CachedSessionResult(
            evaluation = evaluation,
            fallbackReason = fallbackReason,
            metrics = metrics ?: existing?.metrics,
        )
    }

    fun putMetrics(sessionId: Long, metrics: BodyLanguageMetrics) = putMetrics(sessionId.toString(), metrics)

    fun putMetrics(sessionId: String, metrics: BodyLanguageMetrics) {
        val existing = cache[sessionId]
        if (existing != null) {
            cache[sessionId] = existing.copy(metrics = metrics)
        } else {
            cache[sessionId] = CachedSessionResult(
                evaluation = null,
                fallbackReason = null,
                metrics = metrics,
            )
        }
    }

    fun clear(sessionId: Long) = clear(sessionId.toString())

    fun clear(sessionId: String) {
        cache.remove(sessionId)
    }

    fun clearAll() {
        cache.clear()
    }
}
