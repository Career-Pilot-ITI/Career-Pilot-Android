package com.iti.careerpilot.reports.data.datasource.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InterviewSessionsPageDto(
    @SerialName("content")
    val content: List<InterviewSessionDto> = emptyList(),
    @SerialName("pageable")
    val pageable: PageableDto? = null,
    @SerialName("last")
    val last: Boolean = true,
    @SerialName("totalPages")
    val totalPages: Int = 0,
    @SerialName("totalElements")
    val totalElements: Long = 0L,
    @SerialName("first")
    val first: Boolean = true,
    @SerialName("numberOfElements")
    val numberOfElements: Int = 0,
    @SerialName("size")
    val size: Int = 10,
    @SerialName("number")
    val number: Int = 0,
    @SerialName("sort")
    val sort: SortDto? = null,
    @SerialName("empty")
    val empty: Boolean = false,
)

@Serializable
data class PageableDto(
    @SerialName("pageNumber")
    val pageNumber: Int = 0,
    @SerialName("pageSize")
    val pageSize: Int = 10,
    @SerialName("sort")
    val sort: SortDto? = null,
    @SerialName("offset")
    val offset: Long = 0L,
    @SerialName("paged")
    val paged: Boolean = true,
    @SerialName("unpaged")
    val unpaged: Boolean = false,
)

@Serializable
data class SortDto(
    @SerialName("sorted")
    val sorted: Boolean = false,
    @SerialName("unsorted")
    val unsorted: Boolean = true,
    @SerialName("empty")
    val empty: Boolean = true,
)
