package com.iti.careerpilot.ai.domain

import com.iti.careerpilot.ai.cache.InMemorySessionCache
import com.iti.careerpilot.ai.evaluator.AiContentGenerator
import com.iti.careerpilot.ai.evaluator.BodyLanguageAiEvaluator
import com.iti.careerpilot.ai.fallback.LocalBodyLanguageFallbackEngine
import com.iti.careerpilot.ai.FakeBodyLanguageData
import com.iti.core.model.bodylanguage.FallbackReason
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger

class EvaluateBodyLanguageUseCaseTest {

    private lateinit var cache: InMemorySessionCache
    private lateinit var fallbackEngine: LocalBodyLanguageFallbackEngine
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Before
    fun setup() {
        cache = InMemorySessionCache()
        fallbackEngine = LocalBodyLanguageFallbackEngine()
    }

    @Test
    fun `evaluates and saves to cache on first call`() = runTest {
        val callCount = AtomicInteger(0)
        val generator = AiContentGenerator {
            callCount.incrementAndGet()
            FakeBodyLanguageData.sampleEvaluationJson
        }
        val evaluator = BodyLanguageAiEvaluator(generator, fallbackEngine, json, Dispatchers.Unconfined)
        val useCase = EvaluateBodyLanguageUseCase(evaluator, cache)

        val (result, fallback) = useCase.invoke(100L, FakeBodyLanguageData.sampleMetrics)

        assertNotNull(result)
        assertNull(fallback)
        assertEquals(82, result.overallScore)
        assertEquals(1, callCount.get())
        assertEquals(result, cache.getEvaluation(100L)?.first)
        assertEquals(Pair(result, null), cache.getEvaluation(100L))
        assertEquals(FakeBodyLanguageData.sampleMetrics, cache.getMetrics(100L))
    }

    @Test
    fun `returns cached result on subsequent calls without re-evaluating and preserves fallback reason`() = runTest {
        val callCount = AtomicInteger(0)
        val generator = AiContentGenerator {
            callCount.incrementAndGet()
            FakeBodyLanguageData.sampleEvaluationJson
        }
        val evaluator = BodyLanguageAiEvaluator(generator, fallbackEngine, json, Dispatchers.Unconfined)
        val useCase = EvaluateBodyLanguageUseCase(evaluator, cache)

        // Prepopulate cache with fallback reason and metrics
        cache.put(
            sessionId = 200L,
            evaluation = FakeBodyLanguageData.sampleEvaluation,
            fallbackReason = FallbackReason.TIMEOUT,
            metrics = FakeBodyLanguageData.sampleMetrics,
        )

        val (result, fallback) = useCase.invoke(200L, FakeBodyLanguageData.sampleMetrics)

        assertEquals(FakeBodyLanguageData.sampleEvaluation, result)
        assertEquals(FallbackReason.TIMEOUT, fallback)
        assertEquals(0, callCount.get())
        assertEquals(FakeBodyLanguageData.sampleMetrics, cache.getMetrics(200L))
    }

}
