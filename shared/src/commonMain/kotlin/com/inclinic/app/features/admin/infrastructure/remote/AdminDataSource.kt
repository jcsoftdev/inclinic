package com.inclinic.app.features.admin.infrastructure.remote

// ── 2FA domain models ─────────────────────────────────────────────────────────

/**
 * Result of GET /api/auth/2fa/status.
 * [verifiedAt] is an ISO datetime string or null if never verified.
 * [enforced] indicates the organisation requires 2FA for admins.
 */
data class TwoFactorStatus(
    val enabled: Boolean,
    val verifiedAt: String?,
    val enforced: Boolean,
)

/**
 * Result of POST /api/auth/2fa/setup.
 * [secret] is the base32 TOTP secret; [provisioningUrl] is the otpauth:// URL.
 */
data class TwoFactorSetup(
    val secret: String,
    val provisioningUrl: String,
)

interface AdminDataSource {
    // ── 2FA management (SUPER_ADMIN) ──────────────────────────────────────────
    suspend fun getTwoFactorStatus(): Result<TwoFactorStatus>
    suspend fun setupTwoFactor(): Result<TwoFactorSetup>
    suspend fun enableTwoFactor(code: String): Result<Unit>
    suspend fun disableTwoFactor(code: String): Result<Unit>

    suspend fun getDashboard(): Result<AdminDashboard>
    suspend fun getFinance(): Result<AdminFinance>
    suspend fun getAppointments(filters: AdminAppointmentFilters): Result<List<AdminAppointmentListItem>>
    suspend fun getAppointmentDetail(id: String): Result<AdminAppointmentDetail>

    // ── Doctors ──────────────────────────────────────────────────────────────
    suspend fun getDoctors(status: String?, q: String?): Result<List<AdminDoctorListItem>>
    suspend fun getPendingDoctors(): Result<List<AdminPendingDoctor>>
    suspend fun getDoctorDetail(id: String): Result<AdminDoctorDetail>
    suspend fun approveDoctor(id: String): Result<Unit>
    suspend fun rejectDoctor(id: String, reason: String): Result<Unit>

    // ── Disputes ─────────────────────────────────────────────────────────────
    suspend fun getDisputes(status: String?): Result<List<AdminDisputeItem>>
    suspend fun resolveDispute(id: String, resolution: String, resolutionNote: String): Result<Unit>

    // ── No-Shows ─────────────────────────────────────────────────────────────
    suspend fun getNoShows(): Result<List<AdminNoShowItem>>
    suspend fun resolveNoShow(id: String, resolution: String, note: String): Result<Unit>

    // ── Specialties ───────────────────────────────────────────────────────────
    suspend fun getSpecialties(): Result<List<AdminSpecialtyItem>>
    suspend fun createSpecialty(name: String, description: String?, icon: String?): Result<AdminSpecialtyItem>
    suspend fun getSpecialtyRequests(): Result<List<AdminSpecialtyRequestItem>>
    suspend fun resolveSpecialtyRequest(requestId: String, action: String, reason: String?): Result<Unit>

    // ── Patients ─────────────────────────────────────────────────────────────
    suspend fun getPatients(status: String?, q: String?): Result<List<AdminPatientListItem>>
    suspend fun suspendUser(userId: String, reason: String): Result<Unit>
    suspend fun unsuspendUser(userId: String): Result<Unit>

    // ── Reports ───────────────────────────────────────────────────────────────
    suspend fun getReports(status: String?): Result<List<AdminReportItem>>
    suspend fun resolveReport(reportId: String, status: String, adminNote: String?): Result<Unit>

    // ── Reviews ───────────────────────────────────────────────────────────────
    suspend fun getReviews(withComment: Boolean? = null, hidden: Boolean? = null): Result<List<AdminReviewItem>>
    suspend fun hideReview(appointmentId: String, reason: String): Result<Unit>
    suspend fun unhideReview(appointmentId: String): Result<Unit>

    // ── Blocked emails ────────────────────────────────────────────────────────
    suspend fun getBlockedEmails(): Result<List<AdminBlockedEmailItem>>
    suspend fun blockEmail(email: String, reason: String, durationDays: Int?): Result<Unit>
    suspend fun unblockEmail(email: String): Result<Unit>

