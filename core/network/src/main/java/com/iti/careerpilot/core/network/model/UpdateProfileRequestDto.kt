package com.iti.careerpilot.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequestDto(
    val displayName: String? = null,
    val targetRole: String? = null,
    val yearsOfExperience: Int? = null,
    val skills: List<String>? = null,
    val avatarFileId: Long? = null,
    val username: String? = null,
    val email: String? = null,
    val currentJobTitle: String? = null,
    val industry: String? = null,
    val experienceLevel: String? = null,
    val educationLevel: String? = null,
    val targetCompanies: List<String>? = null,
    val timezone: String? = null,
    val cvFileId: Long? = null
)
