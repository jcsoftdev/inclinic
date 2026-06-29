package com.inclinic.app.features.admin.infrastructure.remote

import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlinx.serialization.Serializable

// ── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
private data class AdminStatsDto(
    val pendingDoctors: Int = 0,
    val activeDoctors: Int = 0,
    val suspendedUsers: Int = 0,
    val totalPatients: Int = 0,
    val appointmentsToday: Int = 0,
    val pendingDisputes: Int = 0,
    val pendingSpecialtyRequests: Int = 0,
    val pendingShareRequests: Int = 0,
    val monthRevenue: Double = 0.0,
    val blockedEmails: Int = 0,
    val noShowAppointments: Int = 0,
)

@Serializable
private data class AdminPersonDto(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
)

@Serializable
private data class AdminPersonWrapperDto(
    val id: String = "",
    val user: AdminPersonDto = AdminPersonDto(),
)

@Serializable
private data class AdminSpecialtyDto(
    val id: String = "",
    val name: String = "",
)

@Serializable
private data class AdminAppointmentListItemDto(
    val id: String = "",
    val status: String = "",
    val startTime: String = "",
    val price: Double = 0.0,
    val commission: Double? = null,
    val disputeStatus: String? = null,
    val paymentStatus: String = "",
    val paymentHoldStatus: String? = null,
    val doctor: AdminPersonWrapperDto = AdminPersonWrapperDto(),
    val patient: AdminPersonWrapperDto = AdminPersonWrapperDto(),
    val specialty: AdminSpecialtyDto = AdminSpecialtyDto(),
)

@Serializable
private data class AdminAppointmentDetailDto(
    val id: String = "",
    val status: String = "",
    val startTime: String = "",
    val price: Double = 0.0,
    val commission: Double? = null,
    val disputeStatus: String? = null,
    val disputeReason: String? = null,
    val paymentStatus: String = "",
    val paymentHoldStatus: String? = null,
    val notes: String? = null,
    val rescheduleCount: Int = 0,
    val doctor: AdminPersonWrapperDto = AdminPersonWrapperDto(),
    val patient: AdminPersonWrapperDto = AdminPersonWrapperDto(),
    val specialty: AdminSpecialtyDto = AdminSpecialtyDto(),
)

// ── Doctor DTOs ───────────────────────────────────────────────────────────────

@Serializable
private data class AdminDoctorUserDto(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String? = null,
    val isSuspended: Boolean = false,
    val suspendedAt: String? = null,
    val suspensionReason: String? = null,
    val lastLogin: String? = null,
    val createdAt: String? = null,
)

@Serializable
private data class AdminDoctorSpecialtyWrapperDto(
    val specialty: AdminDoctorSpecialtyNameDto = AdminDoctorSpecialtyNameDto(),
)

@Serializable
private data class AdminDoctorSpecialtyNameDto(
    val name: String = "",
)

@Serializable
private data class AdminDoctorCountDto(
    val appointments: Int = 0,
)

@Serializable
private data class AdminDoctorListItemDto(
    val id: String = "",
    val isActive: Boolean = false,
    val createdAt: String? = null,
    val user: AdminDoctorUserDto = AdminDoctorUserDto(),
    val specialties: List<AdminDoctorSpecialtyWrapperDto> = emptyList(),
    val _count: AdminDoctorCountDto = AdminDoctorCountDto(),
)

@Serializable
private data class AdminDoctorDetailDto(
    val id: String = "",
    val isActive: Boolean = false,
    val isFreelance: Boolean? = null,
    val cmpNumber: String? = null,
    val bio: String? = null,
    val rating: Double? = null,
    val reviewCount: Int? = null,
    val createdAt: String? = null,
    val user: AdminDoctorUserDto = AdminDoctorUserDto(),
    val specialties: List<AdminDoctorSpecialtyWrapperDto> = emptyList(),
    val _count: AdminDoctorCountDto? = null,
)

@Serializable
private data class AdminPendingDoctorDto(
    val id: String = "",
    val createdAt: String? = null,
    val cmpNumber: String? = null,
    val bio: String? = null,
    val isFreelance: Boolean? = null,
    val isActive: Boolean = false,
    val user: AdminDoctorUserDto = AdminDoctorUserDto(),
    val specialties: List<AdminDoctorSpecialtyWrapperDto> = emptyList(),
    val documents: List<AdminDoctorDocumentDto>? = null,
)

