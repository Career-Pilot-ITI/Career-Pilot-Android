package com.iti.careerpilot.editprofile.domain.models

data class RequestProfileUpdate(
    val username: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val avatarFileId: Long? = null,
    val gender: String? = null,
    val dateOfBirth: String? = null,
    val targetRole: String? = null,
    val industry: String? = null,
    val experienceLevel: String? = null,
    val currentJobTitle: String? = null,
    val yearsOfExperience: Int? = null,
    val cvFileId: Long? = null,
    val skills: List<String>? = null,
    val targetCompanies: List<String>? = null,
    val educationLevel: String? = null,
    val trackName: String? = null,
    val trackId: Long? = null,
    val timezone: String? = null,
)
