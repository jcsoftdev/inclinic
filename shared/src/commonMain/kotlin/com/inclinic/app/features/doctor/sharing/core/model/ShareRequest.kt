package com.inclinic.app.features.doctor.sharing.core.model

import kotlin.time.Instant

enum class ShareRequestStatus { PENDING, APPROVED, REJECTED, EXPIRED, REVOKED }

/** Scope of the share request */
enum class ShareScope { FULL_HISTORY, SPECIFIC_RECORDS }

data class ShareRequest(
    val id: String,
    val patientId: String,
    val patientName: String,
    val requesterDoctorId: String,
    val reason: String,
    val scope: ShareScope,
    val status: ShareRequestStatus,
    val requestedAt: Instant,
    val expiresAt: Instant?,
)
