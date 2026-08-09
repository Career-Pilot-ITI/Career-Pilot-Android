package com.iti.careerpilot.ai.cache

import com.iti.core.model.bodylanguage.BodyLanguageEvaluation
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ephemeral in-memory storage for AI body language evaluations during active practice session viewing.
 *
 * NOTE: As per product requirements, video body language evaluations are strictly ephemeral
 * and are never persisted to a local database or remote backend. Once the user leaves the result
 * screen or the app process is terminated, this data is cleared.
 */
@Singleton
class InMemorySessionCache @Inject constructor() {

    private val cache = ConcurrentHashMap<Long, BodyLanguageEvaluation>()

    fun get(sessionId: Long): BodyLanguageEvaluation? = cache[sessionId]

    fun put(sessionId: Long, evaluation: BodyLanguageEvaluation) {
        cache[sessionId] = evaluation
    }

    fun clear(sessionId: Long) {
        cache.remove(sessionId)
    }

    fun clearAll() {
        cache.clear()
    }
}
