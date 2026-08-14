package com.iti.careerpilot.rootnavigation

import androidx.navigation3.runtime.NavKey
import com.iti.common.model.ProfileEditSection
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {

    @Serializable
    data object Splash : Route

    @Serializable
    data object Login : Route

    @Serializable
    data class OTP(val phoneNumber: String) : Route

    @Serializable
    data object NestedNav : Route {

        @Serializable
        data object Home : Route

        @Serializable
        data object SessionHistory : Route

        @Serializable
        data object Profile : Route

    }

    @Serializable
    data object Ats : Route

    @Serializable
    data class AtsJobDetails(val workspaceId: Long) : Route

    @Serializable
    data class AtsScore(val workspaceId: Long) : Route

    @Serializable
    data class AtsCoverLetter(val workspaceId: Long) : Route

    @Serializable
    data class AtsOptimizedCv(
        val workspaceId: Long,
        val jobId: Long,
    ) : Route

    @Serializable
    data class SessionDetails(val id: Long) : Route

    @Serializable
    data class PracticeSession(
        val trackId: Long,
        val sessionId: Long? = null,
        val workspaceId: Long? = null,
    ) : Route

    @Serializable
    data class PracticeResult(
        val sessionId: Long,
    ) : Route

    @Serializable
    data class QuestionBreakdown(val sessionId: Long) : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data class EditProfile(val section: ProfileEditSection = ProfileEditSection.ALL) : Route

    @Serializable
    data class Paywall(val showGetCoins: Boolean = false) : Route

    @Serializable
    data object Onboarding: Route

    @Serializable
    data object Interviews : Route

    @Serializable
    data class ReadyToPractice(
        val trackId: Long,
        val trackName: String,
        val workspaceId: Long? = null,
    ) : Route
}
