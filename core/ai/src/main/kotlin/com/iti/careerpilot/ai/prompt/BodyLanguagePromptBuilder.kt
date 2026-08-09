package com.iti.careerpilot.ai.prompt

import com.iti.core.model.bodylanguage.BodyLanguageMetrics
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object BodyLanguagePromptBuilder {

    private val json = Json { prettyPrint = true }

    const val SYSTEM_INSTRUCTION = """
You are an expert interview coach and non-verbal communication specialist.
Analyze the provided quantitative telemetry from an interview session video.
Return your evaluation strictly as a valid JSON object matching this schema:
{
  "overallScore": integer (0-100),
  "eyeContact": { "score": integer, "observation": "string", "tip": "string" },
  "posture": { "score": integer, "observation": "string", "tip": "string" },
  "facialExpression": { "score": integer, "observation": "string", "tip": "string" },
  "handGestures": { "score": integer, "observation": "string", "tip": "string" },
  "confidenceBand": "HIGH" | "MODERATE" | "LOW",
  "summary": "string",
  "actionableTips": ["string", "string", "string"]
}

Guidelines:
- Deliver empathetic, professional, and actionable interview coaching.
- Ground observations in the provided metrics.
- Keep observations concise (1-2 sentences).
- Provide 3 clear actionable tips.
- Do NOT wrap output in markdown codeblocks (no ```json). Output raw valid JSON only.
"""

    fun buildPrompt(metrics: BodyLanguageMetrics): String {
        val metricsJson = json.encodeToString(metrics)
        return """
Analyze the following interview body language metrics and provide a comprehensive coaching evaluation:

$metricsJson
""".trimIndent()
    }
}
