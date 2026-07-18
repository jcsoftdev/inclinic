package com.inclinic.app.features.admin.notifications.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.notifications.core.model.AdminNotification
import com.inclinic.app.features.admin.notifications.core.model.AdminNotificationKind
import com.inclinic.app.features.admin.notifications.core.port.AdminNotificationFilter

interface AdminNotificationsComponent {
    val state: Value<AdminNotificationsState>

    fun onRefresh()
    fun onFilterChange(filter: AdminNotificationFilter)
    fun onMarkRead(id: String)
    fun onMarkAllRead()
    fun onDelete(id: String)
    fun onBack()

    /**
     * User tapped a notification card. Always marks it read (if unread); additionally emits a
     * navigate [Output] keyed by [AdminNotification.kind] when the notification's `link` carries
     * a parseable id AND the kind maps to a known destination. Unmapped kinds (MESSAGE, SYSTEM)
     * or a missing/unparseable link never crash — the notification is still marked read and the
     * caller simply stays on the notifications list (no [Output] beyond the mark-read side effect).
     */
    fun onNotificationClick(notification: AdminNotification)

    sealed interface Output {
        data object Back : Output
        /** [AdminNotificationKind.APPOINTMENT] with a parseable id in `link`. */
        data class NavigateToAppointment(val appointmentId: String) : Output
        /** [AdminNotificationKind.DOCTOR] with a parseable id in `link`. */
        data class NavigateToDoctor(val doctorId: String) : Output
        /** [AdminNotificationKind.SPECIALTY] — routes to the specialty-requests list (no per-id detail screen exists). */
        data object NavigateToSpecialtyRequests : Output
        /** [AdminNotificationKind.PAYMENT] — routes to the aggregate Finance screen (no per-id payment detail screen exists). */
        data object NavigateToFinance : Output
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