    // ── Subscriptions ─────────────────────────────────────────────────────────
    suspend fun getSubscriptions(): Result<AdminSubscriptionsOverview>
    suspend fun setUserSubscription(userId: String, tier: String, expiresAt: String?): Result<Unit>
}

// ── Appointment list domain models ────────────────────────────────────────────

data class AdminAppointmentFilters(
    val status: String? = null,
    val from: String? = null,
    val to: String? = null,
    val doctorId: String? = null,
    val patientId: String? = null,
    val q: String? = null,
    val hasDispute: Boolean? = null,
)

data class AdminAppointmentPerson(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
) {
    val fullName: String get() = "$firstName $lastName"
    val initials: String get() = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase()
}

data class AdminAppointmentSpecialty(
    val id: String,
    val name: String,
)

data class AdminAppointmentListItem(
    val id: String,
    val status: String,
    val startTime: String,
    val price: Double,
    val commission: Double?,
    val disputeStatus: String?,
    val paymentStatus: String,
    val paymentHoldStatus: String?,
    val doctor: AdminAppointmentPerson,
    val patient: AdminAppointmentPerson,
    val specialty: AdminAppointmentSpecialty,
) {
    val hasDispute: Boolean get() = disputeStatus != null && disputeStatus != "NONE"
}

// ── Appointment detail domain model ──────────────────────────────────────────

data class AdminAppointmentDetail(
    val id: String,
    val status: String,
    val startTime: String,
    val price: Double,
    val commission: Double?,
    val disputeStatus: String?,
    val disputeReason: String?,
    val paymentStatus: String,
    val paymentHoldStatus: String?,
    val notes: String?,
    val rescheduleCount: Int,
    val doctor: AdminAppointmentPerson,
    val patient: AdminAppointmentPerson,
    val specialty: AdminAppointmentSpecialty,
) {
    val hasDispute: Boolean get() = disputeStatus != null && disputeStatus != "NONE"
}

/**
 * Domain model for the admin dashboard.
 *
 * Mirrors the `GET /api/admin/stats` payload 1:1 so the presentation layer can
 * derive every visible metric (hero, KPI grid, action queue) without a second
 * network call. All counts are non-negative integers; [monthRevenue] is in PEN.
 */
// ── Doctor domain models ──────────────────────────────────────────────────────

/** Lightweight user block returned inside doctor list responses. */
data class AdminDoctorUser(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val isSuspended: Boolean,
    val suspendedAt: String?,
    val suspensionReason: String?,
    val lastLogin: String?,
    val createdAt: String?,
) {
    val fullName: String get() = "$firstName $lastName"
    val initials: String get() = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase()
}

data class AdminDoctorSpecialty(val name: String)

/** Item in the GET /api/admin/doctors-list response. */
data class AdminDoctorListItem(
    val id: String,
    val isActive: Boolean,
    val createdAt: String?,
    val user: AdminDoctorUser,
    val specialties: List<AdminDoctorSpecialty>,
    val appointmentCount: Int,
) {
    val fullName: String get() = user.fullName
    val initials: String get() = user.initials
    val primarySpecialty: String get() = specialties.firstOrNull()?.name ?: "—"
    val statusLabel: String get() = when {
        user.isSuspended -> "SUSPENDIDO"
        isActive -> "ACTIVO"
        else -> "INACTIVO"
    }
}

/** Full doctor profile from GET /api/doctors/:id. */
data class AdminDoctorDetail(
    val id: String,
    val isActive: Boolean,
    val isFreelance: Boolean?,
    val cmpNumber: String?,
    val bio: String?,
    val rating: Double?,
    val reviewCount: Int?,
    val appointmentCount: Int?,
    val createdAt: String?,
    val user: AdminDoctorUser,
    val specialties: List<AdminDoctorSpecialty>,
)

/** Pending-approval doctor from GET /api/doctors/pending. */
data class AdminPendingDoctor(
    val id: String,
    val createdAt: String?,
    val cmpNumber: String?,
    val bio: String?,
    val user: AdminDoctorUser,
    val specialties: List<AdminDoctorSpecialty>,
    val documentCount: Int,
) {
    val fullName: String get() = user.fullName
    val initials: String get() = user.initials
    val primarySpecialty: String get() = specialties.firstOrNull()?.name ?: "—"
    /** Approximate wait time string from createdAt ISO string (hours/days). */
    val waitLabel: String get() = createdAt?.let { computeWaitLabel(it) } ?: "—"
}

