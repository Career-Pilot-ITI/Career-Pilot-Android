package com.iti.careerpilot.home.presentation.ready

import com.iti.common.util.UIText

data class ReadyToPracticeState(
    val trackName: String = "",
    val isMicrophoneGranted: Boolean = false,
    val isPermissionDialogVisible: Boolean = false,
    val isStarting: Boolean = false,
    val error: UIText? = null,
) {
    val canBegin: Boolean get() = isMicrophoneGranted && !isStarting
}