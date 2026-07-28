package com.iti.onboarding.data.remote.datasource

import com.iti.careerpilot.core.network.model.SkillDto
import com.iti.careerpilot.core.network.model.UpdateProfileRequestDto
import com.iti.careerpilot.core.network.model.UserProfileDto
import com.iti.common.util.fakeDelay
import com.iti.common.util.shouldFail
import com.iti.onboarding.data.remote.dto.TracksResponseDto
import com.iti.onboarding.data.remote.dto.UploadFileResponseDto
import com.iti.onboarding.presentation.screen.profileinfo.model.ExperienceLevel
import java.io.File
import javax.inject.Inject

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
            TracksResponseDto(3L, "Backend", "Backend Development", true, "2023-01-01"),
            TracksResponseDto(4L, "Frontend", "Frontend Development", true, "2023-01-01"),
            TracksResponseDto(5L, "Java", "Java Development", true, "2023-01-01"),
            TracksResponseDto(6L, "C++", "C++ Development", true, "2023-01-01"),
            TracksResponseDto(7L, "Python", "Python Development", true, "2023-01-01"),
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
        onProgress(100)

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
