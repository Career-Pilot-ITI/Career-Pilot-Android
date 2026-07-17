package com.iti.onboarding.presentation.screen.track.state

import com.iti.common.util.UIText

sealed interface ChoosingTracksEffects {
    data object NavigateNext: ChoosingTracksEffects
    data class ShowError(val message: UIText): ChoosingTracksEffects
}