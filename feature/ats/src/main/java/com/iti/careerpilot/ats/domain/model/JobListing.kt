package com.iti.careerpilot.ats.domain.model

import kotlinx.collections.immutable.ImmutableList

data class JobListing(
    val id: Long,
    val title: String,
    val companyName: String,
    val location: String,
    val description: String,
    val employmentType: String?,
    val seniorityLevel: String?,
    val requiredSkills: ImmutableList<String>,
    val preferredSkills: ImmutableList<String>,
    val responsibilities: ImmutableList<String>,
    val qualifications: ImmutableList<String>,
    val technologies: ImmutableList<String>,
    val salaryMin: Int?,
    val salaryMax: Int?,
    val currency: String?,
    val experienceYears: Int?,
    val educationLevel: String?,
    val applicationUrl: String?,
    val sourceUrl: String?,
    val sourceType: String?,
    val companyLogoUrl: String? = null,
    val postedLabel: String? = null,
    val applicantsLabel: String? = null,
)
