package com.iti.common.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QrCodeGeneratorTest {

    @Test
    fun `generateBitMatrix with empty content returns null`() {
        val result = QrCodeGenerator.generateBitMatrix("", 200)
        assertNull(result)
    }

    @Test
    fun `generateBitMatrix with zero or negative size returns null`() {
        val result = QrCodeGenerator.generateBitMatrix("test", 0)
        assertNull(result)
    }

    @Test
    fun `generateBitMatrix with valid content generates expected matrix dimensions`() {
        val content = "https://career-pilot-indol.vercel.app/challenge?id=abc123"
        val size = 256
        val matrix = QrCodeGenerator.generateBitMatrix(content, size)
        assertNotNull(matrix)
        assertEquals(size, matrix?.width)
        assertEquals(size, matrix?.height)
    }

    @Test
    fun `generateQrBitmap with empty content returns null`() {
        val result = QrCodeGenerator.generateQrBitmap("", 200)
        assertNull(result)
    }

    @Test
    fun `generateQrBitmap with valid content generates non null bitmap`() {
        val content = "https://career-pilot-indol.vercel.app/challenge?id=abc123"
        val bitmap = QrCodeGenerator.generateQrBitmap(content, 256)
        assertNotNull(bitmap)
        assertEquals(256, bitmap?.width)
        assertEquals(256, bitmap?.height)
    }
}
