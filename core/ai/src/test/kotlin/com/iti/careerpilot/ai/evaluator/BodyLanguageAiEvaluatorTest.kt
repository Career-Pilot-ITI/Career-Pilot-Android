package com.iti.careerpilot.ai.evaluator

import com.iti.careerpilot.ai.fallback.LocalBodyLanguageFallbackEngine
import com.iti.careerpilot.ai.testing.FakeBodyLanguageData
import com.iti.core.model.bodylanguage.FallbackReason
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import kotlinx.serialization.json.Json
import java.io.IOException

class BodyLanguageAiEvaluatorTest {

    private lateinit var fallbackEngine: LocalBodyLanguageFallbackEngine
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Before
    fun setup() {
        fallbackEngine = LocalBodyLanguageFallbackEngine()
    }

    @Test
    fun `successful AI response parses correctly without fallback`() = runTest {
        val generator = AiContentGenerator { FakeBodyLanguageData.sampleEvaluationJson }
        val evaluator = BodyLanguageAiEvaluator(
            contentGenerator = generator,
            fallbackEngine = fallbackEngine,
            json = json,
            ioDispatcher = Dispatchers.Unconfined,
        )

        val (evaluation, fallbackReason) = evaluator.evaluate(FakeBodyLanguageData.sampleMetrics)

        assertNull(fallbackReason)
        assertNotNull(evaluation)
        assertEquals(82, evaluation.overallScore)
        assertEquals(80, evaluation.eyeContact.score)
    }

    @Test
    fun `AI response with leading and trailing whitespace and markdown fences parses correctly`() = runTest {
        val wrappedJson = "  \n\n  ```json\n${FakeBodyLanguageData.sampleEvaluationJson}\n```\n  \n"
        val generator = AiContentGenerator { wrappedJson }
        val evaluator = BodyLanguageAiEvaluator(
            contentGenerator = generator,
            fallbackEngine = fallbackEngine,
            json = json,
            ioDispatcher = Dispatchers.Unconfined,
        )

        val (evaluation, fallbackReason) = evaluator.evaluate(FakeBodyLanguageData.sampleMetrics)

        assertNull(fallbackReason)
        assertNotNull(evaluation)
        assertEquals(82, evaluation.overallScore)
        assertEquals(80, evaluation.eyeContact.score)
    }

    @Test
    fun `AI response wrapped in raw markdown fence without json tag parses correctly`() = runTest {
        val wrappedJson = " \n```\n${FakeBodyLanguageData.sampleEvaluationJson}\n``` "
        val generator = AiContentGenerator { wrappedJson }
        val evaluator = BodyLanguageAiEvaluator(
            contentGenerator = generator,
            fallbackEngine = fallbackEngine,
            json = json,
            ioDispatcher = Dispatchers.Unconfined,
        )

        val (evaluation, fallbackReason) = evaluator.evaluate(FakeBodyLanguageData.sampleMetrics)

        assertNull(fallbackReason)
        assertNotNull(evaluation)
        assertEquals(82, evaluation.overallScore)
        assertEquals(80, evaluation.eyeContact.score)
    }

    @Test
    fun `kill switch disabled immediately returns heuristic fallback`() = runTest {
        val generator = AiContentGenerator { FakeBodyLanguageData.sampleEvaluationJson }
        val evaluator = BodyLanguageAiEvaluator(
            contentGenerator = generator,
            fallbackEngine = fallbackEngine,
            json = json,
            ioDispatcher = Dispatchers.Unconfined,
        )

        val (evaluation, fallbackReason) = evaluator.evaluate(
            metrics = FakeBodyLanguageData.sampleMetrics,
            isAiEnabled = false,
        )

        assertEquals(FallbackReason.KILL_SWITCH_DISABLED, fallbackReason)
        assertNotNull(evaluation)
    }

    @Test
    fun `network IOException triggers OFFLINE fallback`() = runTest {
        val generator = AiContentGenerator { throw IOException("No internet connection") }
        val evaluator = BodyLanguageAiEvaluator(
            contentGenerator = generator,
            fallbackEngine = fallbackEngine,
            json = json,
            ioDispatcher = Dispatchers.Unconfined,
        )

        val (evaluation, fallbackReason) = evaluator.evaluate(FakeBodyLanguageData.sampleMetrics)

        assertEquals(FallbackReason.OFFLINE, fallbackReason)
        assertNotNull(evaluation)
    }

    @Test
    fun `malformed JSON response triggers PARSE_FAILURE fallback`() = runTest {
        val generator = AiContentGenerator { "{ invalid json ... }" }
        val evaluator = BodyLanguageAiEvaluator(
            contentGenerator = generator,
            fallbackEngine = fallbackEngine,
            json = json,
            ioDispatcher = Dispatchers.Unconfined,
        )

        val (evaluation, fallbackReason) = evaluator.evaluate(FakeBodyLanguageData.sampleMetrics)

        assertEquals(FallbackReason.PARSE_FAILURE, fallbackReason)
        assertNotNull(evaluation)
    }

    @Test
    fun `timeout triggers TIMEOUT fallback`() = runTest {
        val generator = AiContentGenerator {
            delay(25_000L)
            FakeBodyLanguageData.sampleEvaluationJson
        }
        val evaluator = BodyLanguageAiEvaluator(
            contentGenerator = generator,
            fallbackEngine = fallbackEngine,
            json = json,
            ioDispatcher = Dispatchers.Unconfined,
        )

        val (evaluation, fallbackReason) = evaluator.evaluate(FakeBodyLanguageData.sampleMetrics)

        assertEquals(FallbackReason.TIMEOUT, fallbackReason)
        assertNotNull(evaluation)
    }
}