@Serializable
private data class AdminDoctorDocumentDto(
    val id: String = "",
    val type: String? = null,
)

@Serializable
private data class RejectDoctorBodyDto(val rejectionReason: String)

// ── Doctor mapping helpers ────────────────────────────────────────────────────

private fun AdminDoctorUserDto.toDomain() = AdminDoctorUser(
    id = id,
    firstName = firstName,
    lastName = lastName,
    email = email,
    phone = phone,
    isSuspended = isSuspended,
    suspendedAt = suspendedAt,
    suspensionReason = suspensionReason,
    lastLogin = lastLogin,
    createdAt = createdAt,
)

private fun AdminDoctorSpecialtyWrapperDto.toDomain() = AdminDoctorSpecialty(name = specialty.name)

// ── Dispute DTOs ──────────────────────────────────────────────────────────────

@Serializable
private data class AdminDisputePaymentDto(
    val gatewayPaymentId: String? = null,
    val status: String = "",
)

@Serializable
private data class AdminDisputeItemDto(
    val id: String = "",
    val status: String = "",
    val startTime: String = "",
    val price: Double = 0.0,
    val disputeStatus: String? = null,
    val disputeReason: String? = null,
    val doctor: AdminPersonWrapperDto = AdminPersonWrapperDto(),
    val patient: AdminPersonWrapperDto = AdminPersonWrapperDto(),
    val specialty: AdminSpecialtyDto = AdminSpecialtyDto(),
    val payment: AdminDisputePaymentDto? = null,
)

@Serializable
private data class ResolveDisputeBodyDto(
    val resolution: String,
    val resolutionNote: String,
)

// ── No-Show DTOs ──────────────────────────────────────────────────────────────

@Serializable
private data class AdminNoShowItemDto(
    val id: String = "",
    val startTime: String = "",
    val price: Double = 0.0,
    val doctor: AdminPersonWrapperDto = AdminPersonWrapperDto(),
    val patient: AdminPersonWrapperDto = AdminPersonWrapperDto(),
    val specialty: AdminSpecialtyDto = AdminSpecialtyDto(),
)

@Serializable
private data class ResolveNoShowBodyDto(
    val resolution: String,
    val note: String,
)

// ── Specialty Catalog DTOs ────────────────────────────────────────────────────

@Serializable
private data class SpecialtyCatalogDto(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val icon: String? = null,
    val isActive: Boolean = true,
)

private fun SpecialtyCatalogDto.toDomain() = AdminSpecialtyItem(
    id = id,
    name = name,
    description = description,
    icon = icon,
    isActive = isActive,
)

@Serializable
private data class CreateSpecialtyBodyDto(
    val name: String,
    val description: String? = null,
    val icon: String? = null,
)

// ── Specialty Request DTOs ────────────────────────────────────────────────────

@Serializable
private data class SpecialtyRequestDoctorUserDto(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
)

@Serializable
private data class SpecialtyRequestDoctorDto(
    val user: SpecialtyRequestDoctorUserDto = SpecialtyRequestDoctorUserDto(),
)

@Serializable
private data class SpecialtyRequestSpecialtyDto(
    val id: String = "",
    val name: String = "",
)

@Serializable
private data class SpecialtyRequestDto(
    val id: String = "",
    val status: String = "",
    val comment: String? = null,
    val createdAt: String? = null,
    val specialtyId: String = "",
    val specialty: SpecialtyRequestSpecialtyDto = SpecialtyRequestSpecialtyDto(),
    val doctor: SpecialtyRequestDoctorDto = SpecialtyRequestDoctorDto(),
)

private fun SpecialtyRequestDto.toDomain() = AdminSpecialtyRequestItem(
    id = id,
    status = status,
    comment = comment,
    createdAt = createdAt,
    specialtyId = specialtyId,
    specialtyName = specialty.name,
    doctorFirstName = doctor.user.firstName,
    doctorLastName = doctor.user.lastName,
    doctorEmail = doctor.user.email,
)

@Serializable
private data class ResolveSpecialtyRequestBodyDto(
    val requestId: String,
    val action: String,
    val reason: String? = null,
)

// ── Patient DTOs ──────────────────────────────────────────────────────────────

@Serializable
private data class AdminPatientUserDto(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String? = null,
    val isSuspended: Boolean = false,
    val suspendedAt: String? = null,
    val suspensionReason: String? = null,
    val subscriptionTier: String = "FREE",
    val lastLogin: String? = null,
    val createdAt: String? = null,
)

