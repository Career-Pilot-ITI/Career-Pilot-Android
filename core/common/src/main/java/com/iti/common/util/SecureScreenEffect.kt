package com.iti.common.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun SecureScreenEffect() {
    val context = LocalContext.current
    val activity = context.findActivity() ?: return
    val window = activity.window

    DisposableEffect(window) {

        val wasAlreadySecure =
            window.attributes.flags and
                    WindowManager.LayoutParams.FLAG_SECURE != 0

        if (!wasAlreadySecure) {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        onDispose {
            if (!wasAlreadySecure) {
                window.clearFlags(
                    WindowManager.LayoutParams.FLAG_SECURE
                )
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}