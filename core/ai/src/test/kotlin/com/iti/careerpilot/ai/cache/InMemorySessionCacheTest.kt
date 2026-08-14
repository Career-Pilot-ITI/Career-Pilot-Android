package com.iti.careerpilot.ai.cache

import com.iti.careerpilot.ai.FakeBodyLanguageData
import com.iti.core.model.bodylanguage.FallbackReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class InMemorySessionCacheTest {

    private lateinit var cache: InMemorySessionCache

    @Before
    fun setup() {
        cache = InMemorySessionCache()
    }

    @Test
    fun `put and get returns correct evaluation`() {
        val eval = FakeBodyLanguageData.sampleEvaluation
        cache.put(100L, eval)

        val retrieved = cache.getEvaluation(100L)?.first
        assertNotNull(retrieved)
        assertEquals(82, retrieved?.overallScore)
        assertEquals(eval, retrieved)
    }

    @Test
    fun `put with fallback reason and metrics returns them in getEvaluation and getMetrics`() {
        val eval = FakeBodyLanguageData.sampleEvaluation
        val metrics = FakeBodyLanguageData.sampleMetrics
        cache.put(100L, eval, FallbackReason.OFFLINE, metrics)

        val cachedEvaluation = cache.getEvaluation(100L)
        assertNotNull(cachedEvaluation)
        assertEquals(eval, cachedEvaluation?.first)
        assertEquals(FallbackReason.OFFLINE, cachedEvaluation?.second)

        val cachedMetrics = cache.getMetrics(100L)
        assertNotNull(cachedMetrics)
        assertEquals(metrics, cachedMetrics)
    }

    @Test
    fun `putMetrics updates metrics on existing cached session`() {
        val eval = FakeBodyLanguageData.sampleEvaluation
        val metrics = FakeBodyLanguageData.sampleMetrics
        cache.put(100L, eval)

        assertNull(cache.getMetrics(100L))

        cache.putMetrics(100L, metrics)
        assertEquals(metrics, cache.getMetrics(100L))
        assertEquals(eval, cache.getEvaluation(100L)?.first)
    }

    @Test
    fun `get returns null when key does not exist`() {
        assertNull(cache.getEvaluation(999L))
        assertNull(cache.getMetrics(999L))
    }

    @Test
    fun `clear removes specific session evaluation`() {
        val eval = FakeBodyLanguageData.sampleEvaluation
        cache.put(101L, eval)
        cache.put(102L, eval)

        cache.clear(101L)

        assertNull(cache.getEvaluation(101L))
        assertNotNull(cache.getEvaluation(102L))
    }

    @Test
    fun `clearAll removes all evaluations`() {
        val eval = FakeBodyLanguageData.sampleEvaluation
        cache.put(101L, eval)
        cache.put(102L, eval)

        cache.clearAll()

        assertNull(cache.getEvaluation(101L))
        assertNull(cache.getEvaluation(102L))
    }

    @Test
    fun `concurrent put and get operations are thread-safe`() {
        val executor = Executors.newFixedThreadPool(8)
        val eval = FakeBodyLanguageData.sampleEvaluation

        for (i in 0 until 100) {
            val sessionId = i.toLong()
            executor.submit {
                cache.put(sessionId, eval)
                assertEquals(eval, cache.getEvaluation(sessionId)?.first)
            }
        }

        executor.shutdown()
        val finished = executor.awaitTermination(5, TimeUnit.SECONDS)
        assertEquals(true, finished)
    }
}