@Serializable
private data class AdminPatientCountDto(
    val appointments: Int = 0,
    val therapyPackages: Int = 0,
)

@Serializable
private data class AdminPatientListItemDto(
    val id: String = "",
    val user: AdminPatientUserDto = AdminPatientUserDto(),
    val _count: AdminPatientCountDto = AdminPatientCountDto(),
)

@Serializable
private data class SuspendUserBodyDto(val reason: String)

// ── Report DTOs ───────────────────────────────────────────────────────────────

@Serializable
private data class AdminReportUserDto(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val role: String = "",
)

@Serializable
private data class AdminReportItemDto(
    val id: String = "",
    val status: String = "PENDING",
    val category: String = "other",
    val reason: String = "",
    val createdAt: String? = null,
    val reportedUser: AdminReportUserDto = AdminReportUserDto(),
    val reporter: AdminReportUserDto? = null,
)

@Serializable
private data class ResolveReportBodyDto(
    val status: String,
    val adminNote: String? = null,
)

// ── Review DTOs ───────────────────────────────────────────────────────────────

@Serializable
private data class AdminReviewPersonWrapperDto(
    val id: String = "",
    val user: AdminReviewPersonUserDto = AdminReviewPersonUserDto(),
)

@Serializable
private data class AdminReviewPersonUserDto(
    val firstName: String = "",
    val lastName: String = "",
)

@Serializable
private data class AdminReviewItemDto(
    val id: String = "",
    val rating: Int? = null,
    val ratingComment: String? = null,
    val reviewHiddenAt: String? = null,
    val reviewHiddenReason: String? = null,
    val startTime: String? = null,
    val doctor: AdminReviewPersonWrapperDto = AdminReviewPersonWrapperDto(),
    val patient: AdminReviewPersonWrapperDto = AdminReviewPersonWrapperDto(),
)

@Serializable
private data class HideReviewBodyDto(val reason: String)

// ── Blocked-email DTOs ────────────────────────────────────────────────────────

@Serializable
private data class AdminBlockedEmailDto(
    val id: String = "",
    val email: String = "",
    val reason: String = "",
    val blockedAt: String? = null,
    val expiresAt: String? = null,
    val blockedBy: String? = null,
)

@Serializable
private data class BlockEmailBodyDto(
    val email: String,
    val reason: String,
    val durationDays: Int? = null,
)

private fun AdminBlockedEmailDto.toDomain() = AdminBlockedEmailItem(
    id = id,
    email = email,
    reason = reason,
    blockedAt = blockedAt,
    expiresAt = expiresAt,
    blockedBy = blockedBy,
)

private fun AdminReportUserDto.toDomain() = AdminReportUser(
    id = id,
    firstName = firstName,
    lastName = lastName,
    email = email,
    role = role,
)

private fun AdminReportItemDto.toDomain() = AdminReportItem(
    id = id,
    status = status,
    category = category,
    reason = reason,
    createdAt = createdAt,
    reportedUser = reportedUser.toDomain(),
    reporter = reporter?.toDomain(),
)

private fun AdminPatientListItemDto.toDomain() = AdminPatientListItem(
    id = id,
    userId = user.id,
    firstName = user.firstName,
    lastName = user.lastName,
    email = user.email,
    phone = user.phone,
    isSuspended = user.isSuspended,
    suspendedAt = user.suspendedAt,
    suspensionReason = user.suspensionReason,
    subscriptionTier = user.subscriptionTier,
    lastLogin = user.lastLogin,
    createdAt = user.createdAt,
    appointmentCount = _count.appointments,
    therapyPackageCount = _count.therapyPackages,
)

// ── Finance DTOs ──────────────────────────────────────────────────────────────

@Serializable
private data class AdminFinancePeriodDto(
    val revenue: Double = 0.0,
    val commission: Double = 0.0,
    val appointments: Int = 0,
)

@Serializable
private data class AdminFinanceTotalReleasedDto(
    val revenue: Double = 0.0,
    val commission: Double = 0.0,
)

@Serializable
private data class AdminFinanceHeldDto(
    val total: Double = 0.0,
    val count: Int = 0,
)

@Serializable
private data class AdminFinanceRefundedDto(
    val total: Double = 0.0,
    val count: Int = 0,
)

