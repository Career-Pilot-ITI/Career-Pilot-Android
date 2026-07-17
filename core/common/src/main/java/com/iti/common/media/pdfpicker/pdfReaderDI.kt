package com.iti.common.media.pdfpicker

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PDFReaderDI {

    @Binds
    abstract fun bindsPdfReader(
        impl: PdfReaderImpl
    ): PdfReader
}