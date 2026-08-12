package com.iti.careerpilot.ats.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

internal sealed interface AtsRoute : NavKey {
    @Serializable data object Entry : AtsRoute
    @Serializable data class Score(val workspaceId: Long) : AtsRoute
    @Serializable data class CoverLetter(val workspaceId: Long) : AtsRoute
    @Serializable data class OptimizedCv(val workspaceId: Long) : AtsRoute
}
