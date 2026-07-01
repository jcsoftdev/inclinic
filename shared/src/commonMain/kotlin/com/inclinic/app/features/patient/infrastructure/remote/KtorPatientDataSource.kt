package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.EmergencyContact
import com.inclinic.app.core.model.MedicalProfile
import com.inclinic.app.core.model.PatientProfile
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
private data class DashboardApptUserDto(val firstName: String? = null, val lastName: String? = null)
@Serializable
private data class DashboardApptDoctorDto(val user: DashboardApptUserDto? = null)
@Serializable
private data class DashboardApptSpecialtyDto(val name: String? = null)
@Serializable
private data class DashboardApptDto(
    val id: String,
    val doctorId: String,
    val patientId: String,
    val specialtyId: String? = null,
    val status: String = "PENDING_PAYMENT",
    val price: Double = 0.0,
    val commission: Double = 0.0,
    val startTime: String? = null,
    val endTime: String? = null,
    val rescheduleCount: Int = 0,
    val paymentDeadline: String? = null,
    val notes: String? = null,
    val createdAt: String? = null,
    val isHomeVisit: Boolean = false,
    val isTelemedicine: Boolean = false,
    val doctor: DashboardApptDoctorDto? = null,
    val specialty: DashboardApptSpecialtyDto? = null,
    val rescheduleRequests: List<DashboardRescheduleDto>? = null,
) {
    fun toDomain(): Appointment {
        val now = Clock.System.now()
        val fn = doctor?.user?.firstName
        val ln = doctor?.user?.lastName
        val fullName = listOfNotNull(fn, ln).joinToString(" ").ifBlank { null }
        return Appointment(
            id = id,
            doctorId = doctorId,
            patientId = patientId,
            specialtyId = specialtyId ?: "",
            visitType = when {
                isTelemedicine -> VisitType.VIRTUAL
                isHomeVisit -> VisitType.HOME
                else -> VisitType.CLINIC
            },
            status = runCatching { AppointmentStatus.valueOf(status) }.getOrDefault(AppointmentStatus.PENDING_PAYMENT),
            consultationFee = price,
            commissionAmount = commission,
            startsAt = startTime?.let { runCatching { Instant.parse(it) }.getOrElse { now } } ?: now,
            endsAt = endTime?.let { runCatching { Instant.parse(it) }.getOrElse { now } } ?: now,
            rescheduleCount = rescheduleCount,
            paymentDeadline = paymentDeadline?.let { runCatching { Instant.parse(it) }.getOrNull() },
            notes = notes,
            createdAt = createdAt?.let { runCatching { Instant.parse(it) }.getOrElse { now } } ?: now,
            doctorName = fullName,
            specialtyName = specialty?.name,
            hasPendingReschedule = rescheduleRequests?.any { it.status == "PENDING" } == true,
        )
    }
}

@Serializable
private data class DashboardRescheduleDto(val id: String = "", val status: String = "PENDING")

@Serializable
private data class MedicalProfileDto(
    val bloodType: String? = null,
    val allergies: List<String> = emptyList(),
    val chronicConditions: List<String> = emptyList(),
    val currentMedications: List<String> = emptyList(),
    val pastSurgeries: List<String> = emptyList(),
    val familyHistory: String? = null,
    val heightCm: Float? = null,
    val weightKg: Float? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val emergencyContactRelation: String? = null,
    val insuranceProvider: String? = null,
    val insuranceNumber: String? = null,
) {
    fun toDomain(): MedicalProfile = MedicalProfile(
        bloodType = bloodType,
        allergies = allergies,
        chronicConditions = chronicConditions,
        currentMedications = currentMedications,
        pastSurgeries = pastSurgeries,
        familyHistory = familyHistory,
        heightCm = heightCm,
        weightKg = weightKg,
        emergencyContact = EmergencyContact(
            name = emergencyContactName,
            phone = emergencyContactPhone,
            relation = emergencyContactRelation,
        ),
        insuranceProvider = insuranceProvider,
        insuranceNumber = insuranceNumber,
    )
}

class KtorPatientDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : PatientDataSource {

