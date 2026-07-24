package com.iti.common.snackbar.model

class CareerPilotSnackbarRequest internal constructor(
    val event: CareerPilotSnackbarEvent,
    private val onAction: (() -> Unit)?,
) {
    fun performAction() {
        onAction?.invoke()
    }
}
