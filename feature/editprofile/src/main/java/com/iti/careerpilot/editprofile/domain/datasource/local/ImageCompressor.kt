package com.iti.careerpilot.editprofile.domain.datasource.local

import android.net.Uri
import java.io.File

interface ImageCompressor {
    suspend fun compressToFile(
        uri: Uri,
        maxDimension: Int = 1080,
        quality: Int = 80
    ): File?
}