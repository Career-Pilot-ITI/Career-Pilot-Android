package com.iti.common.util
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource


sealed class UIText {
    class StringResource(val resId: Int, vararg val args: Any) : UIText()

    @Composable
    fun asString(): String {
        return when (this) {
            is StringResource -> stringResource(resId, args)
        }
    }
}