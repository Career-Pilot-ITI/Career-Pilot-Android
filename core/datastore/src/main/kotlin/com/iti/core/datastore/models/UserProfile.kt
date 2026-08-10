package com.iti.core.datastore.models

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: Long = 0,
    val account: AccountInfo = AccountInfo(),
    val personal: PersonalInfo = PersonalInfo(),
    val career: CareerInfo = CareerInfo(),
    val avatar: AvatarInfo = AvatarInfo(),
    val cv: CvInfo = CvInfo(),
    val onboardingCompleted: Boolean? = null,
) {
    val hasCompletedOnboarding: Boolean
        get() = onboardingCompleted ?: personal.displayName.isNotBlank()
}

fun UserProfile.isPaidSubscriber(): Boolean =
    account.subscriptionTier.uppercase() in setOf("PLUS", "PRO", "MAX")

@Serializable
data class AccountInfo(
    val username: String = "",
    val email: String = "",
    val timezone: String = "",
    val termsAccepted: Boolean = false,
    val subscriptionTier: String = "",
    val coinBalance: Int = 0,
    val bodyLanguageConsentGiven: Boolean = false,
)

@Serializable
data class PersonalInfo(
    val phoneNumber: String = "",
    val displayName: String = "",
    val gender: String = "",
    val dateOfBirth: String = ""
)

@Serializable
data class CareerInfo(
    val targetRole: String = "",
    val industry: String = "",
    val experienceLevel: String = "",
    val currentJobTitle: String = "",
    val yearsOfExperience: Int = 0,
    val skills: List<String> = emptyList(),
    val targetCompanies: List<String> = emptyList(),
    val educationLevel: String = "",
    val trackName: String = "",
    val trackId: Long? = null,
)

@Serializable
data class AvatarInfo(
    val avatarUrl: String = "",
    val avatarLocalUri: String = "",
    val avatarSizeBytes: Long = 0
)

@Serializable
data class CvInfo(
    val cvUrl: String = "",
    val cvLocalUri: String = "",
    val cvFileName: String = "",
    val cvSizeBytes: Long = 0
)
