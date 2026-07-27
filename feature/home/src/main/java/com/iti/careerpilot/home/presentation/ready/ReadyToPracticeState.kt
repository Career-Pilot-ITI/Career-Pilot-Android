package com.iti.careerpilot.home.presentation.ready

data class ReadyToPracticeState(
    val trackName: String = "",
    val isMicrophoneGranted: Boolean = false,
    val isPermissionDialogVisible: Boolean = false,
) {
    val canBegin: Boolean get() = isMicrophoneGranted
}