@Serializable
private data class AdminTopDoctorDto(
    val doctorId: String = "",
    val name: String = "",
    val appointments: Int = 0,
    val totalRevenue: Double = 0.0,
    val doctorEarnings: Double = 0.0,
)

@Serializable
private data class AdminFinanceDto(
    val thisMonth: AdminFinancePeriodDto = AdminFinancePeriodDto(),
    val last30Days: AdminFinancePeriodDto = AdminFinancePeriodDto(),
    val last7Days: AdminFinancePeriodDto = AdminFinancePeriodDto(),
    val totalReleased: AdminFinanceTotalReleasedDto = AdminFinanceTotalReleasedDto(),
    val held: AdminFinanceHeldDto = AdminFinanceHeldDto(),
    val refunded: AdminFinanceRefundedDto = AdminFinanceRefundedDto(),
    val topDoctors: List<AdminTopDoctorDto> = emptyList(),
)

// ── Dispute + No-Show mapping helpers ─────────────────────────────────────────

private fun AdminDisputeItemDto.toDomain() = AdminDisputeItem(
    id = id,
    status = status,
    startTime = startTime,
    price = price,
    disputeStatus = disputeStatus,
    disputeReason = disputeReason,
    doctor = doctor.toDomain(),
    patient = patient.toDomain(),
    specialty = specialty.toDomain(),
    paymentGatewayId = payment?.gatewayPaymentId,
    paymentStatus = payment?.status ?: "",
)

private fun AdminNoShowItemDto.toDomain() = AdminNoShowItem(
    id = id,
    startTime = startTime,
    price = price,
    doctor = doctor.toDomain(),
    patient = patient.toDomain(),
    specialty = specialty.toDomain(),
)

// ── Appointment mapping helpers ───────────────────────────────────────────────

private fun AdminPersonWrapperDto.toDomain() = AdminAppointmentPerson(
    id = id,
    firstName = user.firstName,
    lastName = user.lastName,
    email = user.email,
)

private fun AdminSpecialtyDto.toDomain() = AdminAppointmentSpecialty(id = id, name = name)

// ── 2FA DTOs ──────────────────────────────────────────────────────────────────

@Serializable
private data class TwoFactorStatusDto(
    val enabled: Boolean = false,
    val verifiedAt: String? = null,
    val enforced: Boolean = false,
)

@Serializable
private data class TwoFactorSetupDto(
    val secret: String = "",
    val provisioningUrl: String = "",
)

@Serializable
private data class TwoFactorCodeBodyDto(val code: String)

// ── Subscription DTOs ─────────────────────────────────────────────────────────

@Serializable
private data class AdminSubscriptionStatsDto(
    val activeCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val expiredCount: Int = 0,
)

@Serializable
private data class AdminSubscriptionItemDto(
    val userId: String = "",
    val doctorId: String? = null,
    val name: String = "",
    val tier: String = "FREE",
    val expiresAt: String? = null,
    val status: String = "ACTIVE",
)

@Serializable
private data class AdminSubscriptionsOverviewDto(
    val stats: AdminSubscriptionStatsDto = AdminSubscriptionStatsDto(),
    val items: List<AdminSubscriptionItemDto> = emptyList(),
)

@Serializable
private data class SetSubscriptionBodyDto(
    val tier: String,
    val expiresAt: String? = null,
)

private fun AdminSubscriptionItemDto.toDomain() = AdminSubscriptionItem(
    userId = userId,
    doctorId = doctorId,
    name = name,
    tier = tier,
    expiresAt = expiresAt,
    status = when (status) {
        "EXPIRING" -> SubscriptionStatus.EXPIRING
        "EXPIRED"  -> SubscriptionStatus.EXPIRED
        else       -> SubscriptionStatus.ACTIVE
    },
)

private fun AdminSubscriptionsOverviewDto.toDomain() = AdminSubscriptionsOverview(
    stats = AdminSubscriptionStats(
        activeCount = stats.activeCount,
        expiringSoonCount = stats.expiringSoonCount,
        expiredCount = stats.expiredCount,
    ),
    items = items.map { it.toDomain() },
)

// ── DataSource implementation ─────────────────────────────────────────────────

class KtorAdminDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : AdminDataSource {

    // ── 2FA ───────────────────────────────────────────────────────────────────

