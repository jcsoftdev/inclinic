package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.AppNotification
import com.inclinic.app.core.model.NotificationType

interface NotificationsComponent {
    val state: Value<NotificationsState>

    fun onFilterChange(filter: NotificationFilter)
    fun onMarkAllRead()
    fun onNotificationClick(notification: AppNotification)
    fun onBack()

    sealed interface Output {
        data object Back : Output
        data class NavigateToAppointment(val appointmentId: String) : Output
        data class NavigateToPayment(val appointmentId: String) : Output
    }
}

enum class NotificationFilter { ALL, APPOINTMENTS, PAYMENTS, HISTORY }

data class NotificationsState(
    val notifications: List<AppNotification> = emptyList(),
    val filter: NotificationFilter = NotificationFilter.ALL,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val filteredNotifications: List<AppNotification>
        get() = when (filter) {
            NotificationFilter.ALL -> notifications
            NotificationFilter.APPOINTMENTS -> notifications.filter { it.type == NotificationType.APPOINTMENT }
            NotificationFilter.PAYMENTS -> notifications.filter { it.type == NotificationType.PAYMENT }
            NotificationFilter.HISTORY -> notifications.filter { it.type == NotificationType.MEDICAL_HISTORY }
        }
}
