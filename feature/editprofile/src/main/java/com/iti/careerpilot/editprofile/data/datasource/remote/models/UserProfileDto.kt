package com.iti.careerpilot.editprofile.data.datasource.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    val displayName: String? = null,
    val username: String? = null,
    val email: String? = null,
    val avatarUrl: String? = null,
    val gender: String? = null,
    val dateOfBirth: String? = null,
    val targetRole: String? = null,
    val industry: String? = null,
    val experienceLevel: String? = null,
    val currentJobTitle: String? = null,
    val yearsOfExperience: Int? = null,
    val cvUrl: String? = null,
    val skills: List<String>? = null,
    val targetCompanies: List<String>? = null,
    val educationLevel: String? = null,
    val timezone: String? = null,
    val termsAccepted: Boolean? = null,
    val subscriptionTier: String? = null,
    val coinBalance: Int? = null,
    val onboardingCompleted: Boolean? = null,
    val trackName: String? = null
)