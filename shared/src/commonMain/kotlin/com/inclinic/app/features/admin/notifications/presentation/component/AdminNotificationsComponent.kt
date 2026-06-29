package com.inclinic.app.features.admin.notifications.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.notifications.core.model.AdminNotification
import com.inclinic.app.features.admin.notifications.core.port.AdminNotificationFilter

interface AdminNotificationsComponent {
    val state: Value<AdminNotificationsState>

    fun onRefresh()
    fun onFilterChange(filter: AdminNotificationFilter)
    fun onMarkRead(id: String)
    fun onMarkAllRead()
    fun onDelete(id: String)
    fun onBack()

    sealed interface Output {
        data object Back : Output
    }
}

data class AdminNotificationsState(
    val isLoading: Boolean = false,
    val notifications: List<AdminNotification> = emptyList(),
    val activeFilter: AdminNotificationFilter = AdminNotificationFilter.ALL,
    val error: String? = null,
    val isActing: Boolean = false,
) {
    val filteredNotifications: List<AdminNotification>
        get() = when (activeFilter) {
            AdminNotificationFilter.ALL -> notifications
            AdminNotificationFilter.UNREAD -> notifications.filter { !it.isRead }
            AdminNotificationFilter.READ -> notifications.filter { it.isRead }
        }
}
