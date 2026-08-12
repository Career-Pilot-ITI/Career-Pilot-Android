package com.iti.careerpilot.ai.fallback

import com.iti.core.model.bodylanguage.BodyLanguageEvaluation
import com.iti.core.model.bodylanguage.BodyLanguageMetrics
import com.iti.core.model.bodylanguage.ConfidenceBand
import com.iti.core.model.bodylanguage.KeyMomentType
import com.iti.core.model.bodylanguage.MetricEvaluation
import kotlin.math.roundToInt
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalBodyLanguageFallbackEngine @Inject constructor() {

    fun evaluate(metrics: BodyLanguageMetrics): BodyLanguageEvaluation {
        val hasNoDetectedLandmarks = !metrics.isCandidateDetected

        if (hasNoDetectedLandmarks) {
            return BodyLanguageEvaluation(
                schemaVersion = 1,
                overallScore = 0,
                eyeContact = MetricEvaluation(
                    score = 0,
                    observation = "Candidate out of camera view. Eye contact could not be tracked.",
                    tip = "Align your camera at eye level and face the lens directly."
                ),
                posture = MetricEvaluation(
                    score = 0,
                    observation = "No body posture detected during recording.",
                    tip = "Sit upright in front of the camera so head and shoulders are visible."
                ),
                facialExpression = MetricEvaluation(
                    score = 0,
                    observation = "Facial features out of frame.",
                    tip = "Ensure consistent lighting on your face."
                ),
                handGestures = MetricEvaluation(
                    score = 0,
                    observation = "Hands out of camera view.",
                    tip = "Position hands in upper chest frame for natural gestures."
                ),
                confidenceBand = ConfidenceBand.LOW,
                summary = "Candidate was out of camera view for the recording. Position your webcam at eye level.",
                actionableTips = listOf(
                    "Position webcam at eye level to keep your head and shoulders visible.",
                    "Ensure adequate front lighting so facial features are recognized.",
                    "Face the camera directly while speaking to maximize non-verbal presence."
                ),
            )
        }

        val eyeContact = evaluateEyeContact(metrics)
        val posture = evaluatePosture(metrics)
        val facial = evaluateFacialExpression(metrics)
        val hands = evaluateHands(metrics)

        // Weighted overall score: Eye Contact 30%, Posture 25%, Hands 25%, Facial 20%
        val overallScore = (
            eyeContact.score * 0.30f +
            posture.score * 0.25f +
            hands.score * 0.25f +
            facial.score * 0.20f
        ).toInt().coerceIn(0, 100)

        val minDimensionScore = minOf(eyeContact.score, posture.score, facial.score, hands.score)
        val confidenceBand = when {
            overallScore >= 70 && minDimensionScore >= 45 -> ConfidenceBand.HIGH
            overallScore < 50 -> ConfidenceBand.LOW
            else -> ConfidenceBand.MODERATE
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
        val e = metrics.eyeContactPercentage.coerceIn(0f, 100f)
        val lostCount = metrics.keyMoments.count { it.type == KeyMomentType.EYE_CONTACT_LOST }

        val rawScore = when {
            e < 40f -> 30f + e * 1.0f
            e < 50f -> 70f + (e - 40f) * 1.8f
            e <= 75f -> 88f + (e - 50f) * 0.40f
            else -> 98f - (e - 75f) * 1.12f
        }
        val score = rawScore.roundToInt().coerceIn(0, 100)

        val gazeInt = e.toInt()
        val observation = when {
            e in 50f..75f -> "Optimal, natural eye contact maintained (${gazeInt}% of session)."
            e > 75f -> "Constant camera focus (${gazeInt}%). Unbroken staring can feel rigid or unnatural."
            e in 40f..<50f -> "Moderate eye contact (${gazeInt}%). Looked away $lostCount times."
            else -> "Low eye contact (${gazeInt}%). Candidate frequently looked away from the camera."
        }

        val tip = when {
            e in 50f..75f -> "Maintain this balanced camera gaze; it projects confidence and authenticity."
            e > 75f -> "Blink naturally and take brief cognitive gaze breaks while answering."
            e in 40f..<50f -> "Position a visual cue near your camera lens to help draw your gaze back."
            else -> "Elevate your camera to eye level and look directly into the lens when delivering key points."
        }

        return MetricEvaluation(score = score, observation = observation, tip = tip)
    }

    private fun evaluatePosture(metrics: BodyLanguageMetrics): MetricEvaluation {
        val torsoLean = metrics.averageTorsoLeanDeg
        val slouchPct = metrics.slouchPercentage
        val postureShifts = metrics.postureChanges
        val shoulderTilt = metrics.averageShoulderTiltDeg

        val leanBonus = if (torsoLean in 5f..15f) 5 else if (torsoLean > 20f || torsoLean < -5f) -5 else 0
        val tiltDeduction = if (kotlin.math.abs(shoulderTilt) > 6f) 5 else 0
        val shiftDeduction = (postureShifts * 2).coerceAtMost(15)
        val slouchDeduction = (slouchPct * 0.8f).toInt()

        val score = (90 + leanBonus - slouchDeduction - shiftDeduction - tiltDeduction).coerceIn(0, 100)

        val observation = when {
            score >= 85 -> "Excellent upright posture with engaged forward presence (slouch: ${slouchPct.toInt()}%)."
            score >= 65 -> "Generally good posture with occasional slouching (${slouchPct.toInt()}%) or minor movement."
            else -> "Noticeable slouching detected (${slouchPct.toInt()}% of session) with $postureShifts posture shifts."
        }

        val tip = when {
            score >= 85 -> "Keep your shoulders relaxed and back aligned for optimal presence."
            score >= 65 -> "Set up your screen at eye level to naturally support an upright posture."
            else -> "Sit upright with your back supported and lean slightly forward (+5° to +10°) to project readiness."
        }

        return MetricEvaluation(score = score, observation = observation, tip = tip)
    }

    private fun evaluateFacialExpression(metrics: BodyLanguageMetrics): MetricEvaluation {
        val avgExpressiveness = metrics.averageSmile.coerceIn(0f, 1f)
        val expressivePeaks = metrics.keyMoments.count { it.type == KeyMomentType.SMILE_PEAK }

        val peakBonus = (expressivePeaks * 6).coerceAtMost(18)
        val animationBonus = when {
            avgExpressiveness < 0.08f -> (avgExpressiveness / 0.08f * 10f).toInt()
            avgExpressiveness <= 0.40f -> 10
            avgExpressiveness <= 0.60f -> (10f - ((avgExpressiveness - 0.40f) / 0.20f) * 10f).toInt()
            else -> 0
        }
        val rawScore = 78 + peakBonus + animationBonus
        val score = rawScore.coerceIn(0, 100)

        val observation = when {
            expressivePeaks > 0 -> "Dynamic, animated delivery with expressive emphasis peaks."
            avgExpressiveness in 0.08f..0.40f -> "Engaged, natural delivery animation."
            avgExpressiveness > 0.40f -> "High facial expressiveness throughout delivery."
            else -> "Subdued facial expressiveness with low animation."
        }

        val tip = when {
            expressivePeaks > 0 -> "Maintain your animated facial engagement to reinforce emphasis on key responses."
            avgExpressiveness in 0.08f..0.40f -> "Great natural energy; continue using subtle facial animation to emphasize main ideas."
            avgExpressiveness > 0.40f -> "Balance active delivery energy with composed neutral pauses when detailing complex points."
            else -> "Incorporate natural facial animation and emphasis peaks to add dynamism to your delivery."
        }

        return MetricEvaluation(score = score, observation = observation, tip = tip)
    }

    private fun evaluateHands(metrics: BodyLanguageMetrics): MetricEvaluation {
        val v = metrics.handsVisiblePercentage.coerceIn(0f, 100f)
        val touches = metrics.handToFaceTouchCount
        val fidget = metrics.fidgetScore

        val baseScore = when {
            v < 25f -> 65f + (v / 25f) * 27f
            v <= 50f -> 92f
            else -> 92f - ((v - 50f) / 50f) * 17f
        }

        val touchPenalty = touches * 12
        val fidgetPenalty = if (fidget > 0.30f) (fidget * 35f).toInt() else 0

        val score = (baseScore.roundToInt() - touchPenalty - fidgetPenalty).coerceIn(15, 100)

        val observation = when {
            touches >= 2 -> "Frequent hand-to-face touches detected ($touches times), suggesting physical restlessness."
            fidget > 0.35f -> "Noticeable hand fidgeting or rapid hand movements detected."
            v in 20f..60f -> "Purposeful, controlled hand gestures with good visibility (${v.toInt()}%)."
            else -> "Hands mostly rested out of camera view."
        }

        val tip = when {
            touches > 0 -> "Keep hands away from your face, chin, or hair to project composure."
            fidget > 0.30f -> "Rest your hands calmly on the desk or in your lap between gestures."
            else -> "Use open-palm hand gestures at chest level to reinforce key achievements."
        }

        return MetricEvaluation(score = score, observation = observation, tip = tip)
    }

    private fun buildSummary(
        overallScore: Int,
        confidenceBand: ConfidenceBand,
        metrics: BodyLanguageMetrics,
    ): String {
        return when (confidenceBand) {
            ConfidenceBand.HIGH -> "Demonstrated strong, confident presence with balanced eye contact (${metrics.eyeContactPercentage.toInt()}%) and solid posture."
            ConfidenceBand.MODERATE -> "Good non-verbal communication with opportunities to improve posture consistency and reduce look-aways."
            ConfidenceBand.LOW -> "Body language indicators suggest low posture or gaze stability. Focus on camera alignment, upright posture, and reducing fidgeting."
        }
    }
}
