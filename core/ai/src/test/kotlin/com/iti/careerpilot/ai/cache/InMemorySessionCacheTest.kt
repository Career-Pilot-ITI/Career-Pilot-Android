package com.iti.careerpilot.ai.cache

import com.iti.careerpilot.ai.testing.FakeBodyLanguageData
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

        val retrieved = cache.get(100L)
        assertNotNull(retrieved)
        assertEquals(82, retrieved?.overallScore)
        assertEquals(eval, retrieved)
    }

    @Test
    fun `get returns null when key does not exist`() {
        assertNull(cache.get(999L))
    }

    @Test
    fun `clear removes specific session evaluation`() {
        val eval = FakeBodyLanguageData.sampleEvaluation
        cache.put(101L, eval)
        cache.put(102L, eval)

        cache.clear(101L)

        assertNull(cache.get(101L))
        assertNotNull(cache.get(102L))
    }

    @Test
    fun `clearAll removes all evaluations`() {
        val eval = FakeBodyLanguageData.sampleEvaluation
        cache.put(101L, eval)
        cache.put(102L, eval)

        cache.clearAll()

        assertNull(cache.get(101L))
        assertNull(cache.get(102L))
    }

    @Test
    fun `concurrent put and get operations are thread-safe`() {
        val executor = Executors.newFixedThreadPool(8)
        val eval = FakeBodyLanguageData.sampleEvaluation

        for (i in 0 until 100) {
            val sessionId = i.toLong()
            executor.submit {
                cache.put(sessionId, eval)
                assertEquals(eval, cache.get(sessionId))
            }
        }

        executor.shutdown()
        val finished = executor.awaitTermination(5, TimeUnit.SECONDS)
        assertEquals(true, finished)
    }
}
