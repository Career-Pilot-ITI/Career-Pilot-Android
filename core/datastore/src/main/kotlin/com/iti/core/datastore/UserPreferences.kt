package com.iti.core.datastore

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val hasCompletedOnboarding: Boolean = false,
    val token: String? = null,
    val avatarUrl: String? = null,
    val pdfUrl: String? = null,
)
