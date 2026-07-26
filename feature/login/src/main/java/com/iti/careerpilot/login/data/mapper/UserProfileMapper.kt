package com.iti.careerpilot.login.data.mapper

import com.iti.careerpilot.login.data.remote.dto.OtpAuthResponse
import com.iti.core.datastore.models.AccountInfo
import com.iti.core.datastore.models.AvatarInfo
import com.iti.core.datastore.models.CareerInfo
import com.iti.core.datastore.models.CvInfo
import com.iti.core.datastore.models.PersonalInfo
import com.iti.core.datastore.models.UserProfile

fun OtpAuthResponse.toUserProfile(): UserProfile =
    UserProfile(
        id = user.id,
        account = AccountInfo(
            username = user.profile?.username.orEmpty(),
            email = user.profile?.email.orEmpty(),
            timezone = user.profile?.timezone.orEmpty(),
            termsAccepted = user.profile?.termsAccepted ?: false,
        ),
        personal = PersonalInfo(
            phoneNumber = user.phoneNumber.orEmpty(),
            displayName = user.profile?.displayName.orEmpty(),
            gender = user.profile?.gender.orEmpty(),
            dateOfBirth = user.profile?.dateOfBirth.orEmpty()
        ),
        career = CareerInfo(
            targetRole = user.profile?.targetRole.orEmpty(),
            industry = user.profile?.industry.orEmpty(),
            experienceLevel = user.profile?.experienceLevel.orEmpty(),
            currentJobTitle = user.profile?.currentJobTitle.orEmpty(),
            yearsOfExperience = user.profile?.yearsOfExperience ?: 0,
            skills = user.profile?.skills?.mapNotNull { it.skillName } ?: emptyList(),
            targetCompanies = user.profile?.targetCompanies?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
            educationLevel = user.profile?.educationLevel.orEmpty(),
            trackName = user.profile?.trackName.orEmpty()
        ),
        avatar = AvatarInfo(
            avatarUrl = user.profile?.avatarUrl.orEmpty()
        ),
        cv = CvInfo(
            cvUrl = user.profile?.cvUrl.orEmpty()
        ),
        onboardingCompleted = user.profile?.onboardingCompleted,
    )
