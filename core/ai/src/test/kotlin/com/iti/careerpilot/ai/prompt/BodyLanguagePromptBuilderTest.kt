package com.iti.careerpilot.ai.prompt

import com.iti.core.model.bodylanguage.BodyLanguageMetrics
import org.junit.Assert.assertTrue
import org.junit.Test

class BodyLanguagePromptBuilderTest {

    @Test
    fun `buildPrompt includes session telemetry`() {
        val metrics = BodyLanguageMetrics.EMPTY.copy(
            sessionDurationMs = 90_000L,
            eyeContactPercentage = 84.5f,
            slouchPercentage = 5.2f,
        )

        val prompt = BodyLanguagePromptBuilder.buildPrompt(metrics)

        assertTrue(prompt.contains("84.5"))
        assertTrue(prompt.contains("90000"))
        assertTrue(prompt.contains("5.2"))
    }
}
