package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.AvailabilitySlot
import com.inclinic.app.core.model.PaymentResult
import com.inclinic.app.features.patient.payment.application.PaymentDeadlineExpiredException
import com.inclinic.app.core.model.RescheduleProposal
import com.inclinic.app.core.model.RescheduleStatus
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.core.network.ApiEnvelope
import kotlin.time.Clock
import kotlin.time.Instant
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
private data class ErrorEnvelopeDto(val error: String? = null, val message: String? = null, val code: String? = null)

@Serializable
private data class AppointmentUserDto(
    val firstName: String? = null,
    val lastName: String? = null,
)

@Serializable
private data class AppointmentDoctorDto(
    val user: AppointmentUserDto? = null,
)

@Serializable
private data class AppointmentSpecialtyDto(
    val name: String? = null,
)

@Serializable
private data class AppointmentDto(
    val id: String,
    val doctorId: String,
    val patientId: String,
    val specialtyId: String? = null,
    val scheduleId: String? = null,
    val type: String = "CONSULTATION",
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
    val doctor: AppointmentDoctorDto? = null,
    val specialty: AppointmentSpecialtyDto? = null,
    val rescheduleRequests: List<RescheduleRequestMinDto>? = null,
    val needsClosure: Boolean = false,
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
                isHomeVisit    -> VisitType.HOME
                else           -> VisitType.CLINIC
            },
            status = when (status.trim().uppercase()) {
                "CANCELLED", "CANCELED" -> AppointmentStatus.CANCELLED_BY_PATIENT
                else -> runCatching { AppointmentStatus.valueOf(status.trim().uppercase()) }
                    .getOrDefault(AppointmentStatus.PENDING_PAYMENT)
            },
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
            needsClosure = needsClosure,
        )
    }
}

@Serializable
private data class RescheduleRequestMinDto(
    val id: String = "",
    val status: String = "PENDING",
)

@Serializable
private data class SlotDto(
    val time: String,
    val available: Boolean,
) {
    fun toDomain() = AvailabilitySlot(
        id = time,
        startTime = time,
        endTime = time,
        isAvailable = available,
    )
}

@Serializable
private data class AvailabilityResponseDto(
    val slots: List<SlotDto> = emptyList(),
    val schedule: JsonObject? = null,
)

@Serializable
private data class CreateAppointmentBody(
    val doctorId: String,
    val appointmentDate: String,
    val appointmentTime: String,
    val isHomeVisit: Boolean,
    val isTelemedicine: Boolean,
    val notes: String = "",
)

@Serializable
private data class DayInfoDto(
    val level: String,
    val available: Int = 0,
    val total: Int = 0,
)

class KtorAppointmentDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : AppointmentDataSource {

