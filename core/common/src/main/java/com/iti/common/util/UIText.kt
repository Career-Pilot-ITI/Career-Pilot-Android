package com.iti.common.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UIText {

    class StringResource(
        @field:StringRes val resId: Int,
        vararg val args: Any,
    ) : UIText

    @Composable
    fun asString(): String {
        return when (this) {
            is StringResource -> stringResource(resId, *args)
        }
    }

    fun asString(context: Context): String {
        return when (this) {
            is StringResource -> context.getString(resId, *args)
        }
    }
}
