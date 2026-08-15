package com.iti.careerpilot.ats.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JobDto(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String? = null,
    @SerialName("companyName") val companyName: String? = null,
    @SerialName("location") val location: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("employmentType") val employmentType: String? = null,
    @SerialName("seniorityLevel") val seniorityLevel: String? = null,
    @SerialName("requiredSkills") val requiredSkills: List<String> = emptyList(),
    @SerialName("preferredSkills") val preferredSkills: List<String> = emptyList(),
    @Serializable(with = FlexibleStringListSerializer::class)
    @SerialName("responsibilities") val responsibilities: List<String> = emptyList(),
    @Serializable(with = FlexibleStringListSerializer::class)
    @SerialName("qualifications") val qualifications: List<String> = emptyList(),
    @SerialName("technologies") val technologies: List<String> = emptyList(),
    @SerialName("salaryMin") val salaryMin: Int? = null,
    @SerialName("salaryMax") val salaryMax: Int? = null,
    @SerialName("currency") val currency: String? = null,
    @SerialName("experienceYears") val experienceYears: Int? = null,
    @SerialName("educationLevel") val educationLevel: String? = null,
    @SerialName("applicationUrl") val applicationUrl: String? = null,
    @SerialName("sourceUrl") val sourceUrl: String? = null,
    @SerialName("sourceType") val sourceType: String? = null,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("updatedAt") val updatedAt: String? = null,
    @SerialName("companyLogoUrl") val companyLogoUrl: String? = null,
    @SerialName("postedLabel") val postedLabel: String? = null,
    @SerialName("applicantsLabel") val applicantsLabel: String? = null,
)
