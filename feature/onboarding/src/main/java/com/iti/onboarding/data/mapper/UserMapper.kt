package com.iti.onboarding.data.mapper

import com.iti.careerpilot.core.network.model.UserProfileDto
import com.iti.core.datastore.models.AccountInfo
import com.iti.core.datastore.models.AvatarInfo
import com.iti.core.datastore.models.CareerInfo
import com.iti.core.datastore.models.PersonalInfo
import com.iti.core.datastore.models.UserProfile

fun UserProfileDto.toDomain(): UserProfile {
    return UserProfile(
        id = id ?: 0,
        account = AccountInfo(
            username = username.orEmpty(),
            email = email.orEmpty(),
            timezone = timezone.orEmpty(),
            termsAccepted = termsAccepted ?: false,
            subscriptionTier = subscriptionTier.orEmpty(),
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
            trackId = trackId,
        ),
        avatar = AvatarInfo(
            avatarUrl = avatarUrl.orEmpty(),
        ),
        cv = com.iti.core.datastore.models.CvInfo(
            cvUrl = cvUrl.orEmpty(),
        ),
        onboardingCompleted = onboardingCompleted,
    )
}
