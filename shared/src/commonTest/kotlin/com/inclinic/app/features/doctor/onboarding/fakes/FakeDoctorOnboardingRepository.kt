package com.inclinic.app.features.doctor.onboarding.fakes

import com.inclinic.app.core.model.OnboardingStatus
import com.inclinic.app.features.doctor.onboarding.core.model.DocKind
import com.inclinic.app.features.doctor.onboarding.core.model.DoctorOnboardingDraft
import com.inclinic.app.features.doctor.onboarding.core.model.UploadedDoc
import com.inclinic.app.features.doctor.onboarding.core.port.DoctorOnboardingRepository

/**
 * In-memory fake for [DoctorOnboardingRepository].
 * Tests configure results before calling; call counts verify no-network guarantees.
 */
class FakeDoctorOnboardingRepository : DoctorOnboardingRepository {

    var statusResult: Result<OnboardingStatus> = Result.success(OnboardingStatus.PENDING)
    var uploadResult: Result<UploadedDoc> = Result.success(
        UploadedDoc(id = "doc-1", kind = DocKind.CMP_LICENSE, url = "https://cdn.inclinic.com/doc-1")
    )
    var submitResult: Result<Unit> = Result.success(Unit)
    var resubmitResult: Result<Unit> = Result.success(Unit)

    var getStatusCallCount = 0
    var uploadCallCount = 0
    var submitCallCount = 0
    var resubmitCallCount = 0

    var lastUploadedFile: ByteArray? = null
    var lastUploadedFileName: String? = null
    var lastUploadedKind: DocKind? = null
    var lastSubmittedDraft: DoctorOnboardingDraft? = null
    var lastResubmitCorrections: Map<String, String>? = null

    override suspend fun getStatus(): Result<OnboardingStatus> {
        getStatusCallCount++
        return statusResult
    }

    override suspend fun uploadDocument(
        file: ByteArray,
        fileName: String,
        kind: DocKind,
    ): Result<UploadedDoc> {
        uploadCallCount++
        lastUploadedFile = file
        lastUploadedFileName = fileName
        lastUploadedKind = kind
        return uploadResult
    }

    override suspend fun submit(draft: DoctorOnboardingDraft): Result<Unit> {
        submitCallCount++
        lastSubmittedDraft = draft
        return submitResult
    }

    override suspend fun resubmit(corrections: Map<String, String>): Result<Unit> {
        resubmitCallCount++
        lastResubmitCorrections = corrections
        return resubmitResult
    }
}
