package com.iti.careerpilot.reports.presentation.screen.history.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.iti.careerpilot.core.interviews.domain.model.InterviewSession
import com.iti.careerpilot.reports.domain.usecase.GetSessionHistoryUseCase
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import kotlin.coroutines.cancellation.CancellationException

class SessionHistoryPagingSource(
    private val getSessionHistory: GetSessionHistoryUseCase,
) : PagingSource<Int, InterviewSession>() {

    override suspend fun load(
        params: LoadParams<Int>,
    ): LoadResult<Int, InterviewSession> {
        val pageNumber = params.key ?: INITIAL_PAGE
        return try {
            when (
                val result = getSessionHistory(
                    page = pageNumber,
                    size = params.loadSize,
                )
            ) {
                is CareerPilotResult.Error -> LoadResult.Error(
                    SessionHistoryPagingException(result.error),
                )

                is CareerPilotResult.Success -> {
                    val page = result.data
                    LoadResult.Page(
                        data = page.sessions,
                        prevKey = if (page.isFirst) null else page.pageNumber - 1,
                        nextKey = if (page.isLast) null else page.pageNumber + 1,
                    )
                }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        }
    }

    override fun getRefreshKey(
        state: PagingState<Int, InterviewSession>,
    ): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null
        return anchorPage.prevKey?.plus(1) ?: anchorPage.nextKey?.minus(1)
    }

    private companion object {
        const val INITIAL_PAGE = 0
    }
}

class SessionHistoryPagingException(
    val error: NetworkError,
) : Exception()
