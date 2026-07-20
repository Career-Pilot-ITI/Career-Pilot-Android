package com.iti.common.media.pdfpicker

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private const val PDF_MIME_TYPE = "application/pdf"

class PdfPickerLauncher(
    private val onLaunchPdfPicker: () -> Unit
) {
    fun launchPdfPicker() {
        onLaunchPdfPicker()
    }
}


@Composable
fun rememberPdfPickerLauncher(
    onPdfSelected: (Uri) -> Unit
): PdfPickerLauncher {
    val context = LocalContext.current

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }

        onPdfSelected(uri)
    }

    return remember(pdfLauncher) {
        PdfPickerLauncher(
            onLaunchPdfPicker = {
                pdfLauncher.launch(arrayOf(PDF_MIME_TYPE))
            }
        )
    }
}