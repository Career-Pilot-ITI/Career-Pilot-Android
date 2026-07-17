package com.iti.careerpilot.editprofile.data.datasource.remote.mapper


import com.iti.careerpilot.editprofile.data.datasource.remote.models.UpdateProfileRequestDto
import com.iti.careerpilot.editprofile.data.datasource.remote.models.UserProfileDto
import com.iti.careerpilot.editprofile.domain.models.RequestProfileUpdate
import com.iti.core.datastore.models.UserProfile

fun UserProfileDto.toDomain(
    id: Int? = null,
    phoneNumber: String? = null,
): UserProfile {
    return UserProfile(
        id = id ?: 0,
        phoneNumber = phoneNumber.orEmpty(),
        displayName = displayName.orEmpty(),
        username = username.orEmpty(),
        email = email.orEmpty(),
        avatarUrl = avatarUrl.orEmpty(),
        gender = gender.orEmpty(),
        dateOfBirth = dateOfBirth.orEmpty(),
        targetRole = targetRole.orEmpty(),
        industry = industry.orEmpty(),
        experienceLevel = experienceLevel.orEmpty(),
        currentJobTitle = currentJobTitle.orEmpty(),
        yearsOfExperience = yearsOfExperience ?: 0,
        cvUrl = cvUrl.orEmpty(),
        skills = skills.orEmpty(),
        targetCompanies = targetCompanies.orEmpty(),
        educationLevel = educationLevel.orEmpty(),
        timezone = timezone.orEmpty(),
        termsAccepted = termsAccepted ?: false,
        subscriptionTier = subscriptionTier.orEmpty(),
        coinBalance = coinBalance ?: 0,
        onboardingCompleted = onboardingCompleted ?: false,
        trackName = trackName.orEmpty()
    )
}

fun RequestProfileUpdate.toDto(): UpdateProfileRequestDto {
    return UpdateProfileRequestDto(
        username = username,
        email = email,
        displayName = displayName,
        gender = gender,
        dateOfBirth = dateOfBirth,
        targetRole = targetRole,
        industry = industry,
        experienceLevel = experienceLevel,
        currentJobTitle = currentJobTitle,
        yearsOfExperience = yearsOfExperience,
        skills = skills,
        targetCompanies = targetCompanies,
        educationLevel = educationLevel,
        timezone = timezone,
        termsAccepted = termsAccepted,
        subscriptionTier = subscriptionTier,
        trackId = trackId
    )
}