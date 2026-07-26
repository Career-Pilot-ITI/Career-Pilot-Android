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

    data class DynamicString(val value: String) : UIText

    @Composable
    fun asString(): String {
        return when (this) {
            is StringResource -> stringResource(resId, *args)
            is DynamicString -> value
        }
    }

    fun asString(context: Context): String {
        return when (this) {
            is StringResource -> context.getString(resId, *args)
            is DynamicString -> value
        }
    }
}
