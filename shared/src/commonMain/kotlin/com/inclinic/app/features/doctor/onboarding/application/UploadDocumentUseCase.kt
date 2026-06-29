package com.inclinic.app.features.doctor.onboarding.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.onboarding.core.model.DocKind
import com.inclinic.app.features.doctor.onboarding.core.model.UploadedDoc
import com.inclinic.app.features.doctor.onboarding.core.port.DoctorOnboardingRepository
import kotlinx.coroutines.withContext

class UploadDocumentUseCase(
    private val repository: DoctorOnboardingRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        file: ByteArray,
        fileName: String,
        kind: DocKind,
    ): Result<UploadedDoc> =
        withContext(dispatchers.io) { repository.uploadDocument(file, fileName, kind) }
}
