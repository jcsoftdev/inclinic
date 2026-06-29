package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.Prescription
import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsBytes

class KtorPrescriptionDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : PrescriptionDataSource {

    override suspend fun getPatientPrescriptions(): Result<List<Prescription>> = runCatching {
        client.get {
            url("$baseUrl/api/prescriptions")
        }.body<ApiEnvelope<List<Prescription>>>().data ?: emptyList()
    }

    override suspend fun getPrescriptionDetail(prescriptionId: String): Result<Prescription> = runCatching {
        client.get {
            url("$baseUrl/api/prescriptions/$prescriptionId")
        }.body<ApiEnvelope<Prescription>>().data!!
    }

    override fun prescriptionPdfUrl(prescriptionId: String): String =
        "$baseUrl/api/prescriptions/$prescriptionId/pdf"

    override suspend fun downloadPrescriptionPdf(prescriptionId: String): Result<ByteArray> = runCatching {
        client.get {
            url(prescriptionPdfUrl(prescriptionId))
        }.bodyAsBytes()
    }
}
