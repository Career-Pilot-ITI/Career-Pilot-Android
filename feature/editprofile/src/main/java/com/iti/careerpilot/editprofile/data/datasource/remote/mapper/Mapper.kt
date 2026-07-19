package com.iti.careerpilot.editprofile.data.datasource.remote.mapper


import com.iti.careerpilot.editprofile.data.datasource.remote.models.UpdateProfileRequestDto
import com.iti.careerpilot.editprofile.data.datasource.remote.models.UserProfileDto
import com.iti.careerpilot.editprofile.domain.models.RequestProfileUpdate
import com.iti.core.datastore.models.AccountInfo
import com.iti.core.datastore.models.AvatarInfo
import com.iti.core.datastore.models.CareerInfo
import com.iti.core.datastore.models.CvInfo
import com.iti.core.datastore.models.PersonalInfo
import com.iti.core.datastore.models.UserProfile

fun UserProfileDto.toDomain(
    id: Int? = null,
    phoneNumber: String? = null,
): UserProfile {
    return UserProfile(
        id = id ?: 0,
        account = AccountInfo(
            username = username.orEmpty(),
            email = email.orEmpty(),
            timezone = timezone.orEmpty(),
            termsAccepted = termsAccepted ?: false,
            subscriptionTier = subscriptionTier.orEmpty(),
            coinBalance = coinBalance ?: 0,
            onboardingCompleted = onboardingCompleted ?: false,
        ),
        personal = PersonalInfo(
            phoneNumber = phoneNumber.orEmpty(),
            displayName = displayName.orEmpty(),
            gender = gender.orEmpty(),
            dateOfBirth = dateOfBirth.orEmpty(),
        ),
        career = CareerInfo(
            targetRole = targetRole.orEmpty(),
            industry = industry.orEmpty(),
            experienceLevel = experienceLevel.orEmpty(),
            currentJobTitle = currentJobTitle.orEmpty(),
            yearsOfExperience = yearsOfExperience ?: 0,
            skills = skills?.filterNotNull() ?: emptyList(),
            targetCompanies = targetCompanies?.filterNotNull() ?: emptyList(),
            educationLevel = educationLevel.orEmpty(),
            trackName = trackName.orEmpty(),
        ),
        avatar = AvatarInfo(
            avatarUrl = avatarUrl.orEmpty(),
        ),
        cv = CvInfo(
            cvUrl = cvUrl.orEmpty(),
        )
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
    )
}
