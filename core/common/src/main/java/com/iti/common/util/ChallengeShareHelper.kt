package com.iti.common.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ChallengeShareHelper {

    const val BASE_URL = "https://career-pilot-indol.vercel.app"
    const val CHALLENGE_PATH = "/challenge"

    fun buildShareUrl(challengeId: String, invitationCode: String? = null): String {
        val trimmedId = challengeId.trim()
        val trimmedCode = invitationCode?.trim()
        return if (!trimmedCode.isNullOrBlank() && trimmedCode != trimmedId) {
            "$BASE_URL$CHALLENGE_PATH?id=$trimmedId&code=$trimmedCode"
        } else {
            "$BASE_URL$CHALLENGE_PATH?id=$trimmedId"
        }
    }

    fun buildShareMessage(
        creatorName: String,
        trackName: String,
        seniorityLevel: String,
        challengeType: String,
        questionsCount: Int,
        challengeId: String,
        invitationCode: String? = null
    ): String {
        val link = buildShareUrl(challengeId, invitationCode)
        val codeLine = if (!invitationCode.isNullOrBlank() && invitationCode.trim() != challengeId.trim()) "\nInvitation Code: ${invitationCode.trim()}" else ""
        val creator = if (creatorName.isNotBlank()) creatorName else "A peer"

        return """
Career Pilot Challenge Invitation

You have been invited by $creator to take on a $seniorityLevel $trackName mock interview challenge on Career Pilot ($challengeType, $questionsCount questions).

Open the challenge:
$link$codeLine
        """.trimIndent()
    }

    fun createShareTextIntent(message: String): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
    }

    fun saveQrBitmapToCache(context: Context, bitmap: Bitmap, filename: String = "challenge_qr.png"): File? {
        return try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file
        } catch (_: Exception) {
            null
        }
    }

    fun createShareImageIntent(context: Context, imageFile: File, caption: String): Intent {
        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, imageFile)

        return Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun createShareChallengeIntent(
        context: Context,
        message: String,
        qrBitmap: Bitmap? = null
    ): Intent {
        val imageFile = if (qrBitmap != null) {
            saveQrBitmapToCache(context, qrBitmap)
        } else {
            null
        }

        return if (imageFile != null) {
            createShareImageIntent(context, imageFile, message)
        } else {
            createShareTextIntent(message)
        }
    }
}
