package com.iti.careerpilot.reports.data.datasource.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InterviewSessionsPageDto(
    @SerialName("totalElements")
    val totalElements: Long,
    @SerialName("totalPages")
    val totalPages: Int,
    @SerialName("pageable")
    val pageable: PageableDto,
    @SerialName("last")
    val last: Boolean,
    @SerialName("first")
    val first: Boolean,
    @SerialName("numberOfElements")
    val numberOfElements: Int,
    @SerialName("size")
    val size: Int,
    @SerialName("content")
    val content: List<InterviewSessionDto>,
    @SerialName("number")
    val number: Int,
    @SerialName("sort")
    val sort: SortDto,
    @SerialName("empty")
    val empty: Boolean,
)

@Serializable
data class PageableDto(
    @SerialName("unpaged")
    val unpaged: Boolean,
    @SerialName("paged")
    val paged: Boolean,
    @SerialName("pageNumber")
    val pageNumber: Int,
    @SerialName("pageSize")
    val pageSize: Int,
    @SerialName("offset")
    val offset: Long,
    @SerialName("sort")
    val sort: SortDto,
)

@Serializable
data class SortDto(
    @SerialName("unsorted")
    val unsorted: Boolean,
    @SerialName("sorted")
    val sorted: Boolean,
    @SerialName("empty")
    val empty: Boolean,
)
