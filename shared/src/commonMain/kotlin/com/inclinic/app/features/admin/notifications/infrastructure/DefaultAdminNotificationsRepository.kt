package com.inclinic.app.features.admin.notifications.infrastructure

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.notifications.core.model.AdminNotification
import com.inclinic.app.features.admin.notifications.core.model.AdminNotificationKind
import com.inclinic.app.features.admin.notifications.core.port.AdminNotificationsRepository
import com.inclinic.app.features.admin.notifications.infrastructure.remote.AdminNotificationDto
import com.inclinic.app.features.admin.notifications.infrastructure.remote.AdminNotificationsDataSource
import kotlinx.coroutines.withContext
import kotlin.time.Instant

class DefaultAdminNotificationsRepository(
    private val remote: AdminNotificationsDataSource,
    private val dispatchers: AppDispatchers,
) : AdminNotificationsRepository {

    override suspend fun list(limit: Int): Result<List<AdminNotification>> =
        withContext(dispatchers.io) {
            remote.list(limit).map { response -> response.items.map { it.toDomain() } }
        }

    override suspend fun markRead(id: String): Result<Unit> =
        withContext(dispatchers.io) { remote.markRead(id) }

    override suspend fun markAllRead(): Result<Unit> =
        withContext(dispatchers.io) { remote.markAllRead() }

    override suspend fun delete(id: String): Result<Unit> =
        withContext(dispatchers.io) { remote.delete(id) }

    // ── Mapping helpers ───────────────────────────────────────────────────────

    private fun AdminNotificationDto.toDomain() = AdminNotification(
        id = id,
        kind = typeToKind(type),
        title = title,
        body = body ?: "",
        createdAt = runCatching { Instant.parse(createdAt) }.getOrElse { Instant.fromEpochMilliseconds(0) },
        isRead = isRead,
        link = link,
    )

    private fun typeToKind(type: String): AdminNotificationKind = when {
        type.startsWith("APPOINTMENT") || type.startsWith("RESCHEDULE") -> AdminNotificationKind.APPOINTMENT
        type.startsWith("PAYMENT") || type.startsWith("DISPUTE") -> AdminNotificationKind.PAYMENT
        type == "DOCTOR_APPROVED" || type == "DOCTOR_REJECTED" -> AdminNotificationKind.DOCTOR
        type.startsWith("SPECIALTY") -> AdminNotificationKind.SPECIALTY
        type == "CHAT_MESSAGE" -> AdminNotificationKind.MESSAGE
        else -> AdminNotificationKind.SYSTEM
    }
}
