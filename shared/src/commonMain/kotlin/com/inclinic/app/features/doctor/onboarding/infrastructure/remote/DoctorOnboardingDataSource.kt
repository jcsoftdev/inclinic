package com.inclinic.app.features.doctor.onboarding.infrastructure.remote

import com.inclinic.app.features.doctor.onboarding.core.model.DocKind
import com.inclinic.app.features.doctor.onboarding.core.model.DoctorOnboardingDraft
import com.inclinic.app.features.doctor.onboarding.infrastructure.remote.dto.OnboardingStatusDto
import com.inclinic.app.features.doctor.onboarding.infrastructure.remote.dto.UploadedDocDto

interface DoctorOnboardingDataSource {
    suspend fun getStatus(): Result<OnboardingStatusDto>
    suspend fun uploadDocument(file: ByteArray, fileName: String, kind: DocKind): Result<UploadedDocDto>
    suspend fun submit(draft: DoctorOnboardingDraft): Result<Unit>
    suspend fun resubmit(corrections: Map<String, String>): Result<Unit>
}