    override suspend fun getTwoFactorStatus(): Result<TwoFactorStatus> = runCatching {
        val dto = client.get {
            url("$baseUrl/api/auth/2fa/status")
        }.body<ApiEnvelope<TwoFactorStatusDto>>().data ?: TwoFactorStatusDto()
        TwoFactorStatus(enabled = dto.enabled, verifiedAt = dto.verifiedAt, enforced = dto.enforced)
    }

    override suspend fun setupTwoFactor(): Result<TwoFactorSetup> = runCatching {
        val dto = client.post {
            url("$baseUrl/api/auth/2fa/setup")
            contentType(ContentType.Application.Json)
        }.body<ApiEnvelope<TwoFactorSetupDto>>().data ?: error("2FA setup data missing")
        TwoFactorSetup(secret = dto.secret, provisioningUrl = dto.provisioningUrl)
    }

    override suspend fun enableTwoFactor(code: String): Result<Unit> = runCatching {
        client.post {
            url("$baseUrl/api/auth/2fa/enable")
            contentType(ContentType.Application.Json)
            setBody(TwoFactorCodeBodyDto(code = code))
        }
        Unit
    }

    override suspend fun disableTwoFactor(code: String): Result<Unit> = runCatching {
        client.post {
            url("$baseUrl/api/auth/2fa/disable")
            contentType(ContentType.Application.Json)
            setBody(TwoFactorCodeBodyDto(code = code))
        }
        Unit
    }

    override suspend fun getFinance(): Result<AdminFinance> = runCatching {
        val dto = client.get {
            url("$baseUrl/api/admin/finance")
        }.body<ApiEnvelope<AdminFinanceDto>>().data ?: error("Finance data missing")
        AdminFinance(
            thisMonth = AdminFinancePeriod(dto.thisMonth.revenue, dto.thisMonth.commission, dto.thisMonth.appointments),
            last30Days = AdminFinancePeriod(dto.last30Days.revenue, dto.last30Days.commission, dto.last30Days.appointments),
            last7Days = AdminFinancePeriod(dto.last7Days.revenue, dto.last7Days.commission, dto.last7Days.appointments),
            totalReleasedRevenue = dto.totalReleased.revenue,
            totalReleasedCommission = dto.totalReleased.commission,
            held = AdminFinanceHeld(total = dto.held.total, count = dto.held.count),
            refunded = AdminFinanceRefunded(total = dto.refunded.total, count = dto.refunded.count),
            topDoctors = dto.topDoctors.map { d ->
                AdminTopDoctor(
                    doctorId = d.doctorId,
                    name = d.name,
                    appointments = d.appointments,
                    totalRevenue = d.totalRevenue,
                    doctorEarnings = d.doctorEarnings,
                )
            },
        )
    }

    override suspend fun getDashboard(): Result<AdminDashboard> = runCatching {
        val dto = client.get {
            url("$baseUrl/api/admin/stats")
        }.body<ApiEnvelope<AdminStatsDto>>().data ?: error("Admin stats data missing")
        AdminDashboard(
            pendingDoctors = dto.pendingDoctors,
            activeDoctors = dto.activeDoctors,
            suspendedUsers = dto.suspendedUsers,
            totalPatients = dto.totalPatients,
            appointmentsToday = dto.appointmentsToday,
            pendingDisputes = dto.pendingDisputes,
            pendingSpecialtyRequests = dto.pendingSpecialtyRequests,
            pendingShareRequests = dto.pendingShareRequests,
            monthRevenue = dto.monthRevenue,
            blockedEmails = dto.blockedEmails,
            noShowAppointments = dto.noShowAppointments,
        )
    }

    override suspend fun getAppointments(filters: AdminAppointmentFilters): Result<List<AdminAppointmentListItem>> =
        runCatching {
            val dtos = client.get {
                url("$baseUrl/api/admin/appointments-list")
                filters.status?.let { parameter("status", it) }
                filters.from?.let { parameter("from", it) }
                filters.to?.let { parameter("to", it) }
                filters.doctorId?.let { parameter("doctorId", it) }
                filters.patientId?.let { parameter("patientId", it) }
                filters.q?.let { parameter("q", it) }
                filters.hasDispute?.let { parameter("hasDispute", it.toString()) }
            }.body<ApiEnvelope<List<AdminAppointmentListItemDto>>>().data
                ?: error("Appointment list data missing")

            dtos.map { dto ->
                AdminAppointmentListItem(
                    id = dto.id,
                    status = dto.status,
                    startTime = dto.startTime,
                    price = dto.price,
                    commission = dto.commission,
                    disputeStatus = dto.disputeStatus,
                    paymentStatus = dto.paymentStatus,
                    paymentHoldStatus = dto.paymentHoldStatus,
                    doctor = dto.doctor.toDomain(),
                    patient = dto.patient.toDomain(),
                    specialty = dto.specialty.toDomain(),
                )
            }
        }

