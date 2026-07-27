package com.iti.careerpilot.home.presentation.interviews

sealed interface InterviewsAction {
    data class QueryChanged(val query: String) : InterviewsAction
    data class TrackClicked(val trackId: Long, val trackName: String) : InterviewsAction
    data object Retry : InterviewsAction
}