package com.iti.careerpilot.rootnavigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {

    @Serializable
    data object Login : Route

    @Serializable
    data object OTP : Route

    @Serializable
    data object Register : Route

    @Serializable
    data object NestedNav : Route {

        @Serializable
        data object Home : Route

        @Serializable
        data object Reports : Route


        @Serializable
        data object Profile : Route

    }

    @Serializable
    data class SessionDetails(val id: String) : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object Paywall : Route

    @Serializable
    data object ProfileInfo : Route
}