    override suspend fun getAppointmentDetail(id: String): Result<AdminAppointmentDetail> =
        runCatching {
            val dto = client.get {
                url("$baseUrl/api/appointments/$id")
            }.body<ApiEnvelope<AdminAppointmentDetailDto>>().data
                ?: error("Appointment detail data missing")

            AdminAppointmentDetail(
                id = dto.id,
                status = dto.status,
                startTime = dto.startTime,
                price = dto.price,
                commission = dto.commission,
                disputeStatus = dto.disputeStatus,
                disputeReason = dto.disputeReason,
                paymentStatus = dto.paymentStatus,
                paymentHoldStatus = dto.paymentHoldStatus,
                notes = dto.notes,
                rescheduleCount = dto.rescheduleCount,
                doctor = dto.doctor.toDomain(),
                patient = dto.patient.toDomain(),
                specialty = dto.specialty.toDomain(),
            )
        }

    // ── Doctors ──────────────────────────────────────────────────────────────

    override suspend fun getDoctors(status: String?, q: String?): Result<List<AdminDoctorListItem>> =
        runCatching {
            val dtos = client.get {
                url("$baseUrl/api/admin/doctors-list")
                status?.let { parameter("status", it) }
                q?.let { parameter("q", it) }
            }.body<ApiEnvelope<List<AdminDoctorListItemDto>>>().data
                ?: error("Doctors list data missing")

            dtos.map { dto ->
                AdminDoctorListItem(
                    id = dto.id,
                    isActive = dto.isActive,
                    createdAt = dto.createdAt,
                    user = dto.user.toDomain(),
                    specialties = dto.specialties.map { it.toDomain() },
                    appointmentCount = dto._count.appointments,
                )
            }
        }

    override suspend fun getPendingDoctors(): Result<List<AdminPendingDoctor>> =
        runCatching {
            val dtos = client.get {
                url("$baseUrl/api/doctors/pending")
            }.body<ApiEnvelope<List<AdminPendingDoctorDto>>>().data
                ?: error("Pending doctors data missing")

            dtos.map { dto ->
                AdminPendingDoctor(
                    id = dto.id,
                    createdAt = dto.createdAt,
                    cmpNumber = dto.cmpNumber,
                    bio = dto.bio,
                    user = dto.user.toDomain(),
                    specialties = dto.specialties.map { it.toDomain() },
                    documentCount = dto.documents?.size ?: 0,
                )
            }
        }

    override suspend fun getDoctorDetail(id: String): Result<AdminDoctorDetail> =
        runCatching {
            val dto = client.get {
                url("$baseUrl/api/doctors/$id")
            }.body<ApiEnvelope<AdminDoctorDetailDto>>().data
                ?: error("Doctor detail data missing")

            AdminDoctorDetail(
                id = dto.id,
                isActive = dto.isActive,
                isFreelance = dto.isFreelance,
                cmpNumber = dto.cmpNumber,
                bio = dto.bio,
                rating = dto.rating,
                reviewCount = dto.reviewCount,
                appointmentCount = dto._count?.appointments,
                createdAt = dto.createdAt,
                user = dto.user.toDomain(),
                specialties = dto.specialties.map { it.toDomain() },
            )
        }

    override suspend fun approveDoctor(id: String): Result<Unit> =
        runCatching {
            client.post {
                url("$baseUrl/api/doctors/$id/approve")
                contentType(ContentType.Application.Json)
            }
            Unit
        }

    override suspend fun rejectDoctor(id: String, reason: String): Result<Unit> =
        runCatching {
            client.post {
                url("$baseUrl/api/doctors/$id/reject")
                contentType(ContentType.Application.Json)
                setBody(RejectDoctorBodyDto(rejectionReason = reason))
            }
            Unit
        }

    // ── Disputes ─────────────────────────────────────────────────────────────

