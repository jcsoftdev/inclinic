package com.inclinic.app.features.doctor.notifications.infrastructure.remote

import kotlinx.serialization.Serializable

interface DoctorNotificationsDataSource {
    /** GET /api/notifications?limit=30 */
    suspend fun list(limit: Int = 30): Result<NotificationsResponseDto>
    /** PUT /api/notifications/{id} — marks a single notification as read */
    suspend fun markRead(id: String): Result<Unit>
    /** POST /api/notifications/mark-all-read */
    suspend fun markAllRead(): Result<Unit>
}

/**
 * Wrapper matching getUserNotifications response: { items: [...], unreadCount: N }
 */
@Serializable
data class NotificationsResponseDto(
    val items: List<NotificationDto> = emptyList(),
    val unreadCount: Int = 0,
)

/**
 * Matches the Prisma Notification model fields as serialized by Next.js:
 * id, userId, type, title, body, link, resourceType, resourceId, isRead, readAt, createdAt
 */
@Serializable
data class NotificationDto(
    val id: String,
    val type: String,
    val title: String,
    val body: String? = null,
    val link: String? = null,
    val resourceType: String? = null,
    val resourceId: String? = null,
    val isRead: Boolean = false,
    val createdAt: String,
)
