package com.inclinic.app.features.doctor.notifications.core.model

import kotlin.time.Instant

/**
 * Maps backend NotificationType enum to display categories.
 * Backend types: APPOINTMENT_*, DISPUTE_*, PAYMENT_*, DOCTOR_*, SPECIALTY_*,
 * SHARE_REQUEST_*, RESCHEDULE_*, CHAT_MESSAGE, SYSTEM
 */
enum class NotificationKind { APPOINTMENT, PAYMENT, REVIEW, SYSTEM, MESSAGE, SHARE }

data class DoctorNotification(
    val id: String,
    val kind: NotificationKind,
    val title: String,
    val body: String,
    val createdAt: Instant,
    val isRead: Boolean,
    /** Navigation link from the backend (e.g. /doctor/appointments/123) */
    val link: String?,
    /** Deep-link target: qué recurso abre la notificación (ej. "ModalityRequest", "PackageNegotiation"). */
    val resourceType: String? = null,
    val resourceId: String? = null,
)