    override suspend fun getDisputes(status: String?): Result<List<AdminDisputeItem>> =
        runCatching {
            val dtos = client.get {
                url("$baseUrl/api/admin/disputes")
                status?.let { parameter("status", it) }
            }.body<ApiEnvelope<List<AdminDisputeItemDto>>>().data
                ?: error("Disputes data missing")
            dtos.map { it.toDomain() }
        }

    override suspend fun resolveDispute(id: String, resolution: String, resolutionNote: String): Result<Unit> =
        runCatching {
            client.put {
                url("$baseUrl/api/appointments/$id/dispute")
                contentType(ContentType.Application.Json)
                setBody(ResolveDisputeBodyDto(resolution = resolution, resolutionNote = resolutionNote))
            }
            Unit
        }

    // ── No-Shows ─────────────────────────────────────────────────────────────

    override suspend fun getNoShows(): Result<List<AdminNoShowItem>> =
        runCatching {
            val dtos = client.get {
                url("$baseUrl/api/admin/no-shows")
            }.body<ApiEnvelope<List<AdminNoShowItemDto>>>().data
                ?: error("No-shows data missing")
            dtos.map { it.toDomain() }
        }

    override suspend fun resolveNoShow(id: String, resolution: String, note: String): Result<Unit> =
        runCatching {
            client.put {
                url("$baseUrl/api/admin/no-shows/$id")
                contentType(ContentType.Application.Json)
                setBody(ResolveNoShowBodyDto(resolution = resolution, note = note))
            }
            Unit
        }

    // ── Specialties ───────────────────────────────────────────────────────────

    override suspend fun getSpecialties(): Result<List<AdminSpecialtyItem>> =
        runCatching {
            val dtos = client.get {
                url("$baseUrl/api/specialties")
            }.body<ApiEnvelope<List<SpecialtyCatalogDto>>>().data
                ?: error("Specialties data missing")
            dtos.map { it.toDomain() }
        }

    override suspend fun createSpecialty(
        name: String,
        description: String?,
        icon: String?,
    ): Result<AdminSpecialtyItem> =
        runCatching {
            val dto = client.post {
                url("$baseUrl/api/specialties")
                contentType(ContentType.Application.Json)
                setBody(CreateSpecialtyBodyDto(name = name, description = description, icon = icon))
            }.body<ApiEnvelope<SpecialtyCatalogDto>>().data
                ?: error("Create specialty data missing")
            dto.toDomain()
        }

    override suspend fun getSpecialtyRequests(): Result<List<AdminSpecialtyRequestItem>> =
        runCatching {
            val dtos = client.get {
                url("$baseUrl/api/specialties/request")
            }.body<ApiEnvelope<List<SpecialtyRequestDto>>>().data
                ?: error("Specialty requests data missing")
            dtos.map { it.toDomain() }
        }

    override suspend fun resolveSpecialtyRequest(
        requestId: String,
        action: String,
        reason: String?,
    ): Result<Unit> =
        runCatching {
            client.put {
                url("$baseUrl/api/specialties/request")
                contentType(ContentType.Application.Json)
                setBody(ResolveSpecialtyRequestBodyDto(requestId = requestId, action = action, reason = reason))
            }
            Unit
        }

    // ── Patients ─────────────────────────────────────────────────────────────

    override suspend fun getPatients(status: String?, q: String?): Result<List<AdminPatientListItem>> =
        runCatching {
            val dtos = client.get {
                url("$baseUrl/api/admin/patients-list")
                status?.let { parameter("status", it) }
                q?.let { parameter("q", it) }
            }.body<ApiEnvelope<List<AdminPatientListItemDto>>>().data
                ?: error("Patients list data missing")
            dtos.map { it.toDomain() }
        }

    override suspend fun suspendUser(userId: String, reason: String): Result<Unit> =
        runCatching {
            client.post {
                url("$baseUrl/api/admin/users/$userId/suspend")
                contentType(ContentType.Application.Json)
                setBody(SuspendUserBodyDto(reason = reason))
            }
            Unit
        }

    override suspend fun unsuspendUser(userId: String): Result<Unit> =
        runCatching {
            client.delete {
                url("$baseUrl/api/admin/users/$userId/suspend")
            }
            Unit
        }

    // ── Reports ───────────────────────────────────────────────────────────────

