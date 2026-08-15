package com.iti.careerpilot.ats.domain.usecase

import com.iti.careerpilot.ats.domain.repository.AtsRepository
import com.iti.core.model.PdfFile
import javax.inject.Inject

class ReplaceCurrentCvUseCase @Inject constructor(
    private val repository: AtsRepository,
) {
    suspend operator fun invoke(file: PdfFile, onProgress: (Int) -> Unit) =
        repository.replaceCurrentCv(file, onProgress)
}
