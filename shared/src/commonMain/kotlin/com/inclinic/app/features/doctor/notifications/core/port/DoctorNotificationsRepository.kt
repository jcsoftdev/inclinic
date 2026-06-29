package com.inclinic.app.features.doctor.notifications.core.port

import com.inclinic.app.features.doctor.notifications.core.model.DoctorNotification

interface DoctorNotificationsRepository {
    suspend fun list(filter: NotificationFilter): Result<List<DoctorNotification>>
    suspend fun markRead(id: String): Result<Unit>
    suspend fun markAllRead(): Result<Unit>
}

enum class NotificationFilter { ALL, APPOINTMENTS, PAYMENTS, SHARE }
