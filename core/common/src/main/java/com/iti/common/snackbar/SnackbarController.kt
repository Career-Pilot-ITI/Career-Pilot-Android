package com.iti.common.snackbar

import com.iti.common.snackbar.model.CareerPilotSnackbarEvent
import com.iti.common.snackbar.model.CareerPilotSnackbarRequest
import com.iti.common.snackbar.model.CareerPilotSnackbarResult
import kotlinx.coroutines.flow.Flow

interface SnackbarController {
    val requests: Flow<CareerPilotSnackbarRequest>

    suspend fun show(
        event: CareerPilotSnackbarEvent,
    ): CareerPilotSnackbarResult
}
