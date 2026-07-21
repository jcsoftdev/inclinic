package com.inclinic.app.features.doctor.notifications.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.notifications.core.model.DoctorNotification
import com.inclinic.app.features.doctor.notifications.core.model.NotificationKind
import com.inclinic.app.features.doctor.notifications.core.port.NotificationFilter

interface DoctorNotificationsComponent {
    val state: Value<DoctorNotificationsState>

    fun onRefresh()
    fun onFilterChange(filter: NotificationFilter)
    fun onMarkRead(id: String)
    fun onMarkAllRead()

    /** Al tocar una notificación: la marca leída y navega a su recurso (deep-link). */
    fun onNotificationClick(notification: DoctorNotification)
    fun onBack()

    sealed interface Output {
        data object Back : Output
        data class OpenModalityRequest(val requestId: String) : Output
        data class OpenPackageNegotiation(val negotiationId: String) : Output
        data class OpenAppointment(val appointmentId: String) : Output
    }
}

data class DoctorNotificationsState(
    val isLoading: Boolean = false,
    val notifications: List<DoctorNotification> = emptyList(),
    val activeFilter: NotificationFilter = NotificationFilter.ALL,
    val error: String? = null,
) {
    val filteredNotifications: List<DoctorNotification>
        get() = when (activeFilter) {
            NotificationFilter.ALL -> notifications
            NotificationFilter.APPOINTMENTS -> notifications.filter { it.kind == NotificationKind.APPOINTMENT }
            NotificationFilter.PAYMENTS -> notifications.filter { it.kind == NotificationKind.PAYMENT }
            NotificationFilter.SHARE -> notifications.filter { it.kind == NotificationKind.SHARE }
        }
}
