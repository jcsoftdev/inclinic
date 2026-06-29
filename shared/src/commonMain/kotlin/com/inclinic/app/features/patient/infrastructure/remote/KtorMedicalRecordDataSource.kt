package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.AccessType
import com.inclinic.app.core.model.HistoryAccessLog
import com.inclinic.app.core.model.MedicalRecord
import com.inclinic.app.core.model.MedicalRecordDetail
import com.inclinic.app.core.model.RecordPrescription
import com.inclinic.app.core.model.VitalSigns
import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class KtorMedicalRecordDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : MedicalRecordDataSource {

    override suspend fun getPatientRecords(patientId: String): Result<List<MedicalRecord>> = runCatching {
        // Ruta real scoped al paciente. /api/medical-records solo expone POST (DOCTOR).
        client.get {
            url("$baseUrl/api/patients/$patientId/medical-history")
        }.body<ApiEnvelope<List<MedicalRecordListDto>>>().data
            ?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun getRecordDetail(recordId: String): Result<MedicalRecordDetail> = runCatching {
        client.get {
            url("$baseUrl/api/medical-records/$recordId")
        }.body<ApiEnvelope<MedicalRecordDetailDto>>().data!!.toDomain()
    }

    override suspend fun getAccessLogs(): Result<List<HistoryAccessLog>> = runCatching {
        // Ruta real: el feed de quién leyó la historia del paciente actual.
        // /api/medical-records/access-logs no existe.
        client.get {
            url("$baseUrl/api/patients/me/access-log")
        }.body<ApiEnvelope<List<AccessLogDto>>>().data
            ?.map { it.toDomain() } ?: emptyList()
    }
}

// ─── DTO: shape de MedicalAccessLog (medicalAccessLog.service.listAccessLogForPatient) ───
// El backend devuelve filas Prisma con `viewer { firstName, lastName, role }`,
// `action` (READ|DOWNLOAD|LIST), `ip`, `userAgent` y `createdAt`.

@Serializable
internal data class AccessLogDto(
    val id: String,
    val viewerUserId: String = "",
    val viewerRole: String? = null,
    val action: String = "READ",
    val ip: String? = null,
    val userAgent: String? = null,
    val createdAt: Instant,
    val viewer: AccessLogViewerDto? = null,
)

@Serializable
internal data class AccessLogViewerDto(
    val firstName: String? = null,
    val lastName: String? = null,
    val role: String? = null,
)

internal fun AccessLogDto.toDomain(): HistoryAccessLog {
    val rawName = listOfNotNull(viewer?.firstName, viewer?.lastName)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { null }
    val isDoctor = (viewer?.role ?: viewerRole)?.equals("DOCTOR", ignoreCase = true) == true
    return HistoryAccessLog(
        id = id,
        doctorId = viewerUserId,
        doctorName = rawName?.let { if (isDoctor) "Dr. $it" else it },
        accessType = when (action.trim().uppercase()) {
            "DOWNLOAD" -> AccessType.EXPORT_PDF
            "LIST" -> AccessType.RECORDS_ONLY
            else -> AccessType.READ
        },
        ipAddress = ip,
        deviceInfo = userAgent,
        accessedAt = createdAt,
    )
}

// ─── DTO para la lista (medical.service.ts getPatientMedicalHistory) ─────────
// El backend incluye `doctor.user` y `specialty` en la respuesta de lista.
// Usamos un DTO propio para evitar ignorar esas relaciones al deserializar.

@Serializable
internal data class MedicalRecordListDto(
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
    val doctor: MrDoctorDto? = null,
    val specialty: SpecialtyDto? = null,
)

internal fun MedicalRecordListDto.toDomain(): MedicalRecord {
    val first = doctor?.user?.firstName
    val last = doctor?.user?.lastName
    val fullName = listOfNotNull(first, last)
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

// ─── DTOs: shape exacto del backend (medical.service.ts getMedicalRecordById) ───
// El backend devuelve relaciones anidadas (doctor.user, specialty, appointment) y
// los campos sensibles nulleados cuando isLocked = true (paciente FREE).

@Serializable
internal data class MedicalRecordDetailDto(
    val id: String,
    val appointmentId: String? = null,
    val doctorId: String,
    val chiefComplaint: String? = null,
    val symptoms: String? = null,
    val vitalSigns: VitalSignsDto? = null,
    val diagnosis: String? = null,
    val treatmentPlan: String? = null,
    val prescriptions: List<PrescriptionDto> = emptyList(),
    val studiesOrdered: List<String> = emptyList(),
    val attachments: List<String> = emptyList(),
    val notes: String? = null,
    val followUpDate: Instant? = null,
    val createdAt: Instant,
    val isLocked: Boolean = false,
    val doctor: MrDoctorDto? = null,
    val specialty: SpecialtyDto? = null,
)

@Serializable
internal data class MrDoctorDto(val user: MrDoctorUserDto? = null)

@Serializable
internal data class MrDoctorUserDto(
    val firstName: String? = null,
    val lastName: String? = null,
)

@Serializable
internal data class SpecialtyDto(val name: String? = null)

@Serializable
internal data class VitalSignsDto(
    @SerialName("bloodPressureSystolic") val bloodPressureSystolic: Int? = null,
    @SerialName("bloodPressureDiastolic") val bloodPressureDiastolic: Int? = null,
    val heartRate: Int? = null,
    val temperature: Double? = null,
    val oxygenSaturation: Int? = null,
    val weight: Double? = null,
    val height: Double? = null,
)

@Serializable
internal data class PrescriptionDto(
    val medication: String? = null,
    val dosage: String? = null,
    val frequency: String? = null,
    val duration: String? = null,
    val instructions: String? = null,
)

internal fun MedicalRecordDetailDto.toDomain(): MedicalRecordDetail {
    val first = doctor?.user?.firstName
    val last = doctor?.user?.lastName
    val fullName = listOfNotNull(first, last)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { null }
        ?.let { "Dr. $it" }

    return MedicalRecordDetail(
        id = id,
        appointmentId = appointmentId,
        doctorId = doctorId,
        doctorName = fullName,
        specialtyName = specialty?.name,
        recordDate = createdAt,
        chiefComplaint = chiefComplaint,
        symptoms = symptoms,
        vitalSigns = vitalSigns?.let {
            VitalSigns(
                bloodPressureSystolic = it.bloodPressureSystolic,
                bloodPressureDiastolic = it.bloodPressureDiastolic,
                heartRate = it.heartRate,
                temperature = it.temperature,
                oxygenSaturation = it.oxygenSaturation,
                weight = it.weight,
                height = it.height,
            )
        },
        diagnosis = diagnosis,
        treatmentPlan = treatmentPlan,
        prescriptions = prescriptions.map {
            RecordPrescription(
                medication = it.medication,
                dosage = it.dosage,
                frequency = it.frequency,
                duration = it.duration,
                instructions = it.instructions,
            )
        },
        studiesOrdered = studiesOrdered,
        attachments = attachments,
        followUpDate = followUpDate,
        notes = notes,
        isLocked = isLocked,
    )
}
