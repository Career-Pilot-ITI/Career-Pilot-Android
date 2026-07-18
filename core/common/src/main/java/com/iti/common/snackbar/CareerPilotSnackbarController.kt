package com.iti.common.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import com.iti.common.R
import com.iti.common.snackbar.model.CareerPilotSnackbarEvent
import com.iti.common.snackbar.model.CareerPilotSnackbarRequest
import com.iti.common.snackbar.model.CareerPilotSnackbarType
import com.iti.common.util.UIText
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

object CareerPilotSnackbarController : SnackbarController {

    private val requestChannel = Channel<CareerPilotSnackbarRequest>(
        capacity = Channel.BUFFERED,
    )

    override val requests: Flow<CareerPilotSnackbarRequest> =
        requestChannel.receiveAsFlow()

    override suspend fun show(
        message: UIText,
        type: CareerPilotSnackbarType,
        duration: SnackbarDuration,
    ): SnackbarResult {
        val actionLabel = when (type) {
            CareerPilotSnackbarType.UNDO ->
                UIText.StringResource(R.string.snackbar_action_undo)

            CareerPilotSnackbarType.NORMAL,
            CareerPilotSnackbarType.DISMISSIBLE -> null
        }

        return show(
            event = CareerPilotSnackbarEvent(
                message = message,
                type = type,
                duration = duration,
                actionLabel = actionLabel
            ),
        )
    }

    private suspend fun show(
        event: CareerPilotSnackbarEvent,
    ): SnackbarResult {
        val result = CompletableDeferred<SnackbarResult>()

        requestChannel.send(
            CareerPilotSnackbarRequest(
                event = event,
                result = result,
            ),
        )

        return result.await()
    }
}
