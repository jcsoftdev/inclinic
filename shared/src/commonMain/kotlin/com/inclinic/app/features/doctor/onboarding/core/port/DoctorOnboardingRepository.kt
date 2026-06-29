package com.inclinic.app.features.doctor.onboarding.core.port

import com.inclinic.app.core.model.OnboardingStatus
import com.inclinic.app.features.doctor.onboarding.core.model.DocKind
import com.inclinic.app.features.doctor.onboarding.core.model.DoctorOnboardingDraft
import com.inclinic.app.features.doctor.onboarding.core.model.UploadedDoc

interface DoctorOnboardingRepository {
    suspend fun getStatus(): Result<OnboardingStatus>
    suspend fun uploadDocument(file: ByteArray, fileName: String, kind: DocKind): Result<UploadedDoc>
    suspend fun submit(draft: DoctorOnboardingDraft): Result<Unit>
    suspend fun resubmit(corrections: Map<String, String>): Result<Unit>
}
