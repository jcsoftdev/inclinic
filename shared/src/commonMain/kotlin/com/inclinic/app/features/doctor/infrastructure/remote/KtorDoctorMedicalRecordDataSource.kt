package com.inclinic.app.features.doctor.infrastructure.remote

import com.inclinic.app.core.model.MedicalRecord
import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.time.Instant
import kotlinx.serialization.Serializable

class KtorDoctorMedicalRecordDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : DoctorMedicalRecordDataSource {

    // Lista el historial clínico del paciente. La ruta real es
    // `GET /api/patients/:id/medical-history`, que devuelve registros con las
    // relaciones `doctor.user` y `specialty` anidadas, por eso usamos un DTO propio.
    override suspend fun getMedicalRecords(patientId: String): Result<List<MedicalRecord>> = runCatching {
        client.get {
            url("$baseUrl/api/patients/$patientId/medical-history")
        }.body<ApiEnvelope<List<DoctorMedicalRecordListDto>>>().data
            ?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun getMedicalRecordById(recordId: String): Result<MedicalRecord> = runCatching {
        client.get {
            url("$baseUrl/api/medical-records/$recordId")
        }.body<ApiEnvelope<MedicalRecord>>().data ?: error("Medical record not found")
    }

    override suspend fun createMedicalRecord(request: CreateMedicalRecordRequest): Result<MedicalRecord> = runCatching {
        // El backend (`createMedicalRecordSchema`) usa `treatmentPlan` y un array
        // estructurado `prescriptions`, no campos planos. Mapeamos el texto libre
        // de receta a una entrada estructurada para pasar la validación de Zod.
        val prescriptions = request.prescription
            ?.takeIf { it.isNotBlank() }
            ?.let { listOf(CreatePrescriptionBody(medication = it, dosage = "Según indicación médica")) }
            ?: emptyList()
        client.post {
            url("$baseUrl/api/medical-records")
            contentType(ContentType.Application.Json)
            setBody(
                CreateMedicalRecordBody(
                    appointmentId = request.appointmentId?.takeIf { it.isNotBlank() },
                    patientId = request.patientId,
                    diagnosis = request.diagnosis.takeIf { it.isNotBlank() },
                    symptoms = request.symptoms.takeIf { it.isNotBlank() },
                    treatmentPlan = request.treatment.takeIf { it.isNotBlank() },
                    prescriptions = prescriptions,
                    notes = request.notes?.takeIf { it.isNotBlank() },
                ),
            )
        }.body<ApiEnvelope<MedicalRecord>>().data ?: error("Medical record creation failed")
    }

    override suspend fun updateMedicalRecord(recordId: String, request: UpdateMedicalRecordRequest): Result<MedicalRecord> = runCatching {
        client.put {
            url("$baseUrl/api/medical-records/$recordId")
            contentType(ContentType.Application.Json)
            setBody(buildMap<String, String?> {
                request.diagnosis?.let { put("diagnosis", it) }
                request.symptoms?.let { put("symptoms", it) }
                request.treatment?.let { put("treatment", it) }
                request.prescription?.let { put("prescription", it) }
                request.notes?.let { put("notes", it) }
            })
        }.body<ApiEnvelope<MedicalRecord>>().data ?: error("Medical record update failed")
    }
}

// Body de creación (`POST /api/medical-records`) con la forma exacta del backend.
@Serializable
internal data class CreateMedicalRecordBody(
    val patientId: String,
    val appointmentId: String? = null,
    val diagnosis: String? = null,
    val symptoms: String? = null,
    val treatmentPlan: String? = null,
    val prescriptions: List<CreatePrescriptionBody> = emptyList(),
    val notes: String? = null,
    val isPrivate: Boolean = false,
)

@Serializable
internal data class CreatePrescriptionBody(
    val medication: String,
    val dosage: String,
    val frequency: String = "Según indicación médica",
)

// DTO de la lista de historial clínico (`GET /api/patients/:id/medical-history`).
// El backend incluye relaciones anidadas (`doctor.user`, `specialty`) y usa
// `treatmentPlan` en vez de `treatment`, por eso mapeamos con un DTO propio.
@Serializable
internal data class DoctorMedicalRecordListDto(
    val id: String,
    val appointmentId: String? = null,
    val patientId: String,
    val doctorId: String,
    val diagnosis: String? = null,
    val symptoms: String? = null,
    val treatmentPlan: String? = null,
    val notes: String? = null,
    val isLocked: Boolean = false,
    val createdAt: Instant,
    val doctor: DoctorMrDoctorDto? = null,
    val specialty: DoctorMrSpecialtyDto? = null,
)

@Serializable
internal data class DoctorMrDoctorDto(val user: DoctorMrUserDto? = null)

@Serializable
internal data class DoctorMrUserDto(
    val firstName: String? = null,
    val lastName: String? = null,
)

@Serializable
internal data class DoctorMrSpecialtyDto(val name: String? = null)

internal fun DoctorMedicalRecordListDto.toDomain(): MedicalRecord {
    val fullName = listOfNotNull(doctor?.user?.firstName, doctor?.user?.lastName)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { null }
        ?.let { "Dr. $it" }
    return MedicalRecord(
        id = id,
        appointmentId = appointmentId ?: "",
        patientId = patientId,
        doctorId = doctorId,
        doctorName = fullName,
        specialtyName = specialty?.name,
        diagnosis = diagnosis ?: "",
        symptoms = symptoms ?: "",
        treatment = treatmentPlan ?: "",
        prescription = null,
        notes = notes,
        createdAt = createdAt,
        isLocked = isLocked,
    )
}
