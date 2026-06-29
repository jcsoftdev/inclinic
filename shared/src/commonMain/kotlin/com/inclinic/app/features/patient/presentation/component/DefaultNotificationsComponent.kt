package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.AppNotification
import com.inclinic.app.core.model.NotificationType
import com.inclinic.app.features.patient.notifications.application.GetNotificationsUseCase
import com.inclinic.app.features.patient.notifications.application.MarkAllNotificationsReadUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultNotificationsComponent(
    componentContext: ComponentContext,
    private val getNotifications: GetNotificationsUseCase,
    private val markAllRead: MarkAllNotificationsReadUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (NotificationsComponent.Output) -> Unit,
) : NotificationsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(NotificationsState())
    override val state: Value<NotificationsState> = _state

    init { load() }

    override fun onFilterChange(filter: NotificationFilter) {
        _state.update { it.copy(filter = filter) }
    }

    override fun onMarkAllRead() {
        scope.launch {
            markAllRead().onSuccess {
                _state.update { s -> s.copy(notifications = s.notifications.map { it.copy(read = true) }) }
            }
        }
    }

    override fun onNotificationClick(notification: AppNotification) {
        val appointmentId = notification.metadata["appointmentId"] ?: return
        when (notification.type) {
            NotificationType.APPOINTMENT -> onOutput(NotificationsComponent.Output.NavigateToAppointment(appointmentId))
            NotificationType.PAYMENT -> onOutput(NotificationsComponent.Output.NavigateToPayment(appointmentId))
            else -> {}
        }
    }

    override fun onBack() { onOutput(NotificationsComponent.Output.Back) }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getNotifications()
                .onSuccess { list -> _state.update { it.copy(isLoading = false, notifications = list) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error al cargar notificaciones")) } }
        }
    }
}
