package com.iti.common.snackbar

import androidx.compose.material3.SnackbarDuration
import com.iti.common.R
import com.iti.common.snackbar.model.CareerPilotSnackbarEvent
import com.iti.common.snackbar.model.CareerPilotSnackbarRequest
import com.iti.common.snackbar.model.CareerPilotSnackbarType
import com.iti.common.util.UIText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

object CareerPilotSnackbarController : SnackbarController {

    private val requestChannel = Channel<CareerPilotSnackbarRequest>(
        capacity = Channel.CONFLATED,
    )

    override val requests: Flow<CareerPilotSnackbarRequest> =
        requestChannel.receiveAsFlow()

    override fun show(
        message: UIText,
        type: CareerPilotSnackbarType,
        duration: SnackbarDuration,
        onAction: (() -> Unit)?,
    ) {
        val actionLabel = when (type) {
            CareerPilotSnackbarType.UNDO ->
                UIText.StringResource(R.string.snackbar_action_undo)

            CareerPilotSnackbarType.NORMAL,
            CareerPilotSnackbarType.DISMISSIBLE -> null
        }

        send(
            event = CareerPilotSnackbarEvent(
                message = message,
                type = type,
                duration = duration,
                actionLabel = actionLabel,
            ),
            onAction = onAction,
        )
    }

    override fun show(
        message: UIText,
        actionLabel: UIText,
        type: CareerPilotSnackbarType,
        duration: SnackbarDuration,
        onAction: () -> Unit,
    ) {
        send(
            event = CareerPilotSnackbarEvent(
                message = message,
                type = type,
                duration = duration,
                actionLabel = actionLabel,
            ),
            onAction = onAction,
        )
    }

    private fun send(
        event: CareerPilotSnackbarEvent,
        onAction: (() -> Unit)?,
    ) {
        val result = requestChannel.trySend(
            CareerPilotSnackbarRequest(event, onAction),
        )

        check(result.isSuccess) {
            "Snackbar request could not be sent"
        }
    }
}
