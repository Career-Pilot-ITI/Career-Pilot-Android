package com.iti.onboarding.data.mapper

import com.iti.careerpilot.core.network.model.UserResponseDto
import com.iti.core.datastore.models.AccountInfo
import com.iti.core.datastore.models.AvatarInfo
import com.iti.core.datastore.models.CareerInfo
import com.iti.core.datastore.models.PersonalInfo
import com.iti.core.datastore.models.UserProfile

fun UserResponseDto.toDomain(): UserProfile {
    return UserProfile(
        id = id.toInt(),
        account = AccountInfo(
            username = profile?.username.orEmpty(),
            email = email ?: profile?.email.orEmpty(),
            timezone = profile?.timezone.orEmpty(),
        ),
        personal = PersonalInfo(
            phoneNumber = phoneNumber.orEmpty(),
            displayName = profile?.displayName.orEmpty(),
        ),
        career = CareerInfo(
            targetRole = profile?.targetRole.orEmpty(),
            industry = profile?.industry.orEmpty(),
            experienceLevel = profile?.experienceLevel.orEmpty(),
            currentJobTitle = profile?.currentJobTitle.orEmpty(),
            yearsOfExperience = profile?.yearsOfExperience ?: 0,
            skills = profile?.skills ?: emptyList(),
            targetCompanies = profile?.targetCompanies ?: emptyList(),
            educationLevel = profile?.educationLevel.orEmpty(),
        ),
        avatar = AvatarInfo(
            avatarUrl = profile?.avatarUrl.orEmpty(),
        )
    )
}
