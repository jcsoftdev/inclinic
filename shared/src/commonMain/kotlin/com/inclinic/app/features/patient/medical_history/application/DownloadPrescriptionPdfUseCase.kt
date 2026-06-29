package com.inclinic.app.features.patient.medical_history.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.PrescriptionDataSource
import kotlinx.coroutines.withContext

/**
 * Downloads the prescription PDF bytes through the authenticated Ktor client.
 *
 * This is the auth-correct path (carries the Bearer token, unlike a plain
 * browser open). Rendering the bytes still needs a platform file handler
 * (write-to-temp + open system viewer), which is not yet wired — see report.
 */
class DownloadPrescriptionPdfUseCase(
    private val dataSource: PrescriptionDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(prescriptionId: String): Result<ByteArray> =
        withContext(dispatchers.io) { dataSource.downloadPrescriptionPdf(prescriptionId) }
}
