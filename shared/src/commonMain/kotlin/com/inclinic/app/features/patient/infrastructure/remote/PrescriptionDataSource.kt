package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.Prescription

interface PrescriptionDataSource {
    suspend fun getPatientPrescriptions(): Result<List<Prescription>>
    suspend fun getPrescriptionDetail(prescriptionId: String): Result<Prescription>

    /**
     * Absolute URL of the backend-generated PDF for a prescription.
     * Note: the endpoint requires Bearer auth, so opening this URL in a plain
     * browser will fail with 401 unless the platform viewer forwards the token.
     */
    fun prescriptionPdfUrl(prescriptionId: String): String

    /**
     * Downloads the PDF bytes through the authenticated Ktor client.
     * This is the auth-correct path; rendering the bytes requires a platform
     * file handler (write-to-temp + open system viewer), not yet wired.
     */
    suspend fun downloadPrescriptionPdf(prescriptionId: String): Result<ByteArray>
}
