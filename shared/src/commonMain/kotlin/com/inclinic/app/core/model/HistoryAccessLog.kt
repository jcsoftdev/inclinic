package com.inclinic.app.core.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class HistoryAccessLog(
    val id: String,
    val doctorId: String,
    val doctorName: String? = null,
    val accessType: AccessType,
    val ipAddress: String? = null,
    val deviceInfo: String? = null,
    val accessedAt: Instant,
)

@Serializable
enum class AccessType { READ, EXPORT_PDF, FULL_HISTORY, RECORDS_ONLY }
