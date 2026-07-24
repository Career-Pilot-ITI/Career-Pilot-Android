package com.iti.careerpilot.features.paywall.util

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent

/**
 * Utility helper to launch payment URLs via Chrome Custom Tabs safely and reliably.
 */
object CustomTabManager {

    fun launch(
        context: Context,
        url: String,
        toolbarColor: Int? = null
    ) {
        val builder = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setUrlBarHidingEnabled(false)

        toolbarColor?.let { color ->
            val colorSchemeParams = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(color)
                .build()
            builder.setDefaultColorSchemeParams(colorSchemeParams)
        }

        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }
}
