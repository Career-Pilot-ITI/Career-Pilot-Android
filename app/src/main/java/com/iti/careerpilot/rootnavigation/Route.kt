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
    data class SessionDetails(val id: String) : Route

    @Serializable
    data class QuestionBreakdown(val sessionId: String) : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data class EditProfile(val section: ProfileEditSection = ProfileEditSection.ALL) : Route

    @Serializable
    data object Paywall : Route

    @Serializable
    data object Onboarding: Route
}