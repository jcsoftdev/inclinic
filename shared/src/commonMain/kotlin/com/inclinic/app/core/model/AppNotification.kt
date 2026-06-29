package com.inclinic.app.core.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val read: Boolean = false,
    val createdAt: Instant,
    val metadata: Map<String, String> = emptyMap(),
)

@Serializable
enum class NotificationType {
    APPOINTMENT,
    PAYMENT,
    MEDICAL_HISTORY,
    SYSTEM,
}
