package com.iti.core.datastore.models

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: Int = 0,
    val phoneNumber: String = "",
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val avatarLocalUri: String = "",
    val avatarSizeBytes: Long = 0,
    val gender: String = "",
    val dateOfBirth: String = "",
    val targetRole: String = "",
    val industry: String = "",
    val experienceLevel: String = "",
    val currentJobTitle: String = "",
    val yearsOfExperience: Int = 0,
    val cvUrl: String = "",
    val cvLocalUri: String = "",
    val cvFileName: String = "",
    val cvSizeBytes: Long = 0,
    val skills: List<String> = emptyList(),
    val targetCompanies: List<String> = emptyList(),
    val educationLevel: String = "",
    val timezone: String = "",
    val termsAccepted: Boolean = false,
    val subscriptionTier: String = "",
    val coinBalance: Int = 0,
    val onboardingCompleted: Boolean = false,
    val trackName: String = ""
)