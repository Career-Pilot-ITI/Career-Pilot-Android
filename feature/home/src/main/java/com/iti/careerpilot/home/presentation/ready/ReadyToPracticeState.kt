package com.iti.careerpilot.home.presentation.ready

import com.iti.careerpilot.core.access.FeaturePricingMap
import com.iti.core.model.FeatureAccess
import com.iti.core.model.FeatureKey
import com.iti.core.model.Plan

data class ReadyToPracticeState(
    val trackName: String = "",
    val isMicrophoneGranted: Boolean = false,
    val isPermissionDialogVisible: Boolean = false,
    val isVideoMode: Boolean = false,
    val enablePostureTracking: Boolean = false,
    val enableHandTracking: Boolean = false,
    val isPaidPlan: Boolean = false,
    val isCameraGranted: Boolean = false,
    val showCameraPermissionDialog: Boolean = false,
    // Pricing & Access:
    val voiceCoinCost: Int = FeaturePricingMap.coinCost(FeatureKey.VoicePracticeMode),
    val videoCoinCost: Int = FeaturePricingMap.coinCost(FeatureKey.VideoInterview),
    val videoInterviewAccess: FeatureAccess = FeatureAccess.Unknown,
    val audioInterviewAccess: FeatureAccess = FeatureAccess.Unknown,
    val coinBalance: Int = 0,
    val planDisplayName: String = "Free",
    val showVideoGateSheet: Boolean = false,
    val videoGatePlanFeatures: List<String> = emptyList(),
    val videoGateRequiredPlan: Plan = Plan.MAX,
    val showCoinTopUpSheet: Boolean = false,
    val coinTopUpRequiredCost: Int = 0,
) {
    val canBegin: Boolean get() = isMicrophoneGranted && (!isVideoMode || isCameraGranted)
    val isMaxPlan: Boolean get() = planDisplayName.equals("Max", ignoreCase = true)
}