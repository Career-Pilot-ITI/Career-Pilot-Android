package com.iti.careerpilot.profile.data.datasource.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class RequestProfileUpdateDto(
    val username: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val gender: String? = null,
    val dateOfBirth: String? = null,
    val targetRole: String? = null,
    val industry: String? = null,
    val experienceLevel: String? = null,
    val currentJobTitle: String? = null,
    val yearsOfExperience: Int? = null,
    val skills: List<String>? = null,
    val targetCompanies: List<String>? = null,
    val educationLevel: String? = null,
    val timezone: String? = null,
    val termsAccepted: Boolean? = null,
    val subscriptionTier: String? = null,
    val trackId: Int? = null
)
