package com.iti.careerpilot.ai.testing

import com.iti.core.model.bodylanguage.BodyLanguageEvaluation
import com.iti.core.model.bodylanguage.BodyLanguageMetrics
import com.iti.core.model.bodylanguage.ConfidenceBand
import com.iti.core.model.bodylanguage.KeyMoment
import com.iti.core.model.bodylanguage.KeyMomentType
import com.iti.core.model.bodylanguage.MetricEvaluation

object FakeBodyLanguageData {

    val sampleMetrics = BodyLanguageMetrics(
        schemaVersion = 1,
        sessionDurationMs = 120_000L,
        faceDetectionPercentage = 95.0f,
        poseDetectionPercentage = 92.0f,
        handsDetectionPercentage = 65.0f,
        totalFramesAnalyzed = 450,
        averageSmile = 0.45f,
        maxSmile = 0.85f,
        eyeContactPercentage = 78.5f,
        timeLookingAwayMs = 25_800L,
        faceLostCount = 1,
        averageTorsoLeanDeg = 4.2f,
        averageShoulderTiltDeg = 2.1f,
        slouchPercentage = 12.0f,
        postureChanges = 3,
        handsVisiblePercentage = 65.0f,
        handToFaceTouchCount = 2,
        fidgetScore = 0.15f,
        keyMoments = listOf(
            KeyMoment(timestampMs = 15_000L, type = KeyMomentType.SMILE_PEAK, intensity = 0.85f),
            KeyMoment(timestampMs = 45_000L, type = KeyMomentType.EYE_CONTACT_LOST, durationMs = 3_000L),
            KeyMoment(timestampMs = 75_000L, type = KeyMomentType.HAND_FIDGET_SPIKE, intensity = 0.7f),
        ),
    )

    val sampleEvaluation = BodyLanguageEvaluation(
        schemaVersion = 1,
        overallScore = 82,
        eyeContact = MetricEvaluation(
            score = 80,
            observation = "Maintained consistent eye contact 79% of the time with minimal look-aways.",
            tip = "Try to maintain camera contact especially when delivering key technical points.",
        ),
        posture = MetricEvaluation(
            score = 85,
            observation = "Upright posture maintained throughout with only 12% slouch duration.",
            tip = "Keep your shoulders relaxed and squared to the camera.",
        ),
        facialExpression = MetricEvaluation(
            score = 88,
            observation = "Warm facial expressions with natural smiles at appropriate moments.",
            tip = "Keep smiling naturally during greetings and conclusions.",
        ),
        handGestures = MetricEvaluation(
            score = 75,
            observation = "Hands visible 65% of the time, 2 minor hand-to-face touches detected.",
            tip = "Keep hands resting comfortably when not gesturing for emphasis.",
        ),
        confidenceBand = ConfidenceBand.HIGH,
        summary = "Strong non-verbal presence with great eye contact and upright posture. Minor fidgeting noted.",
        actionableTips = listOf(
            "Look directly into the camera lens when making key points",
            "Keep hands visible and still when listening to questions",
            "Maintain upright posture during difficult questions",
        ),
    )

    val sampleEvaluationJson = """
        {
          "overallScore": 82,
          "eyeContact": {
            "score": 80,
            "observation": "Maintained consistent eye contact 79% of the time with minimal look-aways.",
            "tip": "Try to maintain camera contact especially when delivering key technical points."
          },
          "posture": {
            "score": 85,
            "observation": "Upright posture maintained throughout with only 12% slouch duration.",
            "tip": "Keep your shoulders relaxed and squared to the camera."
          },
          "facialExpression": {
            "score": 88,
            "observation": "Warm facial expressions with natural smiles at appropriate moments.",
            "tip": "Keep smiling naturally during greetings and conclusions."
          },
          "handGestures": {
            "score": 75,
            "observation": "Hands visible 65% of the time, 2 minor hand-to-face touches detected.",
            "tip": "Keep hands resting comfortably when not gesturing for emphasis."
          },
          "confidenceBand": "HIGH",
          "summary": "Strong non-verbal presence with great eye contact and upright posture. Minor fidgeting noted.",
          "actionableTips": [
            "Look directly into the camera lens when making key points",
            "Keep hands visible and still when listening to questions",
            "Maintain upright posture during difficult questions"
          ]
        }
    """.trimIndent()
}
