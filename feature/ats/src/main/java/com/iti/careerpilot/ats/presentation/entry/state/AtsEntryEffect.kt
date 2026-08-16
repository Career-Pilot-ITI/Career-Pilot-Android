package com.iti.careerpilot.ats.presentation.entry.state

import com.iti.common.util.UIText

sealed interface AtsEntryEffect {
    data object NavigateToEditProfile : AtsEntryEffect
    data class NavigateToJobDetails(val workspaceId: Long) : AtsEntryEffect
    data class ShowMessage(val message: UIText) : AtsEntryEffect
}
