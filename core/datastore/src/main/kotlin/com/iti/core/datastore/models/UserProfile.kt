package com.iti.core.datastore.models

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: Int = 0,
    val account: AccountInfo = AccountInfo(),
    val personal: PersonalInfo = PersonalInfo(),
    val career: CareerInfo = CareerInfo(),
    val avatar: AvatarInfo = AvatarInfo(),
    val cv: CvInfo = CvInfo()
)

@Serializable
data class AccountInfo(
    val username: String = "",
    val email: String = "",
    val timezone: String = "",
    val termsAccepted: Boolean = false,
    val subscriptionTier: String = "",
    val coinBalance: Int = 0,
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
    val trackName: String = ""
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
