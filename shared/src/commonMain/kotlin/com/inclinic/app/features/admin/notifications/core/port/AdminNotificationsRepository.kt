package com.inclinic.app.features.admin.notifications.core.port

import com.inclinic.app.features.admin.notifications.core.model.AdminNotification

interface AdminNotificationsRepository {
    /** GET /api/notifications?limit=30 */
    suspend fun list(limit: Int = 30): Result<List<AdminNotification>>
    /** POST /api/notifications/:id/read */
    suspend fun markRead(id: String): Result<Unit>
    /** POST /api/notifications/read-all */
    suspend fun markAllRead(): Result<Unit>
    /** DELETE /api/notifications/:id */
    suspend fun delete(id: String): Result<Unit>
}

/**
 * Client-side filter chips for the admin notifications screen.
 *
 * ALL     → show all
 * UNREAD  → isRead == false  ("No leídas")
 * READ    → isRead == true   ("Resueltas" — "resolved" concept maps to read in this backend)
 */
enum class AdminNotificationFilter(val label: String) {
    ALL("Todas"),
    UNREAD("No leídas"),
    READ("Resueltas"),
}
