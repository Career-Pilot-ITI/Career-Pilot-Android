package com.iti.careerpilot.editprofile.data.datasource.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import com.iti.careerpilot.editprofile.domain.datasource.local.ImageCompressor
import com.iti.common.dispatcher.CareerPilotDispatchers
import com.iti.common.dispatcher.Dispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ImageCompressorImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:Dispatcher(CareerPilotDispatchers.Default) private val dispatcherDefault: CoroutineDispatcher,
    @param:Dispatcher(CareerPilotDispatchers.IO) private val dispatcherIO: CoroutineDispatcher,
): ImageCompressor {
    override suspend fun compressToFile(
        uri: Uri,
        maxDimension: Int,
        quality: Int
    ): File? {
        return try {
            val contentResolver = context.contentResolver
            val boundsOptions = withContext(dispatcherIO) {
                BitmapFactory.Options().apply { inJustDecodeBounds = true }.also { options ->
                    contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }
                }
            }
            val sampleSize = calculateInSampleSize(boundsOptions, maxDimension, maxDimension)
            val rawBitmap = withContext(dispatcherIO) {
                val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, decodeOptions)
                } ?: throw IllegalArgumentException("Could not decode image from uri: $uri")
            }
            val outputFile = withContext(dispatcherDefault) {
                val bitmap = fixOrientation(context, uri, rawBitmap)
                val scaledBitmap = scaleDown(bitmap, maxDimension)

                val file = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
                withContext(dispatcherIO) {
                    FileOutputStream(file).use { out ->
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    }
                }
                if (scaledBitmap !== rawBitmap) scaledBitmap.recycle()
                if (bitmap !== rawBitmap) bitmap.recycle()
                rawBitmap.recycle()
                file
            }
            outputFile
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun scaleDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val ratio = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
        if (ratio >= 1f) return bitmap
        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()
        return bitmap.scale(newWidth, newHeight)
    }

    private fun fixOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        val exifStream = context.contentResolver.openInputStream(uri) ?: return bitmap
        val exif = exifStream.use { ExifInterface(it) }
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}