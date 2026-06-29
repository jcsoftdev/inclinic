package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.AppNotification

interface NotificationDataSource {
    suspend fun getNotifications(limit: Int = 30): Result<List<AppNotification>>
    suspend fun markAllRead(): Result<Unit>
}
