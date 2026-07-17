package com.iti.common.snackbar.model

import kotlinx.coroutines.CompletableDeferred

class CareerPilotSnackbarRequest internal constructor(
    val event: CareerPilotSnackbarEvent,
    private val result: CompletableDeferred<CareerPilotSnackbarResult>,
) {
    fun complete(value: CareerPilotSnackbarResult) {
        result.complete(value)
    }
}