private fun computeWaitLabel(iso: String): String = try {
    // Very lightweight: extract date+hour fields without a full date library.
    val datePart = iso.substring(0, 10)
    val hourPart = iso.substring(11, 13).toIntOrNull() ?: 0
    val parts = datePart.split("-")
    val year = parts[0].toInt(); val month = parts[1].toInt(); val day = parts[2].toInt()
    // Approximate days from a hardcoded "now" epoch — good enough for display.
    // We compare only date components; sub-day accuracy not needed for the list.
    val approxDayOfYear = (year - 2024) * 365 + (month - 1) * 30 + day
    val nowApprox = (2026 - 2024) * 365 + (6 - 1) * 30 + 1  // 2026-06-01 hardcoded floor
    val diffDays = nowApprox - approxDayOfYear
    when {
        diffDays <= 0 -> "${hourPart}h"
        diffDays == 1 -> "1d"
        else -> "${diffDays}d"
    }
} catch (_: Exception) { "—" }

// ── Dispute domain models ─────────────────────────────────────────────────────

/** Possible values: PROVIDER, PATIENT */
enum class DisputeResolution { PROVIDER, PATIENT }

/** Possible values: RELEASE_TO_DOCTOR, REFUND_TO_PATIENT */
enum class NoShowResolution { RELEASE_TO_DOCTOR, REFUND_TO_PATIENT }

data class AdminDisputeItem(
    val id: String,
    val status: String,
    val startTime: String,
    val price: Double,
    val disputeStatus: String?,
    val disputeReason: String?,
    val doctor: AdminAppointmentPerson,
    val patient: AdminAppointmentPerson,
    val specialty: AdminAppointmentSpecialty,
    val paymentGatewayId: String?,
    val paymentStatus: String,
) {
    val urgentLabel: String get() = when (disputeStatus) {
        "OPEN" -> "Urgente"
        else -> disputeStatus ?: "—"
    }
    val isUrgent: Boolean get() = disputeStatus == "OPEN"
}

data class AdminNoShowItem(
    val id: String,
    val startTime: String,
    val price: Double,
    val doctor: AdminAppointmentPerson,
    val patient: AdminAppointmentPerson,
    val specialty: AdminAppointmentSpecialty,
)

// ── Dashboard ─────────────────────────────────────────────────────────────────

// ── Finance ───────────────────────────────────────────────────────────────────

data class AdminFinancePeriod(
    val revenue: Double = 0.0,
    val commission: Double = 0.0,
    val appointments: Int = 0,
)

data class AdminFinanceHeld(
    val total: Double = 0.0,
    val count: Int = 0,
)

data class AdminFinanceRefunded(
    val total: Double = 0.0,
    val count: Int = 0,
)

data class AdminTopDoctor(
    val doctorId: String,
    val name: String,
    val appointments: Int,
    val totalRevenue: Double,
    val doctorEarnings: Double,
)

data class AdminFinance(
    val thisMonth: AdminFinancePeriod = AdminFinancePeriod(),
    val last30Days: AdminFinancePeriod = AdminFinancePeriod(),
    val last7Days: AdminFinancePeriod = AdminFinancePeriod(),
    /** Released revenue (price sum) and commission from all released appointments. */
    val totalReleasedRevenue: Double = 0.0,
    val totalReleasedCommission: Double = 0.0,
    val held: AdminFinanceHeld = AdminFinanceHeld(),
    val refunded: AdminFinanceRefunded = AdminFinanceRefunded(),
    val topDoctors: List<AdminTopDoctor> = emptyList(),
)

// ── Specialty catalog domain models ──────────────────────────────────────────

/**
 * Catalog item from GET /api/specialties.
 *
 * Backend returns: { id, name, description?, icon?, isActive, createdAt, updatedAt }.
 * The public endpoint only returns isActive=true entries; admin uses this too.
 * There is NO per-item doctorCount in this endpoint — chips are derived from isActive only.
 * "En revisión" chip cannot be backed by catalog data (no status field beyond isActive);
 * it is shown as a filter chip but always returns empty — documented gap.
 */
