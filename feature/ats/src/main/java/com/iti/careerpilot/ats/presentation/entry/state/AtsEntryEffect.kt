package com.iti.careerpilot.ats.presentation.entry.state

import com.iti.common.util.UIText

sealed interface AtsEntryEffect {
    data object OpenPdfPicker : AtsEntryEffect
    data class NavigateToScore(val workspaceId: Long) : AtsEntryEffect
    data class ShowMessage(val message: UIText) : AtsEntryEffect
}
