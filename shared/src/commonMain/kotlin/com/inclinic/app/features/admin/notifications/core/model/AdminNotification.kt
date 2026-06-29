package com.inclinic.app.features.admin.notifications.core.model

import kotlin.time.Instant

/**
 * Maps backend NotificationType enum to display categories used in the admin screen.
 *
 * Backend types: APPOINTMENT_*, DISPUTE_*, PAYMENT_*, DOCTOR_*, SPECIALTY_*,
 * SHARE_REQUEST_*, RESCHEDULE_*, CHAT_MESSAGE, SYSTEM
 */
enum class AdminNotificationKind { APPOINTMENT, PAYMENT, DOCTOR, SPECIALTY, SYSTEM, MESSAGE }

data class AdminNotification(
    val id: String,
    val kind: AdminNotificationKind,
    val title: String,
    val body: String,
    val createdAt: Instant,
    val isRead: Boolean,
    /** Optional deep-link path from the backend (e.g. /admin/appointments/123). */
    val link: String?,
)
