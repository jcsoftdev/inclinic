package com.inclinic.app.features.doctor.notifications.fakes

import com.inclinic.app.features.doctor.notifications.core.model.DoctorNotification
import com.inclinic.app.features.doctor.notifications.core.model.NotificationKind
import com.inclinic.app.features.doctor.notifications.core.port.DoctorNotificationsRepository
import com.inclinic.app.features.doctor.notifications.core.port.NotificationFilter
import kotlin.time.Instant

class FakeDoctorNotificationsRepository : DoctorNotificationsRepository {

    var listResult: Result<List<DoctorNotification>> = Result.success(emptyList())
    var markReadResult: Result<Unit> = Result.success(Unit)
    var markAllReadResult: Result<Unit> = Result.success(Unit)

    var listCallCount = 0
    var markReadCallCount = 0
    var markAllReadCallCount = 0
    var lastFilter: NotificationFilter? = null
    var lastMarkReadId: String? = null

    override suspend fun list(filter: NotificationFilter): Result<List<DoctorNotification>> {
        listCallCount++
        lastFilter = filter
        return listResult
    }

    override suspend fun markRead(id: String): Result<Unit> {
        markReadCallCount++
        lastMarkReadId = id
        return markReadResult
    }

    override suspend fun markAllRead(): Result<Unit> {
        markAllReadCallCount++
        return markAllReadResult
    }
}

fun stubNotification(id: String, isRead: Boolean = false, kind: NotificationKind = NotificationKind.SYSTEM) =
    DoctorNotification(
        id = id,
        kind = kind,
        title = "Notification $id",
        body = "Body $id",
        createdAt = Instant.fromEpochMilliseconds(0),
        isRead = isRead,
        link = null,
    )
