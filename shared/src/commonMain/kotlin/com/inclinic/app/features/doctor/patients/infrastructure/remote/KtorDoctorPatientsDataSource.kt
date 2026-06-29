package com.inclinic.app.features.doctor.patients.infrastructure.remote

import com.inclinic.app.core.network.ApiEnvelope
import com.inclinic.app.features.doctor.patients.infrastructure.remote.dto.PatientListDto
import com.inclinic.app.features.doctor.patients.infrastructure.remote.dto.PatientListItemDto
import com.inclinic.app.features.doctor.patients.infrastructure.remote.dto.PatientSearchDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

class KtorDoctorPatientsDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : DoctorPatientsDataSource {

    override suspend fun getPatients(): Result<PatientListDto> = runCatching {
        client.get {
            url("$baseUrl/api/doctors/me/patients")
        }.body<ApiEnvelope<PatientListDto>>().data ?: PatientListDto()
    }

    override suspend fun searchPatientByEmail(query: String): Result<List<PatientListItemDto>> = runCatching {
        val response: HttpResponse = client.get {
            url("$baseUrl/api/patients/search")
            parameter("email", query)
        }
        if (response.status == HttpStatusCode.NotFound) return@runCatching emptyList()
        val patient = response.body<ApiEnvelope<PatientSearchDto>>().data
            ?: return@runCatching emptyList()
        listOf(
            PatientListItemDto(
                id = patient.id,
                name = "${patient.user.firstName} ${patient.user.lastName}".trim(),
                avatarUrl = patient.avatar,
                lastVisitDate = null,
                totalAppointments = 0,
                status = null,
            ),
        )
    }
}
