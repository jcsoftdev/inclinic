package com.inclinic.app.features.doctor.packages.core.model

/**
 * An active therapy package purchased by a specific patient.
 *
 * Mirrors the backend `TherapyPackage` model (`/api/therapy-packages`):
 * a doctor sells a multi-session package to one patient, optionally prepaid
 * with a discount. Prices are per-session amounts in soles (S/.).
 */
data class TherapyPackage(
    val id: String,
    val patientId: String,
    val patientName: String,
    val patientEmail: String,
    val specialtyId: String,
    val specialtyName: String,
    val packageName: String,
    val totalSessions: Int,
    val regularPricePerSession: Double,
    val packagePricePerSession: Double,
    val isPrepaid: Boolean,
    val prepaidDiscount: Double?,
    val totalPrepaidAmount: Double?,
    val sessionsCompleted: Int,
    val sessionsScheduled: Int,
    val sessionsUsed: Int,
    val status: PackageStatus,
    val sessions: List<PackageSession>,
) {
    /** Prepaid discount as a positive percentage, 0 when not prepaid. */
    val prepaidDiscountPercent: Int get() = (prepaidDiscount ?: 0.0).toInt()

    /** Doctor's expected income = package price x sessions (pre-commission, in soles). */
    val expectedIncome: Double get() = packagePricePerSession * totalSessions
}

enum class PackageStatus {
    PENDING_PAYMENT,
    ACTIVE,
    COMPLETED,
    CANCELLED,
    UNKNOWN,
    ;

    companion object {
        fun fromRaw(raw: String): PackageStatus = when (raw.uppercase()) {
            "PENDING_PAYMENT" -> PENDING_PAYMENT
            "ACTIVE" -> ACTIVE
            "COMPLETED" -> COMPLETED
            "CANCELLED" -> CANCELLED
            else -> UNKNOWN
        }
    }
}

/** One scheduled / completed session inside a package, shown in the detail screen. */
data class PackageSession(
    val id: String,
    val index: Int,
    val title: String,
    val subtitle: String,
    val status: PackageSessionStatus,
)

enum class PackageSessionStatus { COMPLETED, UPCOMING, UNSCHEDULED }
