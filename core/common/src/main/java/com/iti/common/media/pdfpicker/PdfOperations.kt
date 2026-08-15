package com.iti.common.media.pdfpicker

import android.net.Uri
import com.iti.common.error.StorageError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.PdfFile
import com.iti.core.model.PdfFileMetadata

interface PdfOperations {
    suspend fun readPdf(uri: String): CareerPilotResult<PdfFile, StorageError>
    suspend fun getPdfMetaData(uri: Uri): CareerPilotResult<PdfFileMetadata, StorageError>
    suspend fun storePdfInternally(file: PdfFile): CareerPilotResult<String, StorageError>
}
