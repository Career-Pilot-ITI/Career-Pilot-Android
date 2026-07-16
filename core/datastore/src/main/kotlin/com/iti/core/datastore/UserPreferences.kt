package com.iti.core.datastore

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val hasCompletedOnboarding: Boolean = false,
)
