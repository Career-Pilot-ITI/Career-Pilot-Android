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

fun UserProfileDto.toDomain(current: UserProfile? = null): UserProfile {
    val effectiveTier = if (current?.account?.subscriptionTier?.equals("FREE", ignoreCase = true) == true &&
        (subscriptionTier.equals("PLUS", ignoreCase = true) || subscriptionTier.equals("PRO", ignoreCase = true))) {
        "FREE"
    } else {
        subscriptionTier.orEmpty()
    }
    return UserProfile(
        id = id?.toInt() ?: current?.id ?: 0,
        account = AccountInfo(
            username = username.orEmpty(),
            email = email.orEmpty(),
            timezone = timezone.orEmpty(),
            termsAccepted = termsAccepted ?: false,
            subscriptionTier = effectiveTier,
            coinBalance = coinBalance ?: 0,
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
            skills = skills?.mapNotNull { it.skillName } ?: emptyList(),
            targetCompanies = targetCompanies ?: emptyList(),
            educationLevel = educationLevel.orEmpty(),
            trackName = trackName.orEmpty(),
        ),
        avatar = AvatarInfo(
            avatarUrl = avatarUrl.orEmpty(),
        ),
        cv = CvInfo(
            cvUrl = cvUrl.orEmpty(),
        ),
        onboardingCompleted = onboardingCompleted,
    )
}

fun RequestProfileUpdate.toDto(): UpdateProfileRequestDto {
    return UpdateProfileRequestDto(
        username = username,
        email = email,
        displayName = displayName,
        avatarFileId = avatarFileId,
        gender = gender,
        dateOfBirth = dateOfBirth,
        targetRole = targetRole,
        industry = industry,
        experienceLevel = experienceLevel,
        currentJobTitle = currentJobTitle,
        yearsOfExperience = yearsOfExperience,
        cvFileId = cvFileId,
        skills = skills,
        targetCompanies = targetCompanies,
        educationLevel = educationLevel,
        timezone = timezone,
    )
}
