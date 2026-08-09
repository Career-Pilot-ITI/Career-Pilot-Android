package com.iti.careerpilot.ai.evaluator

import com.iti.careerpilot.ai.fallback.LocalBodyLanguageFallbackEngine
import com.iti.careerpilot.ai.prompt.BodyLanguagePromptBuilder
import com.iti.common.dispatcher.CareerPilotDispatchers.IO
import com.iti.common.dispatcher.Dispatcher
import com.iti.core.model.bodylanguage.BodyLanguageEvaluation
import com.iti.core.model.bodylanguage.BodyLanguageMetrics
import com.iti.core.model.bodylanguage.FallbackReason
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BodyLanguageAiEvaluator @Inject constructor(
    private val contentGenerator: AiContentGenerator,
    private val fallbackEngine: LocalBodyLanguageFallbackEngine,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Evaluates interview body language telemetry.
     *
     * Returns a [Pair] of the evaluation result and an optional [FallbackReason].
     * If the evaluation was generated directly by Firebase Vertex AI, the reason is `null`.
     * If a fallback heuristic was used (timeout, offline, parse error, kill switch),
     * the reason is populated for user disclosure.
     */
    suspend fun evaluate(
        metrics: BodyLanguageMetrics,
        isAiEnabled: Boolean = true,
    ): Pair<BodyLanguageEvaluation, FallbackReason?> = withContext(ioDispatcher) {
        if (!isAiEnabled) {
            return@withContext Pair(
                fallbackEngine.evaluate(metrics),
                FallbackReason.KILL_SWITCH_DISABLED,
            )
        }

        try {
            withTimeout(20_000L) {
                val prompt = BodyLanguagePromptBuilder.buildPrompt(metrics)
                val responseText = contentGenerator.generateContent(prompt)

                // Clean potential markdown wrappers if model emitted them
                val cleanedJson = responseText
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val evaluation = json.decodeFromString<BodyLanguageEvaluation>(cleanedJson)
                Pair(evaluation, null)
            }
        } catch (e: TimeoutCancellationException) {
            Pair(fallbackEngine.evaluate(metrics), FallbackReason.TIMEOUT)
        } catch (e: SerializationException) {
            Pair(fallbackEngine.evaluate(metrics), FallbackReason.PARSE_FAILURE)
        } catch (e: IOException) {
            Pair(fallbackEngine.evaluate(metrics), FallbackReason.OFFLINE)
        } catch (e: Exception) {
            Pair(fallbackEngine.evaluate(metrics), FallbackReason.OFFLINE)
        }
    }
}
