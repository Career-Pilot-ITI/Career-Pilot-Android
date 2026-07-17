package com.iti.onboarding.domain.model

/**
 * Platform-independent representation of the selected CV.
 *
 * The Android Uri is resolved in the data layer before this model reaches the
 * domain layer. The byte array is ready for a future multipart/binary upload.
 */
class CvDocument(
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val bytes: ByteArray,
)
