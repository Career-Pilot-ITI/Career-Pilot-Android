package com.iti.careerpilot.ats.domain.util

import com.iti.careerpilot.ats.domain.model.JobListing

data class JobDisplayMetadata(
    val experienceYears: Int?,
    val workArrangement: WorkArrangement?,
)

enum class WorkArrangement { REMOTE, HYBRID, ON_SITE }

object JobDisplayMetadataResolver {
    fun resolve(job: JobListing): JobDisplayMetadata = JobDisplayMetadata(
        experienceYears = job.experienceYears ?: EXPERIENCE_REGEX
            .find(job.description)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull(),
        workArrangement = resolveArrangement(job),
    )

    private fun resolveArrangement(job: JobListing): WorkArrangement? {
        val explicitText = listOfNotNull(job.location, job.employmentType, job.description)
            .joinToString(" ")
            .lowercase()
        return when {
            HYBRID_REGEX.containsMatchIn(explicitText) -> WorkArrangement.HYBRID
            REMOTE_REGEX.containsMatchIn(explicitText) -> WorkArrangement.REMOTE
            ON_SITE_REGEX.containsMatchIn(explicitText) -> WorkArrangement.ON_SITE
            else -> null
        }
    }

    private val EXPERIENCE_REGEX = Regex("(?i)\\b(\\d{1,2})\\+?\\s+years?\\b")
    private val HYBRID_REGEX = Regex("\\bhybrid\\b")
    private val REMOTE_REGEX = Regex("\\b(remote|work from home)\\b")
    private val ON_SITE_REGEX = Regex("\\b(on[- ]site|in office)\\b")
}