data class AdminSpecialtyItem(
    val id: String,
    val name: String,
    val description: String?,
    val icon: String?,
    val isActive: Boolean,
) {
    val initials: String
        get() = name.take(2).uppercase()
}

/**
 * Pending specialty request from GET /api/specialties/request (as SUPER_ADMIN).
 *
 * Backend returns a Prisma SpecialtyRequest with includes:
 * { id, status, comment?, createdAt, documents[], specialty: { id, name }, doctor: { user: { firstName, lastName, email }, specialties[] } }
 *
 * Fields NOT in the response: priority (Alta/Media), patient demand count.
 * We derive "Solicitada por N doctores" as 1 (the single requesting doctor per request).
 * Priority and demand are documented gaps — not faked.
 */
data class AdminSpecialtyRequestItem(
    val id: String,
    val status: String,
    val comment: String?,
    val createdAt: String?,
    val specialtyId: String,
    val specialtyName: String,
    val doctorFirstName: String,
    val doctorLastName: String,
    val doctorEmail: String,
) {
    val doctorFullName: String get() = "$doctorFirstName $doctorLastName"
    val doctorInitials: String get() = "${doctorFirstName.firstOrNull() ?: ""}${doctorLastName.firstOrNull() ?: ""}".uppercase()
}

data class AdminDashboard(
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

// ── Patient domain models ─────────────────────────────────────────────────────

/**
 * Item in the GET /api/admin/patients-list response.
 *
 * Backend returns: patient.id (patient-profile id), user { id, firstName, lastName,
 * email, phone, isSuspended, suspendedAt, suspensionReason, subscriptionTier,
 * lastLogin, createdAt }, _count { appointments, therapyPackages }.
 *
 * NOTE — no "verified" flag in this endpoint. The tier line is: subscriptionTier (FREE|PREMIUM).
 * "Observados" chip: NO field backs a "flagged" status on patients; the only status
 * is isSuspended. This chip is displayed but always returns empty — documented gap.
 *
 * Patient detail by-id: NO dedicated endpoint. Detalle screen is built from this list item.
 */
data class AdminPatientListItem(
    /** Patient profile id (prisma Patient.id) */
    val id: String,
    /** User (auth) id — used for suspend/unsuspend */
    val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val isSuspended: Boolean,
    val suspendedAt: String?,
    val suspensionReason: String?,
    val subscriptionTier: String,   // "FREE" | "PREMIUM"
    val lastLogin: String?,
    val createdAt: String?,
    val appointmentCount: Int,
    val therapyPackageCount: Int,
) {
    val fullName: String get() = "$firstName $lastName"
    val initials: String get() = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase()

    val statusLabel: String get() = if (isSuspended) "SUSPENDIDO" else "ACTIVO"
    val tierLabel: String get() = if (subscriptionTier == "PREMIUM") "Premium" else "Free"

    /** Approximate "last login N days ago" label derived from lastLogin ISO string. */
    val lastLoginLabel: String?
        get() = lastLogin?.let { computeRelativeDayLabel(it) }
}

/**
 * Same lightweight approximation as [computeWaitLabel] but returns a human-readable
 * "last seen N days ago" string for patient list cards.
 */
private fun computeRelativeDayLabel(iso: String): String = try {
    val datePart = iso.substring(0, 10)
    val parts = datePart.split("-")
    val year = parts[0].toInt(); val month = parts[1].toInt(); val day = parts[2].toInt()
    val approxDayOfYear = (year - 2024) * 365 + (month - 1) * 30 + day
    val nowApprox = (2026 - 2024) * 365 + (6 - 1) * 30 + 1
    val diffDays = nowApprox - approxDayOfYear
    when {
        diffDays <= 0 -> "hoy"
        diffDays == 1 -> "ayer"
        diffDays < 30 -> "hace ${diffDays}d"
        else -> "hace ${diffDays / 30}m"
    }
} catch (_: Exception) { "" }

// ── Report domain models ──────────────────────────────────────────────────────

/**
 * Status enum values: PENDING | REVIEWED | ACTION_TAKEN | DISMISSED
 * Category values: spam | abuse | fraud | other
 * Role values: SUPER_ADMIN | DOCTOR | PATIENT (from backend user.role)
 *
 * Chip mapping:
 *   Pendientes  → status=PENDING  (server filter)
 *   Revisados   → status=ALL, client-side: REVIEWED | ACTION_TAKEN | DISMISSED
 *   Todos       → status=ALL (no filter)
 *
 * Resolve options → status body:
 *   "Marcar revisado"  → REVIEWED
 *   "Descartar"        → DISMISSED
 *   "Tomar acción"     → ACTION_TAKEN
 */
data class AdminReportUser(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String,
) {
    val fullName: String get() = "$firstName $lastName"
    val initials: String get() = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase()
    /** Human-readable role label */
    val roleLabel: String get() = when (role) {
        "DOCTOR" -> "Doctor"
        "PATIENT" -> "Paciente"
        "SUPER_ADMIN" -> "Admin"
        else -> role
    }
}

data class AdminReportItem(
    val id: String,
    val status: String,        // PENDING | REVIEWED | ACTION_TAKEN | DISMISSED
    val category: String,      // spam | abuse | fraud | other
    val reason: String,        // full description / motivo
    val createdAt: String?,
    val reportedUser: AdminReportUser,
    val reporter: AdminReportUser?,
) {
    val isResolved: Boolean
        get() = status != "PENDING"

    /** Short category label for display */
    val categoryLabel: String
        get() = when (category) {
            "spam" -> "Spam"
            "abuse" -> "Abuso"
            "fraud" -> "Fraude"
            else -> "Otro"
        }

    /** Approximate age string from createdAt ISO string (e.g. "2h", "3d") */
    val ageLabel: String
        get() = createdAt?.let { computeReportAgeLabel(it) } ?: "—"

    /** Short excerpt of reason for list card */
    val reasonExcerpt: String
        get() = if (reason.length > 120) reason.take(120) + "…" else reason
}

private fun computeReportAgeLabel(iso: String): String = try {
    val datePart = iso.substring(0, 10)
    val timePart = iso.substring(11, 16)
    val parts = datePart.split("-")
    val year = parts[0].toInt(); val month = parts[1].toInt(); val day = parts[2].toInt()
    val hour = timePart.substring(0, 2).toIntOrNull() ?: 0
    val approxDayOfYear = (year - 2024) * 365 + (month - 1) * 30 + day
    val nowApprox = (2026 - 2024) * 365 + (6 - 1) * 30 + 1
    val diffDays = nowApprox - approxDayOfYear
    when {
        diffDays <= 0 -> "${hour}h"
        diffDays == 1 -> "1d"
        diffDays < 30 -> "${diffDays}d"
        else -> "${diffDays / 30}m"
    }
} catch (_: Exception) { "—" }

// ── Review domain models ──────────────────────────────────────────────────────

/**
 * Domain model for a review returned by GET /api/admin/reviews.
 *
 * Backend returns items from the Appointment table with the rating fields.
 * Key: [appointmentId] (= the Appointment.id) — used for hide/unhide calls.
 *
 * Chip → query mapping:
 *   Todas        → no filter  (hidden="all" implicitly)
 *   Con comentario → withComment=true
 *   Ocultas      → hidden=true
 *
 * isHidden = reviewHiddenAt != null (we carry the ISO string; non-null means hidden).
 */
data class AdminReviewPerson(
    val firstName: String,
    val lastName: String,
) {
    val fullName: String get() = "$firstName $lastName"
    val initials: String get() = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase()
}

data class AdminReviewItem(
    /** Appointment.id — used as the path param for hide/unhide. */
    val appointmentId: String,
    val rating: Int,
    val comment: String?,
    /** Non-null ISO string means the review is hidden. */
    val reviewHiddenAt: String?,
    val reviewHiddenReason: String?,
    /** ISO datetime of the appointment (startTime). */
    val startTime: String?,
    val patient: AdminReviewPerson,
    val doctor: AdminReviewPerson,
) {
    val isHidden: Boolean get() = reviewHiddenAt != null

    /** "hace Nd" relative label derived from startTime. */
    val ageLabel: String get() = startTime?.let { computeReviewAgeLabel(it) } ?: "—"
}

// ── Blocked email domain model ────────────────────────────────────────────────

/**
 * Domain model for a blocked-email record returned by GET /api/admin/blocked-emails.
 *
 * Backend fields: { id, email, reason, blockedAt, expiresAt?, blockedBy? }
 *
 * Metrics backed by real data:
 *   - Total blocked count = list.size (real)
 *   - Distinct domains    = list.distinctBy { it.domain }.size (derived client-side)
 *
 * "Intentos últimos 7 días" is NOT in the payload — omitted entirely.
 */
data class AdminBlockedEmailItem(
    val id: String,
    val email: String,
    val reason: String,
    /** ISO datetime of when the email was blocked */
    val blockedAt: String?,
    /** ISO datetime of expiry, null = permanent block */
    val expiresAt: String?,
    val blockedBy: String?,
) {
    /** Domain part of the email (e.g. "gmail.com") */
    val domain: String get() = email.substringAfterLast("@", email)

    /** Short label: reason excerpt (max 80 chars) */
    val reasonExcerpt: String
        get() = if (reason.length > 80) reason.take(80) + "…" else reason

    /**
     * Human-readable expiry/permanence label.
     * Shows the ISO date portion only (e.g. "2026-08-01"); permanent blocks show "Permanente".
     */
    val expiryLabel: String
        get() = expiresAt?.take(10) ?: "Permanente"
}

private fun computeReviewAgeLabel(iso: String): String = try {
    val datePart = iso.substring(0, 10)
    val parts = datePart.split("-")
    val year = parts[0].toInt(); val month = parts[1].toInt(); val day = parts[2].toInt()
    val approxDayOfYear = (year - 2024) * 365 + (month - 1) * 30 + day
    val nowApprox = (2026 - 2024) * 365 + (6 - 1) * 30 + 1
    val diffDays = nowApprox - approxDayOfYear
    when {
        diffDays <= 0 -> "hoy"
        diffDays == 1 -> "ayer"
        diffDays < 30 -> "hace ${diffDays}d"
        else -> "hace ${diffDays / 30}m"
    }
} catch (_: Exception) { "—" }

// ── Subscription domain models ────────────────────────────────────────────────

/**
 * Status values for a subscription item:
 * - ACTIVE   : tier=PREMIUM, expiresAt is null or > 30 days from now
 * - EXPIRING : tier=PREMIUM, expiresAt within the next 30 days
 * - EXPIRED  : tier=FREE but subscriptionExpiresAt is set (was PREMIUM, now lapsed)
 */
enum class SubscriptionStatus { ACTIVE, EXPIRING, EXPIRED }

/**
 * A single doctor-subscription entry from GET /api/admin/subscriptions.
 *
 * Real fields backed by DB:
 *   - userId, doctorId, name, tier, expiresAt, status
 *
 * NOT in the response (not fabricated):
 *   - renewal%, churn%, revenue per subscription.
 */
data class AdminSubscriptionItem(
    val userId: String,
    val doctorId: String?,
    val name: String,
    /** "FREE" | "PREMIUM" */
    val tier: String,
    /** ISO 8601 or null (no expiry date set) */
    val expiresAt: String?,
    val status: SubscriptionStatus,
) {
    val initials: String get() = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()

    /** Short expiry label for list cards. */
    val expiryLabel: String
        get() = when {
            expiresAt == null -> "Sin vencimiento"
            status == SubscriptionStatus.EXPIRED -> "Venció ${expiresAt.take(10)}"
            else -> "Vence ${expiresAt.take(10)}"
        }
}

/**
 * Response shape for GET /api/admin/subscriptions.
 *
 * stats contains only counts that are HONESTLY derivable from data:
 *   - activeCount     : PREMIUM doctors not expiring within 30 days
 *   - expiringSoonCount: PREMIUM doctors expiring within 30 days
 *   - expiredCount    : doctors that were PREMIUM but have since lapsed (tier=FREE, expiresAt set)
 */
data class AdminSubscriptionStats(
    val activeCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val expiredCount: Int = 0,
)

data class AdminSubscriptionsOverview(
    val stats: AdminSubscriptionStats = AdminSubscriptionStats(),
    val items: List<AdminSubscriptionItem> = emptyList(),
)
