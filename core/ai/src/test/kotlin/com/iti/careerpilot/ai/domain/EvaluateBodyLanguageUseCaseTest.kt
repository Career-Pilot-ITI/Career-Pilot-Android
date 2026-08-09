package com.iti.careerpilot.ai.domain

import com.iti.careerpilot.ai.cache.InMemorySessionCache
import com.iti.careerpilot.ai.evaluator.AiContentGenerator
import com.iti.careerpilot.ai.evaluator.BodyLanguageAiEvaluator
import com.iti.careerpilot.ai.fallback.LocalBodyLanguageFallbackEngine
import com.iti.careerpilot.ai.testing.FakeBodyLanguageData
import com.iti.core.model.bodylanguage.FallbackReason
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class EvaluateBodyLanguageUseCaseTest {

    private lateinit var cache: InMemorySessionCache
    private lateinit var fallbackEngine: LocalBodyLanguageFallbackEngine

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
        val evaluator = BodyLanguageAiEvaluator(generator, fallbackEngine, Dispatchers.Unconfined)
        val useCase = EvaluateBodyLanguageUseCase(evaluator, cache) { true }

        val (result, fallback) = useCase.invoke(100L, FakeBodyLanguageData.sampleMetrics)

        assertNotNull(result)
        assertNull(fallback)
        assertEquals(82, result.overallScore)
        assertEquals(1, callCount.get())
        assertEquals(result, cache.get(100L))
    }

    @Test
    fun `returns cached result on subsequent calls without re-evaluating`() = runTest {
        val callCount = AtomicInteger(0)
        val generator = AiContentGenerator {
            callCount.incrementAndGet()
            FakeBodyLanguageData.sampleEvaluationJson
        }
        val evaluator = BodyLanguageAiEvaluator(generator, fallbackEngine, Dispatchers.Unconfined)
        val useCase = EvaluateBodyLanguageUseCase(evaluator, cache) { true }

        // Prepopulate cache
        cache.put(200L, FakeBodyLanguageData.sampleEvaluation)

        val (result, fallback) = useCase.invoke(200L, FakeBodyLanguageData.sampleMetrics)

        assertEquals(FakeBodyLanguageData.sampleEvaluation, result)
        assertNull(fallback)
        assertEquals(0, callCount.get())
    }

    @Test
    fun `kill switch disabled produces fallback and caches result`() = runTest {
        val generator = AiContentGenerator { FakeBodyLanguageData.sampleEvaluationJson }
        val evaluator = BodyLanguageAiEvaluator(generator, fallbackEngine, Dispatchers.Unconfined)
        val useCase = EvaluateBodyLanguageUseCase(evaluator, cache) { false }

        val (result, fallback) = useCase.invoke(300L, FakeBodyLanguageData.sampleMetrics)

        assertEquals(FallbackReason.KILL_SWITCH_DISABLED, fallback)
        assertNotNull(result)
        assertEquals(result, cache.get(300L))
    }
}
