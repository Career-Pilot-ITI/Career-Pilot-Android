package com.iti.common.media.pdfpicker

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toUri
import com.iti.common.dispatcher.CareerPilotDispatchers
import com.iti.common.dispatcher.Dispatcher
import com.iti.common.error.StorageError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.PdfFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import javax.inject.Inject


class PdfOperationsImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:Dispatcher(CareerPilotDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : PdfOperations {

    private val contentResolver: ContentResolver = context.contentResolver

    override suspend fun readPdf(
        uri: String,
    ): CareerPilotResult<PdfFile, StorageError> = withContext(ioDispatcher) {
        try {
            val parsedUri = uri.toUri()
            val metadata = readMetadata(parsedUri)
            val mimeType = contentResolver.getType(parsedUri)
                ?: PDF_MIME_TYPE

            val isPdf = mimeType.equals(PDF_MIME_TYPE, ignoreCase = true) ||
                    metadata.name.endsWith(PDF_EXTENSION, ignoreCase = true)

            if (!isPdf) {
                return@withContext CareerPilotResult.Error(
                    StorageError.INCOMPATIBLE_FILE,
                )
            }

            if (metadata.sizeBytes != null && metadata.sizeBytes > MAX_CV_SIZE_BYTES) {
                return@withContext CareerPilotResult.Error(
                    StorageError.FILE_TOO_LARGE,
                )
            }

            val bytes = readBytesWithLimit(parsedUri)
                ?: return@withContext CareerPilotResult.Error(
                    StorageError.UNKNOWN,
                )

            if (bytes.size > MAX_CV_SIZE_BYTES) {
                return@withContext CareerPilotResult.Error(
                    StorageError.FILE_TOO_LARGE,
                )
            }

            CareerPilotResult.Success(
                PdfFile(
                    name = metadata.name,
                    mimeType = PDF_MIME_TYPE,
                    sizeBytes = bytes.size.toLong(),
                    bytes = bytes,
                )
            )
        } catch (_: FileTooLargeException) {
            CareerPilotResult.Error(StorageError.FILE_TOO_LARGE)
        } catch (_: SecurityException) {
            CareerPilotResult.Error(StorageError.UNKNOWN)
        } catch (_: IllegalArgumentException) {
            CareerPilotResult.Error(StorageError.UNKNOWN)
        } catch (_: IOException) {
            CareerPilotResult.Error(StorageError.UNKNOWN)
        }
    }

    override suspend fun storePdfInternally(uri: String): CareerPilotResult<String, StorageError> =
        withContext(ioDispatcher) {
            try {
                val parsedUri = uri.toUri()

                val inputStream = context.contentResolver.openInputStream(parsedUri)
                    ?: return@withContext CareerPilotResult.Error(StorageError.FileNotFound)

                val fileName = "cv_${System.currentTimeMillis()}.pdf"
                val destinationFile = File(context.filesDir, fileName)

                inputStream.use { input ->
                    destinationFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val savedUriString = Uri.fromFile(destinationFile).toString()
                CareerPilotResult.Success(savedUriString)
            } catch (e: SecurityException) {
                CareerPilotResult.Error(StorageError.PermissionDenied)
            } catch (e: Exception) {
                CareerPilotResult.Error(StorageError.UNKNOWN)
            }
        }

    private fun readMetadata(uri: Uri): FileMetadata {
        var name = DEFAULT_FILE_NAME
        var size: Long? = null

        contentResolver.query(
            uri,
            arrayOf(
                OpenableColumns.DISPLAY_NAME,
                OpenableColumns.SIZE,
            ),
            null,
            null,
            null,
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use

            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

            if (nameIndex >= 0 && !cursor.isNull(nameIndex)) {
                name = cursor.getString(nameIndex)
            }

            if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                size = cursor.getLong(sizeIndex)
            }
        }

        return FileMetadata(
            name = name,
            sizeBytes = size,
        )
    }

    private fun readBytesWithLimit(uri: Uri): ByteArray? {
        val inputStream = contentResolver.openInputStream(uri) ?: return null

        inputStream.use { input ->
            ByteArrayOutputStream().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var totalBytes = 0

                while (true) {
                    val readCount = input.read(buffer)
                    if (readCount == -1) break

                    totalBytes += readCount
                    if (totalBytes > MAX_CV_SIZE_BYTES) {
                        throw FileTooLargeException()
                    }

                    output.write(buffer, 0, readCount)
                }

                return output.toByteArray()
            }
        }
    }

    private data class FileMetadata(
        val name: String,
        val sizeBytes: Long?,
    )

    private class FileTooLargeException : IOException()

    private companion object {
        const val PDF_MIME_TYPE = "application/pdf"
        const val PDF_EXTENSION = ".pdf"
        const val DEFAULT_FILE_NAME = "resume.pdf"
        const val MAX_CV_SIZE_BYTES = 10 * 1024 * 1024
        const val DEFAULT_BUFFER_SIZE = 8 * 1024
    }
}