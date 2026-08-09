package com.iti.careerpilot.ai.fallback

import com.iti.core.model.bodylanguage.BodyLanguageEvaluation
import com.iti.core.model.bodylanguage.BodyLanguageMetrics
import com.iti.core.model.bodylanguage.ConfidenceBand
import com.iti.core.model.bodylanguage.KeyMomentType
import com.iti.core.model.bodylanguage.MetricEvaluation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalBodyLanguageFallbackEngine @Inject constructor() {

    fun evaluate(metrics: BodyLanguageMetrics): BodyLanguageEvaluation {
        val eyeContact = evaluateEyeContact(metrics)
        val posture = evaluatePosture(metrics)
        val facial = evaluateFacialExpression(metrics)
        val hands = evaluateHands(metrics)

        // Weighted overall score: Eye Contact 30%, Posture 30%, Facial 20%, Hands 20%
        val overallScore = (
            eyeContact.score * 0.30f +
            posture.score * 0.30f +
            facial.score * 0.20f +
            hands.score * 0.20f
        ).toInt().coerceIn(0, 100)

        val confidenceBand = when {
            overallScore >= 75 -> ConfidenceBand.HIGH
            overallScore >= 50 -> ConfidenceBand.MODERATE
            else -> ConfidenceBand.LOW
        }

        val tips = mutableListOf<String>()
        if (eyeContact.score < 75) tips.add(eyeContact.tip)
        if (posture.score < 75) tips.add(posture.tip)
        if (facial.score < 75) tips.add(facial.tip)
        if (hands.score < 75) tips.add(hands.tip)
        if (tips.isEmpty()) {
            tips.add("Maintain your confident posture and steady eye contact in upcoming interviews.")
        }

        val summary = buildSummary(overallScore, confidenceBand, metrics)

        return BodyLanguageEvaluation(
            schemaVersion = 1,
            overallScore = overallScore,
            eyeContact = eyeContact,
            posture = posture,
            facialExpression = facial,
            handGestures = hands,
            confidenceBand = confidenceBand,
            summary = summary,
            actionableTips = tips.take(3),
        )
    }

    private fun evaluateEyeContact(metrics: BodyLanguageMetrics): MetricEvaluation {
        val score = metrics.eyeContactPercentage.toInt().coerceIn(0, 100)
        val lostCount = metrics.keyMoments.count { it.type == KeyMomentType.EYE_CONTACT_LOST }

        val observation = when {
            score >= 80 -> "Maintained strong, consistent eye contact (${score}% of session)."
            score >= 60 -> "Moderate eye contact (${score}%). Looked away $lostCount times."
            else -> "Infrequent eye contact (${score}%). Candidate frequently looked away from the camera."
        }

        val tip = when {
            score >= 80 -> "Great camera gaze! Keep looking directly into the lens when delivering key points."
            score >= 60 -> "Try to keep your eyes centered on the camera lens rather than looking around the room."
            else -> "Practice looking steadily at the camera to convey confidence and engagement."
        }

        return MetricEvaluation(score = score, observation = observation, tip = tip)
    }

    private fun evaluatePosture(metrics: BodyLanguageMetrics): MetricEvaluation {
        val score = (100f - metrics.slouchPercentage).toInt().coerceIn(0, 100)
        val slouchMoments = metrics.keyMoments.count { it.type == KeyMomentType.SLOUCH_START }

        val observation = when {
            score >= 80 -> "Excellent upright posture throughout the session (slouch duration: ${metrics.slouchPercentage.toInt()}%)."
            score >= 60 -> "Generally acceptable posture with occasional slouching (${metrics.slouchPercentage.toInt()}%)."
            else -> "Significant slouching detected (${metrics.slouchPercentage.toInt()}% of session duration) with $slouchMoments posture shifts."
        }

        val tip = when {
            score >= 80 -> "Keep your shoulders relaxed and back aligned for optimal presence."
            score >= 60 -> "Set up your screen at eye level to naturally prevent slouching."
            else -> "Sit upright with your back supported and shoulders relaxed to project readiness."
        }

        return MetricEvaluation(score = score, observation = observation, tip = tip)
    }

    private fun evaluateFacialExpression(metrics: BodyLanguageMetrics): MetricEvaluation {
        val smileScore = ((metrics.averageSmile * 0.5f + metrics.maxSmile * 0.5f) * 100f).toInt().coerceIn(0, 100)
        val smilePeaks = metrics.keyMoments.count { it.type == KeyMomentType.SMILE_PEAK }

        val observation = when {
            smileScore >= 40 || smilePeaks > 0 -> "Warm and engaging facial expressions with natural smiles observed."
            else -> "Neutral or serious facial expression maintained throughout the session."
        }

        val tip = when {
            smileScore >= 40 || smilePeaks > 0 -> "Natural warmth builds great rapport with interviewers."
            else -> "Remember to smile warmly during greetings and when concluding answers."
        }

        return MetricEvaluation(score = (50 + smileScore / 2).coerceIn(0, 100), observation = observation, tip = tip)
    }

    private fun evaluateHands(metrics: BodyLanguageMetrics): MetricEvaluation {
        val baseScore = (metrics.handsVisiblePercentage * 0.7f).toInt()
        val penalty = (metrics.handToFaceTouchCount * 10) + (metrics.fidgetScore * 30f).toInt()
        val score = (baseScore + 40 - penalty).coerceIn(0, 100)

        val observation = when {
            metrics.handToFaceTouchCount > 2 -> "Frequent hand-to-face touches detected (${metrics.handToFaceTouchCount} times) suggesting nervousness."
            metrics.fidgetScore > 0.4f -> "Noticeable hand fidgeting or rapid hand movements detected."
            metrics.handsVisiblePercentage > 30f -> "Expressive, controlled hand gestures with good visibility (${metrics.handsVisiblePercentage.toInt()}%)."
            else -> "Hands mostly rested out of camera view."
        }

        val tip = when {
            metrics.handToFaceTouchCount > 0 -> "Avoid touching your face or chin, as it can distract interviewers."
            metrics.fidgetScore > 0.3f -> "Rest your hands calmly on the desk or in your lap between gestures."
            else -> "Use purposeful hand gestures to emphasize key achievements."
        }

        return MetricEvaluation(score = score, observation = observation, tip = tip)
    }

    private fun buildSummary(
        overallScore: Int,
        confidenceBand: ConfidenceBand,
        metrics: BodyLanguageMetrics,
    ): String {
        return when (confidenceBand) {
            ConfidenceBand.HIGH -> "Demonstrated strong, confident presence with steady eye contact (${metrics.eyeContactPercentage.toInt()}%) and solid posture."
            ConfidenceBand.MODERATE -> "Good non-verbal communication with opportunities to improve posture consistency and reduce look-aways."
            ConfidenceBand.LOW -> "Body language indicators suggest nervousness or low engagement. Focus on camera alignment, upright posture, and reducing fidgeting."
        }
    }
}
