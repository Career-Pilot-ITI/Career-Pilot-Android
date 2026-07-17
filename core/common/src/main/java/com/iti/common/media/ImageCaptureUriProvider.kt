package com.iti.common.media

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class ImageCaptureUriProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun createImageCaptureUri(): Uri {
        val file = File(context.cacheDir, "avatar_capture_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
