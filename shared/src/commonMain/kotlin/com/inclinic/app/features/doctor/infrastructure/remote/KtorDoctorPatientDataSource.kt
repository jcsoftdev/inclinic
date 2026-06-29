package com.inclinic.app.features.doctor.infrastructure.remote

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url

class KtorDoctorPatientDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : DoctorPatientDataSource {

    override suspend fun getPatientDetail(patientId: String): Result<PatientDetail> = runCatching {
        client.get {
            url("$baseUrl/api/patients/$patientId")
        }.body<ApiEnvelope<PatientDetail>>().data ?: error("Patient not found")
    }

    override suspend fun getPatientAppointments(patientId: String, limit: Int): Result<List<Appointment>> = runCatching {
        client.get {
            url("$baseUrl/api/appointments")
            parameter("patientId", patientId)
            parameter("limit", limit)
        }.body<ApiEnvelope<List<Appointment>>>().data ?: emptyList()
    }
}
