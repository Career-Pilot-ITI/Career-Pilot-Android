package com.iti.common.snackbar.model

import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.CompletableDeferred

class CareerPilotSnackbarRequest internal constructor(
    val event: CareerPilotSnackbarEvent,
    private val result: CompletableDeferred<SnackbarResult>,
) {
    fun complete(value: SnackbarResult) {
        result.complete(value)
    }
}
