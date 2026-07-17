package com.iti.common.media.pdfpicker

import com.iti.common.error.StorageError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.PdfFile

interface PdfReader {
    suspend fun readPdf(uri: String):  CareerPilotResult<PdfFile, StorageError>
}