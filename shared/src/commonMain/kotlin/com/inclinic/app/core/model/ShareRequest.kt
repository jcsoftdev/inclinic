package com.inclinic.app.core.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ShareRequest(
    val id: String,
    val doctorId: String,
    val doctorName: String? = null,
    val specialtyName: String? = null,
    val reason: String? = null,
    val scope: ShareScope,
    val status: ShareStatus,
    val requestedAt: Instant,
    val expiresAt: Instant? = null,
    val duration: Int? = null,
    val recordsRequested: List<String>? = null,
)

@Serializable
enum class ShareScope { FULL_HISTORY, SPECIFIC_RECORDS }

@Serializable
enum class ShareStatus { PENDING, APPROVED, REJECTED, EXPIRED, REVOKED }
