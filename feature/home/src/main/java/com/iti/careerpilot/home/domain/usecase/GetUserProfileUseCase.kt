package com.iti.careerpilot.home.domain.usecase

import com.iti.core.datastore.models.UserProfile
import com.iti.core.datastore.repo.UserProfileRepo
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userProfileRepo: UserProfileRepo,
) {
    operator fun invoke(): StateFlow<UserProfile> = userProfileRepo.userProfile
}
