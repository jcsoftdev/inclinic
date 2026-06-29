package com.inclinic.app.features.patient.medical_history.application

import com.inclinic.app.features.patient.infrastructure.remote.PrescriptionDataSource

/**
 * Builds the absolute URL of the backend-generated prescription PDF.
 *
 * The screen opens this URL with the platform viewer (`LocalUriHandler`).
 * Caveat: `GET /prescriptions/:id/pdf` requires Bearer auth, so a plain
 * browser open may return 401. See [DownloadPrescriptionPdfUseCase] for the
 * auth-correct byte download (pending a platform file handler).
 */
class GetPrescriptionPdfUrlUseCase(
    private val dataSource: PrescriptionDataSource,
) {
    operator fun invoke(prescriptionId: String): String =
        dataSource.prescriptionPdfUrl(prescriptionId)
}