    override suspend fun getReports(status: String?): Result<List<AdminReportItem>> =
        runCatching {
            val dtos = client.get {
                url("$baseUrl/api/moderation/reports")
                // Pass explicit status or "ALL" to get everything; backend defaults to PENDING if null
                status?.let { parameter("status", it) }
            }.body<ApiEnvelope<List<AdminReportItemDto>>>().data
                ?: error("Reports data missing")
            dtos.map { it.toDomain() }
        }

    override suspend fun resolveReport(
        reportId: String,
        status: String,
        adminNote: String?,
    ): Result<Unit> =
        runCatching {
            client.post {
                url("$baseUrl/api/moderation/reports/$reportId/resolve")
                contentType(ContentType.Application.Json)
                setBody(ResolveReportBodyDto(status = status, adminNote = adminNote))
            }
            Unit
        }

    // ── Reviews ───────────────────────────────────────────────────────────────

    override suspend fun getReviews(
        withComment: Boolean?,
        hidden: Boolean?,
    ): Result<List<AdminReviewItem>> =
        runCatching {
            val dtos = client.get {
                url("$baseUrl/api/admin/reviews")
                withComment?.let { parameter("withComment", it.toString()) }
                hidden?.let { parameter("hidden", it.toString()) }
            }.body<ApiEnvelope<List<AdminReviewItemDto>>>().data
                ?: error("Reviews data missing")

            dtos.mapNotNull { dto ->
                val rating = dto.rating ?: return@mapNotNull null
                AdminReviewItem(
                    appointmentId = dto.id,
                    rating = rating,
                    comment = dto.ratingComment,
                    reviewHiddenAt = dto.reviewHiddenAt,
                    reviewHiddenReason = dto.reviewHiddenReason,
                    startTime = dto.startTime,
                    patient = AdminReviewPerson(
                        firstName = dto.patient.user.firstName,
                        lastName = dto.patient.user.lastName,
                    ),
                    doctor = AdminReviewPerson(
                        firstName = dto.doctor.user.firstName,
                        lastName = dto.doctor.user.lastName,
                    ),
                )
            }
        }

    override suspend fun hideReview(appointmentId: String, reason: String): Result<Unit> =
        runCatching {
            client.post {
                url("$baseUrl/api/admin/reviews/$appointmentId/hide")
                contentType(ContentType.Application.Json)
                setBody(HideReviewBodyDto(reason = reason))
            }
            Unit
        }

    override suspend fun unhideReview(appointmentId: String): Result<Unit> =
        runCatching {
            client.delete {
                url("$baseUrl/api/admin/reviews/$appointmentId/hide")
            }
            Unit
        }

    // ── Blocked emails ────────────────────────────────────────────────────────

    override suspend fun getBlockedEmails(): Result<List<AdminBlockedEmailItem>> =
        runCatching {
            val dtos = client.get {
                url("$baseUrl/api/admin/blocked-emails")
            }.body<ApiEnvelope<List<AdminBlockedEmailDto>>>().data
                ?: error("Blocked emails data missing")
            dtos.map { it.toDomain() }
        }

    override suspend fun blockEmail(email: String, reason: String, durationDays: Int?): Result<Unit> =
        runCatching {
            client.post {
                url("$baseUrl/api/admin/blocked-emails")
                contentType(ContentType.Application.Json)
                setBody(BlockEmailBodyDto(email = email, reason = reason, durationDays = durationDays))
            }
            Unit
        }

    override suspend fun unblockEmail(email: String): Result<Unit> =
        runCatching {
            // email used as path param — encodeURLPathPart handles special chars (e.g. "@", ".")
            val encoded = email.encodeURLPathPart()
            client.delete {
                url("$baseUrl/api/admin/blocked-emails/$encoded")
            }
            Unit
        }

    // ── Subscriptions ─────────────────────────────────────────────────────────

    override suspend fun getSubscriptions(): Result<AdminSubscriptionsOverview> =
        runCatching {
            val dto = client.get {
                url("$baseUrl/api/admin/subscriptions")
            }.body<ApiEnvelope<AdminSubscriptionsOverviewDto>>().data
                ?: error("Subscriptions data missing")
            dto.toDomain()
        }

    override suspend fun setUserSubscription(
        userId: String,
        tier: String,
        expiresAt: String?,
    ): Result<Unit> =
        runCatching {
            client.put {
                url("$baseUrl/api/admin/users/$userId/subscription")
                contentType(ContentType.Application.Json)
                setBody(SetSubscriptionBodyDto(tier = tier, expiresAt = expiresAt))
            }
            Unit
        }
}
