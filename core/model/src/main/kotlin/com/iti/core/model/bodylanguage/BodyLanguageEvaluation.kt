package com.iti.core.model.bodylanguage

import kotlinx.serialization.Serializable

@Serializable
enum class ConfidenceBand {
    HIGH,
    MODERATE,
    LOW,
}

@Serializable
data class MetricEvaluation(
    val score: Int,                // 0–100 normalized score
    val observation: String,       // What the model / analyzer saw
    val tip: String,               // Actionable interview advice
)

@Serializable
data class BodyLanguageEvaluation(
    val schemaVersion: Int = 1,
    val overallScore: Int,
    val eyeContact: MetricEvaluation,
    val posture: MetricEvaluation,
    val facialExpression: MetricEvaluation,
    val handGestures: MetricEvaluation,
    val confidenceBand: ConfidenceBand,
    val summary: String,
    val actionableTips: List<String>,
)

enum class FallbackReason {
    OFFLINE,
    TIMEOUT,
    SAFETY_BLOCK,
    KILL_SWITCH_DISABLED,
    QUOTA_EXCEEDED,
    PARSE_FAILURE,
}
