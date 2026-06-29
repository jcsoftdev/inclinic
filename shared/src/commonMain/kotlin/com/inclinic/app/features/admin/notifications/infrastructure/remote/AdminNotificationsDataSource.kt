package com.inclinic.app.features.admin.notifications.infrastructure.remote

import kotlinx.serialization.Serializable

interface AdminNotificationsDataSource {
    /** GET /api/notifications?limit=30 */
    suspend fun list(limit: Int = 30): Result<AdminNotificationsResponseDto>
    /** POST /api/notifications/:id/read */
    suspend fun markRead(id: String): Result<Unit>
    /** POST /api/notifications/read-all */
    suspend fun markAllRead(): Result<Unit>
    /** DELETE /api/notifications/:id */
    suspend fun delete(id: String): Result<Unit>
}

/**
 * Matches the backend getUserNotifications response: { items: [...], unreadCount: N }
 */
@Serializable
data class AdminNotificationsResponseDto(
    val items: List<AdminNotificationDto> = emptyList(),
    val unreadCount: Int = 0,
)

/**
 * Matches the Prisma Notification model fields returned by the API:
 * id, userId, type, title, body, link, resourceType, resourceId, isRead, readAt, createdAt
 */
@Serializable
data class AdminNotificationDto(
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