    override suspend fun getPatientProfile(patientId: String): Result<PatientProfile> = runCatching {
        client.get {
            url("$baseUrl/api/patients/$patientId")
        }.body<ApiEnvelope<PatientProfile>>().data ?: error("Patient not found")
    }

    override suspend fun updatePatientProfile(
        patientId: String,
        name: String,
        phone: String?,
        dateOfBirth: String?,
    ): Result<PatientProfile> = runCatching {
        client.patch {
            url("$baseUrl/api/patients/$patientId")
            contentType(ContentType.Application.Json)
            setBody(buildMap<String, Any?> {
                put("name", name)
                if (phone != null) put("phone", phone)
                if (dateOfBirth != null) put("dateOfBirth", dateOfBirth)
            })
        }.body<ApiEnvelope<PatientProfile>>().data ?: error("Update failed")
    }

    override suspend fun getMedicalProfile(patientId: String): Result<MedicalProfile> = runCatching {
        client.get {
            url("$baseUrl/api/patients/$patientId/medical-profile")
        }.body<ApiEnvelope<MedicalProfileDto?>>().data?.toDomain() ?: MedicalProfile.empty()
    }

    override suspend fun updateMedicalProfile(patientId: String, profile: MedicalProfile): Result<MedicalProfile> = runCatching {
        // Build request body omitting nulls — backend does upsert (PUT).
        val body = buildMap<String, Any?> {
            profile.bloodType?.let { put("bloodType", it) }
            put("allergies", profile.allergies)
            put("chronicConditions", profile.chronicConditions)
            put("currentMedications", profile.currentMedications)
            put("pastSurgeries", profile.pastSurgeries)
            profile.familyHistory?.let { put("familyHistory", it) }
            profile.heightCm?.let { put("heightCm", it) }
            profile.weightKg?.let { put("weightKg", it) }
            profile.emergencyContact.name?.let { put("emergencyContactName", it) }
            profile.emergencyContact.phone?.let { put("emergencyContactPhone", it) }
            profile.emergencyContact.relation?.let { put("emergencyContactRelation", it) }
            profile.insuranceProvider?.let { put("insuranceProvider", it) }
            profile.insuranceNumber?.let { put("insuranceNumber", it) }
        }
        client.put {
            url("$baseUrl/api/patients/$patientId/medical-profile")
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<ApiEnvelope<MedicalProfileDto?>>().data?.toDomain() ?: profile
    }

    override suspend fun getDashboard(patientId: String): Result<PatientDashboard> = runCatching {
        val appts = client.get {
            url("$baseUrl/api/appointments")
            parameter("status", "CONFIRMED")
        }.body<ApiEnvelope<List<DashboardApptDto>>>().data ?: emptyList()
        val next = appts.firstOrNull()?.toDomain()
        PatientDashboard(upcomingCount = appts.size, recentDoctors = emptyList(), nextAppointment = next)
    }

    override suspend fun deleteAccount(password: String, reason: String?): Result<Unit> = runCatching {
        client.post {
            url("$baseUrl/api/users/me/delete")
            contentType(ContentType.Application.Json)
            setBody(buildMap<String, Any?> {
                put("password", password)
                if (reason != null) put("reason", reason)
            })
        }.body<ApiEnvelope<DeleteAccountResultDto>>()
        Unit
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        val response = client.patch {
            url("$baseUrl/api/users/me/password")
            contentType(ContentType.Application.Json)
            setBody(ChangePasswordRequestDto(currentPassword, newPassword))
        }
        when {
            response.status.value in 200..299 -> Unit
            response.status.value == 400 -> {
                val body = response.bodyAsText()
                if (body.contains("INVALID_CREDENTIALS")) {
                    error("INVALID_CREDENTIALS")
                } else {
                    error("Error al cambiar contraseña: ${response.status}")
                }
            }
            else -> error("Error al cambiar contraseña: ${response.status}")
        }
    }
}

@Serializable
private data class DeleteAccountResultDto(
    val success: Boolean = true,
    val deletedAt: String? = null,
    val note: String? = null,
)

@Serializable
private data class ChangePasswordRequestDto(
    val currentPassword: String,
    val newPassword: String,
)
