package com.iti.careerpilot.ats.domain.model

data class JobListing(
    val id: Long,
    val title: String,
    val companyName: String,
    val location: String,
    val description: String,
    val employmentType: String?,
    val seniorityLevel: String?,
    val requiredSkills: List<String>,
    val preferredSkills: List<String>,
    val responsibilities: List<String>,
    val qualifications: List<String>,
    val technologies: List<String>,
    val salaryMin: Int?,
    val salaryMax: Int?,
    val currency: String?,
    val experienceYears: Int?,
    val educationLevel: String?,
    val applicationUrl: String?,
    val sourceUrl: String?,
    val sourceType: String?,
)
