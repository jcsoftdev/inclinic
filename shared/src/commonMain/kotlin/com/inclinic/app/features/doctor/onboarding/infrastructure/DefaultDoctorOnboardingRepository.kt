package com.inclinic.app.features.doctor.onboarding.infrastructure

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.OnboardingStatus
import com.inclinic.app.features.doctor.onboarding.core.model.DocKind
import com.inclinic.app.features.doctor.onboarding.core.model.DoctorOnboardingDraft
import com.inclinic.app.features.doctor.onboarding.core.model.UploadedDoc
import com.inclinic.app.features.doctor.onboarding.core.port.DoctorOnboardingRepository
import com.inclinic.app.features.doctor.onboarding.infrastructure.remote.DoctorOnboardingDataSource
import kotlinx.coroutines.withContext

class DefaultDoctorOnboardingRepository(
    private val remote: DoctorOnboardingDataSource,
    private val dispatchers: AppDispatchers,
) : DoctorOnboardingRepository {

    override suspend fun getStatus(): Result<OnboardingStatus> =
        withContext(dispatchers.io) {
            remote.getStatus().map { dto ->
                runCatching { OnboardingStatus.valueOf(dto.status) }
                    .getOrElse { OnboardingStatus.NONE }
            }
        }

    override suspend fun uploadDocument(
        file: ByteArray,
        fileName: String,
        kind: DocKind,
    ): Result<UploadedDoc> =
        withContext(dispatchers.io) {
            remote.uploadDocument(file, fileName, kind).map { dto ->
                UploadedDoc(
                    id = dto.id,
                    kind = runCatching { DocKind.valueOf(dto.kind) }.getOrElse { DocKind.OTHER },
                    url = dto.url,
                )
            }
        }

    override suspend fun submit(draft: DoctorOnboardingDraft): Result<Unit> =
        withContext(dispatchers.io) { remote.submit(draft) }

    override suspend fun resubmit(corrections: Map<String, String>): Result<Unit> =
        withContext(dispatchers.io) { remote.resubmit(corrections) }
}
