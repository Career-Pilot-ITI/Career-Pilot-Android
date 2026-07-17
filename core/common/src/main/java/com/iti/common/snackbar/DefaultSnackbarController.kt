package com.iti.common.snackbar

import com.iti.common.snackbar.model.CareerPilotSnackbarEvent
import com.iti.common.snackbar.model.CareerPilotSnackbarRequest
import com.iti.common.snackbar.model.CareerPilotSnackbarResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

internal class DefaultSnackbarController @Inject constructor() : SnackbarController {

    private val requestChannel = Channel<CareerPilotSnackbarRequest>(
        capacity = Channel.BUFFERED,
    )

    override val requests: Flow<CareerPilotSnackbarRequest> =
        requestChannel.receiveAsFlow()

    override suspend fun show(
        event: CareerPilotSnackbarEvent,
    ): CareerPilotSnackbarResult {
        val result = CompletableDeferred<CareerPilotSnackbarResult>()

        requestChannel.send(
            CareerPilotSnackbarRequest(
                event = event,
                result = result,
            ),
        )

        return result.await()
    }
}
