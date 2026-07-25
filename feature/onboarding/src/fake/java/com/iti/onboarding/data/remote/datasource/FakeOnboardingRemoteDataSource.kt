package com.iti.onboarding.data.remote.datasource

import com.iti.careerpilot.core.network.model.SkillDto
import com.iti.careerpilot.core.network.model.UpdateProfileRequestDto
import com.iti.careerpilot.core.network.model.UserProfileDto
import com.iti.onboarding.data.remote.dto.TracksResponseDto
import com.iti.onboarding.data.remote.dto.UploadFileResponseDto
import com.iti.common.util.fakeDelay
import com.iti.common.util.shouldFail
import java.io.File
import javax.inject.Inject

import com.iti.onboarding.presentation.screen.profileinfo.model.ExperienceLevel
import kotlinx.coroutines.delay

class FakeOnboardingRemoteDataSource @Inject constructor() : OnboardingRemoteDataSource {
    override suspend fun uploadFile(file: File, onProgress: (Int) -> Unit): UploadFileResponseDto {
        fakeDelay()
        if (shouldFail()) throw Exception("Fake network error")
        onProgress(100)
        return UploadFileResponseDto(
            id = 1L,
            type = "IMAGE",
            originalName = file.name,
            url = "https://fake.url/avatar.png",
            sizeBytes = file.length(),
            createdAt = "2023-10-01T00:00:00Z"
        )
    }

    override suspend fun updateProfile(request: UpdateProfileRequestDto): UserProfileDto {
        fakeDelay()
        return UserProfileDto(
            id = 1L,
            displayName = request.displayName,
            email = request.email,
            targetRole = request.targetRole,
            yearsOfExperience = request.yearsOfExperience,
            skills = request.skills?.map { SkillDto(skillName = it) },
            onboardingCompleted = request.onboardingCompleted ?: true
        )
    }

    override suspend fun getTracks(): List<TracksResponseDto> {
        fakeDelay()
        if (shouldFail()) throw Exception("Fake network error")
        return listOf(
            TracksResponseDto(1L, "Android", "Android Development", true, "2023-01-01"),
            TracksResponseDto(2L, "iOS", "iOS Development", true, "2023-01-01"),
            TracksResponseDto(3L, "Backend", "Backend Development", true, "2023-01-01")
        )
    }

    override suspend fun uploadCv(file: File, onProgress: (Int) -> Unit): UploadFileResponseDto {
        fakeDelay()
        if (shouldFail()) throw Exception("Fake network error")
        onProgress(100)
        return UploadFileResponseDto(
            id = 2L,
            type = "PDF",
            originalName = file.name,
            url = "https://fake.url/cv.pdf",
            sizeBytes = file.length(),
            createdAt = "2023-10-01T00:00:00Z"
        )
    }

    override suspend fun analyzeCv(
        file: File,
        onProgress: (Int) -> Unit,
    ): UserProfileDto {
        if (shouldFail()) {
            throw Exception("Fake network error")
        }

        val progressUpdates = listOf(
            0,
            5,
            12,
            24,
            38,
            51,
            63,
            74,
            86,
            94,
            100,
        )

        progressUpdates.forEach { progress ->
            delay(250)
            onProgress(progress)
        }

        return UserProfileDto(
            id = 1L,
            displayName = "Fake User",
            email = "fake@example.com",
            currentJobTitle = "Android Developer",
            experienceLevel = ExperienceLevel.MID_LEVEL.apiKey,
            yearsOfExperience = 3,
            skills = listOf(
                SkillDto(skillName = "Kotlin"),
                SkillDto(skillName = "Android"),
            ),
            trackName = "Android",
        )
    }
}
