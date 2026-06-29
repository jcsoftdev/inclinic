package com.inclinic.app.features.doctor.notifications.infrastructure

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.notifications.core.model.DoctorNotification
import com.inclinic.app.features.doctor.notifications.core.model.NotificationKind
import com.inclinic.app.features.doctor.notifications.core.port.DoctorNotificationsRepository
import com.inclinic.app.features.doctor.notifications.core.port.NotificationFilter
import com.inclinic.app.features.doctor.notifications.infrastructure.remote.DoctorNotificationsDataSource
import com.inclinic.app.features.doctor.notifications.infrastructure.remote.NotificationDto
import kotlinx.coroutines.withContext
import kotlin.time.Instant

class DefaultDoctorNotificationsRepository(
    private val remote: DoctorNotificationsDataSource,
    private val dispatchers: AppDispatchers,
) : DoctorNotificationsRepository {

    override suspend fun list(filter: NotificationFilter): Result<List<DoctorNotification>> =
        withContext(dispatchers.io) {
            remote.list().map { response ->
                response.items.map { it.toDomain() }
            }
        }

    override suspend fun markRead(id: String): Result<Unit> =
        withContext(dispatchers.io) { remote.markRead(id) }

    override suspend fun markAllRead(): Result<Unit> =
        withContext(dispatchers.io) { remote.markAllRead() }

    private fun NotificationDto.toDomain() = DoctorNotification(
        id = id,
        kind = typeToKind(type),
        title = title,
        body = body ?: "",
        createdAt = runCatching { Instant.parse(createdAt) }.getOrElse { Instant.fromEpochMilliseconds(0) },
        isRead = isRead,
        link = link,
    )

    private fun typeToKind(type: String): NotificationKind = when {
        type.startsWith("APPOINTMENT") || type.startsWith("RESCHEDULE") -> NotificationKind.APPOINTMENT
        type.startsWith("PAYMENT") || type.startsWith("DISPUTE") -> NotificationKind.PAYMENT
        type == "CHAT_MESSAGE" -> NotificationKind.MESSAGE
        type.startsWith("SHARE") -> NotificationKind.SHARE
        type == "DOCTOR_APPROVED" || type == "DOCTOR_REJECTED" ||
            type.startsWith("SPECIALTY") -> NotificationKind.REVIEW
        else -> NotificationKind.SYSTEM
    }
}