    override suspend fun getAvailability(doctorId: String, date: String): Result<List<AvailabilitySlot>> = runCatching {
        client.get {
            url("$baseUrl/api/appointments/availability")
            parameter("doctorId", doctorId)
            parameter("date", date)
        }.body<ApiEnvelope<AvailabilityResponseDto>>().data?.slots?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun getMonthAvailability(doctorId: String, month: String): Result<Map<String, String>> = runCatching {
        client.get {
            url("$baseUrl/api/appointments/availability/month")
            parameter("doctorId", doctorId)
            parameter("month", month)
        }.body<ApiEnvelope<Map<String, DayInfoDto>>>().data
            ?.mapValues { it.value.level } ?: emptyMap()
    }

    override suspend fun createAppointment(
        doctorId: String,
        date: String,
        slotId: String,
        visitType: String,
        notes: String?,
    ): Result<Appointment> = runCatching {
        val response = client.post {
            url("$baseUrl/api/appointments")
            contentType(ContentType.Application.Json)
            setBody(CreateAppointmentBody(
                doctorId = doctorId,
                appointmentDate = date,
                appointmentTime = slotId,
                isHomeVisit = visitType == "HOME",
                isTelemedicine = visitType == "VIRTUAL",
                notes = notes.orEmpty(),
            ))
        }
        if (!response.status.isSuccess()) {
            val err = runCatching { response.body<ErrorEnvelopeDto>() }.getOrNull()
            error(err?.error ?: err?.message ?: "Error ${response.status.value}")
        }
        response.body<ApiEnvelope<AppointmentDto>>().data?.toDomain() ?: error("Appointment creation failed")
    }

    override suspend fun getPatientAppointments(
        patientId: String,
        status: String?,
        page: Int,
    ): Result<List<Appointment>> = runCatching {
        client.get {
            url("$baseUrl/api/appointments")
            parameter("patientId", patientId)
            status?.let { parameter("status", it) }
            parameter("page", page)
        }.body<ApiEnvelope<List<AppointmentDto>>>().data?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> = runCatching {
        client.get {
            url("$baseUrl/api/appointments/$appointmentId")
        }.body<ApiEnvelope<AppointmentDto>>().data?.toDomain() ?: error("Appointment not found")
    }

    override suspend fun cancelAppointment(appointmentId: String, reason: String): Result<Unit> = runCatching {
        client.post {
            url("$baseUrl/api/appointments/$appointmentId/cancel")
            contentType(ContentType.Application.Json)
            setBody(mapOf("reason" to reason))
        }
        Unit
    }

    override suspend fun rescheduleAppointment(
        appointmentId: String,
        date: String,
        slotId: String,
    ): Result<Appointment> = runCatching {
        client.post {
            url("$baseUrl/api/appointments/$appointmentId/reschedule")
            contentType(ContentType.Application.Json)
            setBody(mapOf("appointmentDate" to date, "appointmentTime" to slotId))
        }.body<ApiEnvelope<AppointmentDto>>().data?.toDomain() ?: error("Reschedule failed")
    }

    override suspend fun processPayment(cardToken: String, paymentMethodId: String, appointmentId: String): Result<PaymentResult> = runCatching {
        @Serializable
        data class PaymentResponseDto(
            val success: Boolean = false,
            val appointmentId: String? = null,
            val inProcess: Boolean = false,
            val message: String? = null,
        )
        val response = client.post {
            url("$baseUrl/api/payments/process")
            contentType(ContentType.Application.Json)
            setBody(mapOf("cardToken" to cardToken, "paymentMethodId" to paymentMethodId, "appointmentId" to appointmentId))
        }
        if (!response.status.isSuccess()) {
            val err = runCatching { response.body<ErrorEnvelopeDto>() }.getOrNull()
            val msg = err?.error ?: err?.message ?: "Error al procesar el pago"
            if (err?.code == "PAYMENT_DEADLINE_EXPIRED" || response.status.value == 410) {
                throw PaymentDeadlineExpiredException(msg)
            }
            error(msg)
        }
        val dto = response.body<ApiEnvelope<PaymentResponseDto>>().data ?: error("Respuesta de pago vacía")
        PaymentResult(
            appointmentId = dto.appointmentId ?: appointmentId,
            status = if (dto.inProcess) "in_process" else "approved",
        )
    }

    override suspend fun processPackagePayment(cardToken: String, paymentMethodId: String, therapyPackageId: String): Result<PaymentResult> = runCatching {
        @Serializable
        data class PaymentResponseDto(
            val success: Boolean = false,
            val therapyPackageId: String? = null,
            val inProcess: Boolean = false,
            val message: String? = null,
        )
        val response = client.post {
            url("$baseUrl/api/payments/process")
            contentType(ContentType.Application.Json)
            setBody(mapOf("cardToken" to cardToken, "paymentMethodId" to paymentMethodId, "therapyPackageId" to therapyPackageId))
        }
        if (!response.status.isSuccess()) {
            val err = runCatching { response.body<ErrorEnvelopeDto>() }.getOrNull()
            val msg = err?.error ?: err?.message ?: "Error al procesar el pago"
            if (err?.code == "PAYMENT_DEADLINE_EXPIRED" || response.status.value == 410) {
                throw PaymentDeadlineExpiredException(msg)
            }
            error(msg)
        }
        val dto = response.body<ApiEnvelope<PaymentResponseDto>>().data ?: error("Respuesta de pago vacía")
        PaymentResult(
            appointmentId = dto.therapyPackageId ?: therapyPackageId,
            status = if (dto.inProcess) "in_process" else "approved",
        )
    }

    override suspend fun getPendingRescheduleProposal(appointmentId: String): Result<RescheduleProposal?> = runCatching {
        val response = client.get {
            url("$baseUrl/api/appointments/$appointmentId")
        }.body<ApiEnvelope<AppointmentWithRescheduleDto>>()
        response.data?.pendingReschedule?.toDomain(response.data)
    }

    override suspend fun respondRescheduleProposal(requestId: String, accept: Boolean, responseNote: String?): Result<Unit> = runCatching {
        val response = client.post {
            url("$baseUrl/api/appointments/reschedule-requests/$requestId/respond")
            contentType(ContentType.Application.Json)
            setBody(RespondRescheduleBody(accept = accept, responseNote = responseNote))
        }
        if (!response.status.isSuccess()) {
            val err = runCatching { response.body<ErrorEnvelopeDto>() }.getOrNull()
            error(err?.error ?: err?.message ?: "Error al responder la reagenda")
        }
    }

    override suspend fun disputeAppointment(appointmentId: String, reason: String, details: String): Result<Unit> = runCatching {
        val response = client.post {
            url("$baseUrl/api/appointments/$appointmentId/dispute")
            contentType(ContentType.Application.Json)
            setBody(mapOf("reason" to reason, "details" to details))
        }
        if (!response.status.isSuccess()) {
            val err = runCatching { response.body<ErrorEnvelopeDto>() }.getOrNull()
            error(err?.error ?: err?.message ?: "Error al disputar la cita")
        }
    }

    override suspend fun confirmRating(appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?): Result<Unit> = runCatching {
        // El backend (POST /appointments/{id}/confirm) solo acepta un `rating` único.
        // Colapsamos las 3 dimensiones de la UI al promedio redondeado (1..5).
        val rating = ((punctuality + professionalism + empathy).toDouble() / 3.0)
            .let { kotlin.math.round(it).toInt() }
            .coerceIn(1, 5)
        val response = client.post {
            url("$baseUrl/api/appointments/$appointmentId/confirm")
            contentType(ContentType.Application.Json)
            setBody(buildMap<String, Any> {
                put("rating", rating)
                if (comment != null) put("comment", comment)
            })
        }
        if (!response.status.isSuccess()) {
            val err = runCatching { response.body<ErrorEnvelopeDto>() }.getOrNull()
            error(err?.error ?: err?.message ?: "Error al confirmar la calificación")
        }
    }

    override suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?): Result<Unit> = runCatching {
        // El backend (POST /appointments/{id}/change-request) solo modela un toggle
        // presencial<->domicilio vía `requestedHomeVisit`. No soporta VIRTUAL ni
        // persiste address/reason (campos ignorados server-side por ahora).
        val response = client.post {
            url("$baseUrl/api/appointments/$appointmentId/change-request")
            contentType(ContentType.Application.Json)
            setBody(mapOf("requestedHomeVisit" to (newVisitType == "HOME")))
        }
        if (!response.status.isSuccess()) {
            val err = runCatching { response.body<ErrorEnvelopeDto>() }.getOrNull()
            error(err?.error ?: err?.message ?: "Error al solicitar cambio de tipo de visita")
        }
    }
}

@Serializable
private data class RespondRescheduleBody(val accept: Boolean, val responseNote: String? = null)

@Serializable
private data class RescheduleRequestDto(
    val id: String,
    val appointmentId: String,
    val requestedBy: String = "DOCTOR",
    val proposedStart: String,
    val proposedEnd: String,
    val reason: String? = null,
    val status: String = "PENDING",
    val expiresAt: String,
    val createdAt: String,
) {
    fun toDomain(parent: AppointmentWithRescheduleDto?): RescheduleProposal {
        val now = Clock.System.now()
        return RescheduleProposal(
            id = id,
            appointmentId = appointmentId,
            requestedBy = requestedBy,
            proposedStart = runCatching { Instant.parse(proposedStart) }.getOrElse { now },
            proposedEnd = runCatching { Instant.parse(proposedEnd) }.getOrElse { now },
            reason = reason,
            status = runCatching { RescheduleStatus.valueOf(status) }.getOrDefault(RescheduleStatus.PENDING),
            expiresAt = runCatching { Instant.parse(expiresAt) }.getOrElse { now },
            createdAt = runCatching { Instant.parse(createdAt) }.getOrElse { now },
            doctorName = parent?.let {
                val fn = it.doctor?.user?.firstName
                val ln = it.doctor?.user?.lastName
                listOfNotNull(fn, ln).joinToString(" ").ifBlank { null }
            },
            specialtyName = parent?.specialty?.name,
            originalStart = parent?.startTime?.let { runCatching { Instant.parse(it) }.getOrNull() },
            visitType = when {
                parent?.isTelemedicine == true -> VisitType.VIRTUAL
                parent?.isHomeVisit == true -> VisitType.HOME
                else -> VisitType.CLINIC
            },
        )
    }
}

@Serializable
private data class AppointmentWithRescheduleDto(
    val id: String = "",
    val doctorId: String = "",
    val patientId: String = "",
    val startTime: String? = null,
    val isHomeVisit: Boolean = false,
    val isTelemedicine: Boolean = false,
    val doctor: AppointmentDoctorDto? = null,
    val specialty: AppointmentSpecialtyDto? = null,
    val pendingReschedule: RescheduleRequestDto? = null,
)
