package com.iti.careerpilot.home.presentation.ready

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
) {
    val canBegin: Boolean get() = isMicrophoneGranted && (!isVideoMode || isCameraGranted)
}