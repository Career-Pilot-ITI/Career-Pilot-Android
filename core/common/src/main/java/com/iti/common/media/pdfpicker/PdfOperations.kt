package com.iti.common.media.pdfpicker

import com.iti.common.error.StorageError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.PdfFile

interface PdfOperations {
    suspend fun readPdf(uri: String): CareerPilotResult<PdfFile, StorageError>
    suspend fun storePdfInternally(uri: String): CareerPilotResult<String, StorageError>
}